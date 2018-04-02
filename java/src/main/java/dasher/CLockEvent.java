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
 * Event which cues the Interface to lock or unlock Dasher, and may
 * optionally give a completion perrcentage of an event necessitating
 * the lock's engagement.
 * <p>
 * The interface will always honour a cue to lock Dasher; user
 * interface components should, if possible, inform the user that
 * the lock is engaged, and, again if possible, give some indication
 * of the percent completion.
 * <p>
 * If raising lock events relating to a monolithic operation, or
 * an operation whose duration is unknown, zero percent completion
 * should be indicated until finished.  
 */
public class CLockEvent extends CEvent {

	/**
	 * Creates a new Lock event
	 * 
	 * @param strMessage Message to show the user during lock
	 * @param bLock Whether the lock should currently be engaged
	 * @param iPercent Percentage completion
	 */
	public CLockEvent(String strMessage, boolean bLock, int iPercent) {
		m_iEventType = 7; // EV_LOCK
		m_strMessage = strMessage;
		m_bLock = bLock;
		m_iPercent = iPercent;
	}
	  
	/**
	 * Message intended for the user
	 */
	public String m_strMessage;
	
	/**
	 * Lock engaged
	 */
	public boolean m_bLock;
	
	/**
	 * Percent completion
	 */
	public int m_iPercent;
}
