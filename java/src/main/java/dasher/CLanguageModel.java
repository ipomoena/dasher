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

/* CSFS: WARNING: This is a DasherComponent derived class and so MUST
 * have UnregisterComponent called on destruction.
 */

/**
 * LanguageModels are responsible for predicting / guessing the probabilities
 * associated with members of a given alphabet in a given context.
 * <p>
 * They have no internal state except for knowledge of the
 * alphabet in which they make predictions; 
 * rather they produce, modify and destroy Context objects
 * (subclasses of CContextBase) which represent the context in which
 * they were working. This enables the impression of multiple
 * diverging models, when in fact the model is only capable of
 * answering one question: "What comes next in this context?"
 * <p>
 * Models can adapt themselves in response to user input, and should
 * do so if they are capable when LearnSymbol is called. Calls
 * to EnterSymbol on the other hand should modify their context
 * without altering the model.
 * 
 */
public abstract class CLanguageModel extends CDasherComponent {

	/**
	 * Value to be returned when we need a null context.
	 */
	public static final CContextBase nullContext = null;
	
	/**
	 * Alphabet in which we make predictions
	 */
	protected CSymbolAlphabet m_Alphabet;
	
	/**
	 * Creates a LanguageModel working in a given alphabet.
	 * 
	 * @param EventHandler Event handler with which to register ourselves
	 * @param SettingsStore Settings repository to use
	 * @param Alphabet Alphabet to work in
	 */
	
	public CLanguageModel(CEventHandler EventHandler, CSettingsStore SettingsStore, CSymbolAlphabet Alphabet) {
	  super(EventHandler, SettingsStore);
	  m_Alphabet = Alphabet;
	}

	/**
	 * Ignores all events
	 */
	public void HandleEvent(CEvent Event) {
		/* Ignores all events */
	}

	/**
	 * Indicates whether this is a remote/asynchronous model
	 * with respect to returning probabilities.
	 * 
	 * @return True if asynchronous, false if not
	 */
	public boolean isRemote() {
		return false;
	}
	
	/////////////////////////////////////////////////////////////////////////////
	// Context creation/destruction
	////////////////////////////////////////////////////////////////////////////

	/* CPPMLanguageModel and others: These were using a 
	 * horrible hack wherein an integer (really a CPPMContext * ) 
	 * was being used to represent the context of a given node when 
	 * outside the generating class, in order that it could be
	 * swapped out for some other CLanguageModel derivation and retain 
	 * type-compatibility at the expense of being entirely type-unsafe. 
	 * Since Java doesn't like to be type unsafe, I've replaced this by 
	 * all Context-representing classes being a child of CContextBase, which 
	 * has no members or methods, thus retaining type-safety. */
	
	// Create a context (empty)
	/**
	 * Creates an empty context
	 * 
	 * @return Child of CContextBase representing the empty context
	 */
	public abstract CContextBase CreateEmptyContext();
	
	/**
	 * Clones a given context; changes to one clone must not
	 * effect the state of the other.
	 * 
	 * @param Context Context to clone
	 * @return Clone of said context.
	 */
	public abstract CContextBase CloneContext(CContextBase Context);
	
	/**
	 * Releases a given context; if the LanguageModel is keeping
	 * track of them internally or doing allocation work, it should
	 * release their storage.
	 * 
	 * @param Context Context to release 
	 */
	public abstract void ReleaseContext(CContextBase Context);

	/////////////////////////////////////////////////////////////////////////////
	// Context modifiers
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Modifies the supplied context, appending a given symbol.
	 * <p>
	 * The model should not alter its predictions based upon this
	 * entry.
	 * @param context Context to modify
	 * @param Symbol Symbol to enter
	 */
	public abstract void EnterSymbol(CContextBase context, int Symbol);

	/**
	 * Modifies the supplied context, appending a given symbol.
	 * <p>
	 * The model should learn from this if it can in order to make
	 * better predictions in future.
	 * <p>
	 * If the model is not capable of learning, this should behave
	 * precisely as EnterSymbol.
	 * 
	 * @param context Context to modify
	 * @param Symbol Symbol to enter
	 */
	public abstract void LearnSymbol(CContextBase context, int Symbol);

	/////////////////////////////////////////////////////////////////////////////
	// Prediction
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Given a context and a normalisation value to which all
	 * probabilities must add up, this should produce an array
	 * in which each term corresponds to the relative probability
	 * of that symbol coming next.
	 * <p>
	 * The indices assigned to symbols should match those in the
	 * SymbolAlphabet we're working with.
	 * <p>
	 * The size of the array returned should be equal to the number of
	 * symbols in our current alphabet. 
	 */
	public abstract long[] GetProbs(CContextBase Context , long iNorm);

	/** Get some measure of the memory usage for diagnostic
	 * purposes. No need to implement this if you're not comparing
	 * language models. The exact meaning of the result will
	 * depend on the implementation (for example, could be the
	 * number of nodes in a trie, or the physical memory usage).
	 * 
	 * @return Memory usage in bytes, or 0 if unknown or we don't care
	 */
	public abstract int GetMemory();

	/**
	 * Gets our working alphabet
	 * 
	 * @return m_Alphabet
	 */
	public CSymbolAlphabet SymbolAlphabet() {
	    return m_Alphabet;
	}

	/**
	 * Gets the number of characters in our alphabet
	 * 
	 * @return m_Alphabet.size()
	 */
	protected int GetSize() {
	    return m_Alphabet.GetSize();
	}


	 

	
}
