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
 * Event which can be loosely interpreted to mean "I want to know
 * what the context is". It will be issued by the Interface when
 * the internal context buffer is empty, or the current context is
 * most likely no longer valid, but a UI component may know what
 * the context ought to be.
 * <p>
 * Components should respond by setting the newContext property.
 * When first dispatched this property is empty, so components
 * should be wary of overwriting another's answer unless they are
 * sure they have a better context.
 * <p>
 * The MaxLength property specifies how much context the Interface
 * is asking for; longer strings should not be supplied.
 */
public class CEditContextEvent extends CEvent {

	/**
	 * Creates a new event with type 3 (EV_EDIT_CONTEXT) and a
	 * specified maxLength.
	 * 
	 * @param iMaxLength Maximum length of context desired
	 */
	public CEditContextEvent(int iMaxLength) {
		m_iEventType = 3; //EV_EDIT_CONTEXT
		m_iMaxLength = iMaxLength;
	}
	/**
	 * Maximum length of context desired
	 */
	public int m_iMaxLength;
	
	/**
	 * Empty string, to be replaced with a context if one is known
	 */
	public String newContext = "";

	
}
