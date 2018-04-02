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
 * Event which signals that text has been entered or deleted.
 * <p>
 * An m_iEditType of 1 indicates new text, and 2 indicates deletion.
 */
public class CEditEvent extends CEvent {

	/**
	 * Creates a new EditEvent with a given type and text.
	 * <p>
	 * If deleting text, the content of m_sText is likely not
	 * to matter, though its length may be used in deciding how
	 * many characters to remove.
	 * 
	 * @param iEditType 1 for new text, 2 for deleted text
	 * @param sText Text to be added / deleted.
	 */
	public CEditEvent(int iEditType, String sText) {
	    m_iEventType = 2; // EV_EDIT
	    m_iEditType = iEditType;
	    m_sText = sText;
	};

	/**
	 * Edit event type
	 */
	public int m_iEditType;
	
	/**
	 * Text to add/remove
	 */
	public String m_sText;
	
}
