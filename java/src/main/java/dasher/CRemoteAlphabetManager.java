/*
  This file is part of JDasher.

  JDasher is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  JDasher is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with JDasher; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

  Copyright (C) 2006      Christopher Smowton <cs448@cam.ac.uk>

  JDasher is a port derived from the Dasher project; for information on
  the project see www.dasher.org.uk; for information on JDasher itself
  and related projects see www.smowton.net/chris

*/

package dasher;

import java.util.LinkedList;
import java.util.Queue;

/** 
 * This is part of an experiment with remote language modelling. It is
 * designed for use with the CRemotePPM language model. 
 * <p>
 * This class is designed to allow us to update nodes asynchronously whilst
 * using as much of Dasher's existing code as possible. Historically, the task would go:
 * <p><ul><li>AlphabetManager is asked to rebuild a node
 * <li>It asks the Model for the probabilities required, supplying a 
 * normalisation constant
 * <li>Which in turn modifies this constant to produce 'non-uniform norm' and 
 * asks the langauge model
 * <li>The model takes the language model's reply and performs some 'post-processing'
 * <li>Finally the alphManager can rebuild based on these probabilities
 * </ul><p>
 * My new schema is: When asked to populate a node, we note the details of the
 * request and place it in a queue. We directly notify the language model of the
 * requirement, supplying the model-calculated normalisation constant, the
 * generation of which has been refactored out into a seperate function,
 * getNonUniformNorm(int iNorm).
 * <p>
 * When the reply comes back, we apply the model's post-processing by calling
 * AdjustProbs(long[] probs), which again used to be a part of the getProbs sequence
 * but is now a seperate function so that the model *can* do its modifications
 * on probabilities which come from elsewhere. The main codepath has been modified
 * so that it calls the appropriate refactored functions, also increasing code
 * readability.
 */
public class CRemoteAlphabetManager extends CAlphabetManager implements ProbsListener {
	
	/**
	 * Struct describing a pending PopulateChildrenWithSymbol
	 */
	class PendingPopulate {
		
		/**
		 * Node to be populated
		 */
		CDasherNode Node;
		
		/**
		 * Symbol belonging to its existing child, if any
		 */
		int iExistingSymbol;
		
		/**
		 * Existing child of the Node we are populating, if any
		 */
		CDasherNode ExistingChild;
		
	}

	
	/**
	 * Queue of requests pending with the remote server
	 */
	private Queue<PendingPopulate> pendingPopulates;
	
	/**
	 * Create a new RemoteAlphabetManager which uses a given Model
	 * and LanguageModel to produce probabilities.
	 * 
	 * @param model Model which knows how to mangle probabilities
	 * @param langmodel LanguageModel which produces probabilities
	 */
	public CRemoteAlphabetManager(CDasherModel model, CLanguageModel langmodel) {
		super(model, langmodel);
		pendingPopulates = new LinkedList<PendingPopulate>();
		
		((CRemotePPM)m_LanguageModel).RegisterProbsListener(this);
		
	}

	/**
	 * Override of CAlphabetManager's PopulateChildrenWithSymbol method.
	 * <p>
	 * Requests that our remote language model should produce probabilities
	 * appropriate to this Node and returns without actually populating the
	 * Node's children.
	 * <p>
	 * The population itself will be performed when our asynchronous
	 * request completes; at this point, probsArrived() will be called.
	 * 
	 * @param Node Node whose children we are to populate
	 * @param iExistingSymbol Symbol of the Node's existing child, if any
	 * @param ExistingChild Pre-existing child of the Node we are to
	 * populate, if any. Null if none. 
	 */
	public void PopulateChildrenWithSymbol(CDasherNode Node, int iExistingSymbol, CDasherNode ExistingChild) {
		
		PendingPopulate pend = new PendingPopulate();
		
		pend.Node = Node;
		pend.iExistingSymbol = iExistingSymbol;
		pend.ExistingChild = ExistingChild;
		
		pendingPopulates.add(pend);
		
		// Ordinarily the call to the language model would be made after getting this
		// modified normalisation factor. I have altered the structure so that the
		// normalisation factor can be retrieved seperately.
		
		((CRemotePPM)m_LanguageModel).PromptForProbs(Node.Context(), m_Model.getNonUniformNorm((int)m_Model.GetLongParameter(Elp_parameters.LP_NORMALIZATION)));
		
	}
	
	/**
	 * Called by the LanguageModel when a request sent to the remote
	 * language modelling server responds to a request.
	 * <p>
	 * This will perform the actual population by calling the ordinary
	 * AlphabetManager's PopulateChildrenWithSymbol method.
	 * 
	 * @param probs Probabilities received
	 */
	public void probsArrived(long[] probs) {
		
		// System.out.printf("Probs arrived: %s%n", ShowArray(probs));
		
		m_Model.adjustProbs(probs);
		// Apply the model's tweaks to the retrieved probabilities.
		
		PendingPopulate nextPopulate = pendingPopulates.remove();
		
		super.PopulateChildrenWithSymbol(nextPopulate.Node, nextPopulate.iExistingSymbol, nextPopulate.ExistingChild, probs);
		
		m_Model.m_DasherInterface.Redraw(true);
		
	}
	
}

/**
 * Interface to be implemented by classes which wish to be
 * notified of the completion of asynchronous getProbs requests
 * by CRemotePPM.
 * <p>
 * To actually receive notifications, one must also register with
 * the LanguageModel using its RegisterProbsListener method.
 */
interface ProbsListener {
	
	public void probsArrived(long[] probs);
	
}
