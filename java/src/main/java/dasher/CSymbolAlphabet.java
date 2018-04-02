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

/**
 * A symbol alphabet is a companion to an Alphabet which contains
 * the minimal information needed to perform language modelling
 * across said alphabet.
 * <p>
 * Essentially all it knows about is the size of the alphabet and the
 * indices of the space symbol.
 * <p>
 * Typically, a CAlphabet will be formed first, and a SymbolAlphabet
 * produced afterwards prior to creating a LanguageModel.
 */
public class CSymbolAlphabet {

	/**
	 * Size of this alphabet
	 */
	protected int m_iSize;
	
	/**
	 * Index of the space symbol (needed to allow the language
	 * model to establish where words end)
	 */
    protected int m_SpaceSymbol;
    
    /**
     * Companion alphabet
     */
    protected CAlphabet AlphabetPointer;
	
	/////////////////////////////////////////////////////////////////////////////
	    // iSize is the number of Symbols - please dont change the constructor
	    // Add functions to set customizable behaviour

    /**
     * Creates a new Alphabet with a given size and a space
     * symbol of -1.
     * 
     * @param iSize Alphabet size
     */
    public CSymbolAlphabet(int iSize) {
	    m_iSize = iSize;
	    SetSpaceSymbol(-1);       // Default (uniformative) value.
	    } 
	
    /**
     * Gets the size of this alphabet
     * 
     * @return Size
     */
    public int GetSize() {
	      return m_iSize;
	}
	
    /**
     * Sets the index of the space symbol
     * 
     * @param _SpaceSymbol Space symbol index
     */
    public void SetSpaceSymbol(int _SpaceSymbol) {
	     m_SpaceSymbol = _SpaceSymbol;
	} 
	
    /**
     * Gets the index of the space symbol
     * 
     * @return Space symbol index
     */
    public int GetSpaceSymbol() {
		// Get the space symbol. -1 means that no symbol has been set.
		return m_SpaceSymbol;
	} 
	
	/**
	 * Sets our companion alphabet
	 * 
	 * @param _AlphabetPointer New alphabet
	 */
    public void SetAlphabetPointer(CAlphabet _AlphabetPointer) {
		
		// NOTE - you are VERY strongly discouraged from
		// using this function. It is only here as a hack
		// until I figure out a better solution.
		
		AlphabetPointer = _AlphabetPointer;
	}
	
	/**
	 * Gets our companion alphabet
	 * 
	 * @return Companion alphabet
	 */
    CAlphabet GetAlphabetPointer() {
		
		// NOTE - you are VERY strongly discouraged from
		// using this function. It is only here as a hack
		// until I figure out a better solution.
		
		return AlphabetPointer;
	} 
	
}