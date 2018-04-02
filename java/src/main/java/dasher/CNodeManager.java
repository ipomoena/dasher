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

import java.util.ArrayList;

/**
 * A NodeManager is used by the DasherModel to perform
 * tasks which require some knowledge about the collection of
 * Nodes besides the Model's highly general view. 
 * This includes the handling of output when the
 * user enters or leaves a given node and extending the Model's
 * tree of DasherNodes, either forwards or backwards, whenever
 * necessary.
 */
public abstract class CNodeManager {
	
	/**
	 * Our unique ID
	 */
	protected int m_iID;
	
	/**
	 * Creates a new NodeManager with a given ID
	 * 
	 * @param iID ID to use
	 */
	public CNodeManager(int iID) {
		m_iID = iID;
	}
	
	/**
	 * Gets our unique ID
	 * 
	 * @return UID
	 */
	public int GetID() {
		return m_iID;
	}
	
	/**
	 * Increment this manager's reference count.
	 */
	public abstract void Ref();
	
	/**
	 * Decrement this manager's reference count. We can safely
	 * destroy any allocated resources when this reaches zero.
	 */
	public abstract void Unref();
	
	/**
	 * Produces a new root Node belonging to this NodeManager
	 * 
	 * @param Parent Parent to which the new root will be a child
	 * @param iLower New node's lower probability bound with respect to its parent
	 * @param iUpper New node's upper probability bound with respect to its parent
	 * @param UserData Usage depends on the type of NodeManager
	 * @return new root CDasherNode
	 */
	public abstract CDasherNode GetRoot(CDasherNode Parent, long iLower, long iUpper, int UserData); // VOID POINTER CHANGED TO INT 
	
	/**
	 * Fills this Node's child list.
	 * 
	 * @param Node Node to populate
	 */
	public abstract void PopulateChildren( CDasherNode Node );
	
	/**
	 * Removes all references to this node and deallocates storage
	 * if appropriate. Essentially we should do whatever is necessary
	 * to make this Node available for garbage collection.
	 * 
	 * @param Node Node to free
	 */
	public abstract void ClearNode(CDasherNode Node);
	
	/**
	 * Performs output appropriate to a given Node, if any.
	 * <p>
	 * This method will be called when a user enters a given Node.
	 * <p>
	 * This method is defined to do nothing in this class; if
     * a subclass does not wish to take action at this point, it
     * is safe to avoid overriding this.
	 * 
	 * @param Node Node to output
	 * @param Added CSymbolProbs detailing what has been output should
	 * be added to this list
	 * @param iNormalization Normalisation value (total to which
	 * node probabilities always add up)
	 */
	public void Output(CDasherNode Node, ArrayList<CSymbolProb> Added, int iNormalization)	{}
    
	/**
	 * Reverse or undo the output associated with a given Node
	 * <p>
	 * This method is defined to do nothing in this class; if
     * a subclass does not wish to take action at this point, it
     * is safe to avoid overriding this. 
	 * 
	 * @param Node Node to undo
	 */
	public void Undo( CDasherNode Node ) {}

    /**
     * Signals that the user has entered a given Node. Output
     * should not be performed at this stage.
     * <p>
     * This method is defined to do nothing in this class; if
     * a subclass does not wish to take action at this point, it
     * is safe to avoid overriding this.
     * 
     * @param Node
     */
	public void Enter(CDasherNode Node) {}
    
	/**
	 * Signals that the user has left a given Node. Output should
	 * not be performed at this stage.
	 * <p>
	 * This method is defined to do nothing in this class; if
     * a subclass does not wish to take action at this point, it
     * is safe to avoid overriding this.
	 * 
	 * @param Node Node to be left
	 */
    public void Leave(CDasherNode Node) {}

    /**
     * Rebuild's the parent of a given Node.
     * <p>
     * The new Node should have all of its children populated, one
     * of which should be the supplied Node.
     * <p>
     * See CAlphabetManager for a concrete example.
     * 
     * @param Node Node whose parent is to be rebuilt
     * @param iGeneration Tree depth of the node to be created
     * @return Parent of the input Node
     */
    public CDasherNode RebuildParent (CDasherNode Node, int iGeneration) {
      return null;
    }
}
