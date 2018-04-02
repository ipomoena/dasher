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
 * Represents a custom colour scheme, wrapping a ColourInfo object
 * and importing its contents into Collections suitable for random
 * access.
 * <p>
 * This also ensures that the ColourInfo object is read-only
 * when in use.
 */
public class CCustomColours {
	
	/**
	 * Wrapped colour info object.
	 */
	protected CColourIO.ColourInfo m_ColourInfo;
	
	/**
	 * Fast-access array of available colours' red values.
	 */
	protected ArrayList<Integer> m_Red = new ArrayList<Integer>();
	
	/**
	 * Fast-access array of available colours' green values.
	 */
	protected ArrayList<Integer> m_Green = new ArrayList<Integer>();
	
	/**
	 * Fast-access array of available colours' blue values.
	 */
	protected ArrayList<Integer> m_Blue = new ArrayList<Integer>();

	/**
	 * Sole constructor; copies the data from a given ColourInfo
	 * object.
	 * 
	 * @param ColInfo ColourInfo object detailing the scheme we wish to represent.
	 */
	public CCustomColours(CColourIO.ColourInfo ColInfo) {
		m_ColourInfo = ColInfo;
		
		// Add all the colours.
		for(int i = 0; i < m_ColourInfo.Reds.size(); i++) {    // loop colours
			m_Red.add(m_ColourInfo.Reds.get(i));
			m_Green.add(m_ColourInfo.Greens.get(i));
			m_Blue.add(m_ColourInfo.Blues.get(i));
		}
	}
		
	/**
	 * Gets number of available colours.
	 * 
	 * @return Number of available colours.
	 */
	public int GetNumColours() {
		return m_Red.size();
	}
	
	/**
	 * Gets the red value of a given colour.
	 * 
	 * @param colour Index of the colour to look up.
	 * @return Red value, on a scale from 0 to 255.
	 */
	public int GetRed(int colour) {
		return m_Red.get(colour);
	}
	
	/**
	 * Gets the green value of a given colour.
	 * 
	 * @param colour Index of the colour to look up.
	 * @return green value, on a scale from 0 to 255.
	 */
	public int GetGreen(int colour) {
		return m_Green.get(colour);
	}
	
	/**
	 * Gets the blue value of a given colour.
	 * 
	 * @param colour Index of the colour to look up.
	 * @return blue value, on a scale from 0 to 255.
	 */
	public int GetBlue(int colour) {
		return m_Blue.get(colour);
	}
}
