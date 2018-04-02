
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

import javax.swing.JApplet;

/**
 * TimerTask which orders a specified applet to redraw every time
 * it is run, the idea being that the DasherScreen Panel will be
 * ordered to redraw and thus produce a new frame. This differs
 * from Dasher's traditional methods in which drawing is triggered
 * at the bottom level; here the Applet's redraw triggers the generation
 * of a new frame.
 */
public class DoFrameTask extends java.util.TimerTask {

	/**
	 * Applet to repaint.
	 */
	private JApplet m_Applet;

	/**
	 * Creates a new task to redraw a specified applet.
	 * 
	 * @param iApplet Applet to redraw
	 */
	public DoFrameTask(JApplet iApplet) {
		m_Applet = iApplet;
	}
	
	/**
	 * Redraws, incrementing the FramesInQueue counter beforehand
	 * and decrementing afterwards. If FramesInQueue is non-zero
	 * when we are called, a frame is skipped, both to avoid
	 * tasks stacking up, and to prevent the confusion involved
	 * if two frames are in progress simeltaneously.
	 */
	public void run() {
		
		m_Applet.repaint();
			
	}
	
}
