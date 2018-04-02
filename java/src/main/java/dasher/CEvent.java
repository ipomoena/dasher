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
 * Base class representing a generic Event.
 * <p>
 * Generally this should not be instantiated and dispatched; rather
 * one should use a specialisation of this class with a non-zero
 * m_iEventType. As this will appear to have type 0, most components
 * will ignore it.
 * <p>
 * Currently assigned types:
 * <p>1: Parameter change (CParameterNotificationEvent)
 * <p>2: Edit event (CEditEvent)
 * <p>3: Edit context request (CEditContextEvent)
 * <p>4: Dasher Starting notification (CStartEvent)
 * <p>5: Dasher Stopping notification (CStopEvent)
 * <p>6: Control Mode related (CControlEvent)
 * <p>7: Lock event (locks/unlocks Dasher and may state
 * progress of the current operation) (CLockEvent)
 * <p>8: Message event -- typically interpreted
 * by showing the user a message box (CMessageEvent)
 */
public class CEvent {
	
	/**
	 * Event type. This is set by the constructor of various
	 * superclasses of Event.
	 */
	public int m_iEventType;
}
