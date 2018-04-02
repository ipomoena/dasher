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

package dasher.net;

/**
 * Dummy implementation of CDasherInput which provides input by
 * virtue of accepting co-ordinates from some external source
 * and reporting them when next GetCoordinates is called.
 * <p>
 * This is used by NetDasher to relay mouse co-ordinates extracted
 * from an XML document by another class. 
 */
public class NetInput extends dasher.CDasherInput {

	/**
	 * Cache of the most recently specified co-ordinates
	 * which will be returned next time GetCoordinates is called.
	 */
	private long[] nextCoords;
	
	/**
	 * Flag indicating whether new co-ordinates have been supplied
	 * since the last call to GetCoordinates.
	 */
	private boolean coordsAvailable;
	
	/**
	 * Creates a new input device; calls our super-constructor
	 * with an ID of 12 and a type of 1.
	 * 
	 * @param events EventHandler with which to mention ourselves
	 * @param sets Settings repository to use 
	 */
	public NetInput(dasher.CEventHandler events, dasher.CSettingsStore sets) {
		super(events, sets, 12, 1, "Network Input");
		nextCoords = new long[2];
	}

	public int GetCoordinateCount() {
		return 2;
	}

	/**
	 * Returns the last co-ordinates supplied by setNextCoords
	 * if coordsAvailable is true, or (0,0) otherwise.
	 * <p>
	 * Also sets coordsAvailable to false.
	 * 
	 * @return 0, since these are always screen co-ordinates.
	 */
	public int GetCoordinates(int iN, long[] Coordinates) {
		
		if(coordsAvailable) {
			Coordinates[0] = nextCoords[0];
			Coordinates[1] = nextCoords[1];
			
			coordsAvailable = false;
		}
		else {
			Coordinates[0] = Coordinates[1] = 0;
		}
		
		return 0;
	}
	
	/**
	 * Sets the next co-ordinates to be reported by GetCoordinates
	 * 
	 * @param x X co-ordinate to return
	 * @param y Y co-ordinate to return
	 */
	public void setNextCoords(long x, long y) {
		nextCoords[0] = x;
		nextCoords[1] = y;
		
		coordsAvailable = true;
	}

	
	
}
