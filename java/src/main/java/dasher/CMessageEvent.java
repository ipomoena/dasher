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
 * Event carrying a message for the user. User interface components
 * should, if possible, display its contents to the user in some manner,
 * typically by showing a message box. 
 */
public class CMessageEvent extends CEvent {
	
	/**
	 * Creates a new message event
	 * 
	 * @param strMessage Message for the user
	 * @param iID This message's unique numerical identifier
	 * @param iType 0 for informative, 1 for warning, 2 for error
	 */
	public CMessageEvent(String strMessage, int iID, int iType) {
		m_iEventType = 8; // EV_MESSAGE
		m_strMessage = strMessage;
		m_iID = iID;
		m_iType = iType; // 0: information. 1: warning. 2: error.
	}

	/**
	 * Message body
	 */
	public String m_strMessage;

	/**
	 * Message UID
	 */
	public int m_iID;

	/**
	 * Message type
	 */
	public int m_iType;
}
