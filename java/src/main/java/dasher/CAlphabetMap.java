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

import java.util.HashMap;

/**
 * Simple map from the textual representation of alphabet symbols
 * to their symbol numbers. This used to be a lot more complicated
 * but now essentially just wraps a Java HashMap and could easily
 * be removed and replaced by a HashMap member of Alphabet.
 */
public class CAlphabetMap {

	/* CSFS: Reimplemented this class, since it was a pain to get working
	 * in the same way as the C++ version, plus the original code actually
	 * included a brief essay at the beginning warning against its being
	 * suboptimal, and advising us to use a proper hash table.
	 */
	
	/**
	 * The map itself, from Strings to SSymbols (a small class
	 * giving its integer index and a boolean value
	 * which determines whether it constitutes a prefix symbol.
	 * This is always set to false by the constructor, so the
	 * class too is redundant and should be replaced by a simple
	 * Integer.
	 * 
	 * @see SSymbol
	 */
	protected HashMap<String, SSymbol> hashTable;
	// maps string representations to symbol indexes
	
	/**
	 * Constant used to represent any string which does not
	 * correspond to a valid symbol.
	 */
	private final int Undefined = 0;
	
	/**
	 * Creates the Hash Table with a given initial size.
	 * 
	 * @param InitialTableSize Initial size
	 */
	public CAlphabetMap(int InitialTableSize)	{
		hashTable = new HashMap<String, SSymbol>(InitialTableSize);
	}
	
	/**
	 * Default constructor; creates the hash table with a size
	 * of 255.
	 */
	public CAlphabetMap() {
		this(255); // Default value from alphabet_map.h
	}
	
	/* CSFS: It looks to me as if even in the original C++,
	 * nothing ever gets to use the KeyIsPrefix flag -- there are
	 * actions to be taken if it's true, but nothing ever sets it so.
	 * Consider removing and reverting to a plain integer to represent
	 * a symbol?
	 */
	
	/**
	 * Adds a new key/value pair to the table.
	 * 
	 * @param Key String representation of a symbol
	 * @param Value Integer index of same symbol
	 */
	public void Add(String Key, int Value) {
		SSymbol newsym = new SSymbol();
		newsym.prefix = false;
		newsym.symbol = Value;
		hashTable.put(Key, newsym);
	}
	
	/**
	 * Retrieves a symbol index given the string representation.
	 * 
	 * @param Key String to look up
	 * @return SSymbol containing the integer index of the symbol, or Undefined if the supplied String did not correspond to any symbol.
	 */	
	public SSymbol Get(String Key) {
		
		if(hashTable.containsKey(Key)) {
			return hashTable.get(Key);
		}
		else {
			SSymbol retval = new SSymbol();
			retval.symbol = Undefined;
			retval.prefix = false;
			return retval;
		
		}
	}
}
