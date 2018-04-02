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
 * Small struct representing a group of Nodes, as used to draw
 * coloured grouping boxes which surround a family of nodes, such
 * as punctuation.
 */
public class SGroupInfo {
	
	/**
	 * First child group
	 */
	SGroupInfo Child;
	
	/**
	 * Next sibling group
	 */
	SGroupInfo Next;
	
	/**
	 * Label for this group
	 */
	String strLabel;
	
	/**
	 * Index of the first node contained in this group
	 */
	int iStart;
	
	/**
	 * Index of the last node contained in this group
	 */
	int iEnd;
	
	/**
	 * Colour index used to draw this group
	 */
	int iColour;
	
	/**
	 * Should this group be drawn?
	 */
	boolean bVisible;
}