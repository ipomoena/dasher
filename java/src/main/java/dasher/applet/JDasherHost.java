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

/** Capabilities required of anything which contains a Dasher control.
 * This provides decoupling between the Applet and the core, so that Dasher
 * can be integrated into any Java app.
 */
public interface JDasherHost {

	/**
	 * Should trigger a redraw of Dasher by any means necessary.
	 * <p>
	 * This may mean simply calling Dasher.Draw if no user interface
	 * setup is necessary.
	 * <p>
	 * Under Swing this typically means starting a top-level repaint.
	 *
	 */
	public void Redraw();
	
	/**
	 * Register ourselves to receive mouse motion events from the host.
	 * <p>
	 * The host should either register the submitted object as a MouseMotionListener
	 * or supply its own events by some other means.
	 * 
	 * @param e Listener to add
	 */
	public void regMouseMotionListener(java.awt.event.MouseMotionListener e);
	
	/**
	 * Passes an event to the host; the host need not do anything
	 * if it doesn't want to.
	 * <p>
	 * At present, all events are passed up.
	 * 
	 * @param event Event being passed
	 */
	public void handleEvent(dasher.CEvent event);
	
}
