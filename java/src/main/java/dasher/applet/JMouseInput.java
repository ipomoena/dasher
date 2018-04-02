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

package dasher.applet;

import java.awt.event.*;

/**
 * Simple mouse input device which uses a mouse motion listener to
 * track the mouse position and reports the latest reading when
 * GetCoordinates is called.
 * <p>
 * Only methods which differ significantly from their abstract meanings
 * documented in CDasherInput are documented here; for further details
 * see CDasherInput.
 */
public class JMouseInput extends dasher.CDasherInput implements MouseMotionListener {

	/**
	 * Last seen mouse X co-ordinate
	 */
	private int mouseX;
	
	/**
	 * Last seen mouse Y co-ordinate
	 */
	private int mouseY;
	
	public JMouseInput(dasher.CEventHandler EventHandler, dasher.CSettingsStore SettingsStore) {
		super(EventHandler, SettingsStore, 0, 0, "Mouse Input"); 
	}

	
	public int GetCoordinateCount() {
	
		return 2;
		
	}

	/**
	 * Ignored
	 */
	public void mouseDragged(MouseEvent e) {
		// Do nothing		
	}

	/**
	 * Nasty hack alert! At present, this stores the read
	 * mouse co-ordinates less 10 in the X direction and 30 in the
	 * Y direction, since this is the border in the current setup
	 * of JDasher. This means that it will break if the Panel
	 * is moved elsewhere on the screen.
	 * <p>
	 * This needs to be generalised so that we offset the co-ordinates
	 * appropriately and respond to changes in the Screen's position.
	 */
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX() - 10;
		mouseY = e.getY() - 30;
		
		// Nasty hack whilst I figure out how to do this properly...
	}

	public int GetCoordinates(int iN, long[] Coordinates) {
		
		Coordinates[0] = mouseX;
		Coordinates[1] = mouseY;
		return 0;
		
	}
	
	
	
}
