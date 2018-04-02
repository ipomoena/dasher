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
 * A start handler is a helper to an input filter which serves the
 * purpose of starting and stopping Dasher.
 * <p>
 * At present they are only supported by CDefaultFilter.
 * <p>
 * Its Timer event will be called during its parent filter's
 * Timer event; similarly it will be given a chance to draw itself
 * in the DecorateView phase within its parent's DecorateView call. 
 */
public abstract class CStartHandler extends CDasherComponent {
	
	/**
	 * Interface which this Start Handler should control
	 */
	protected CDasherInterfaceBase m_Interface;
	
	/**
	 * Creates a new start handler which controls a given Interface
	 * 
	 * @param EventHandler Event handler with which to register ourselves
	 * @param SettingsStore Settings repository to use
	 * @param Interface Interface this Start Handler should control
	 */
	public CStartHandler(CEventHandler EventHandler, CSettingsStore SettingsStore, CDasherInterfaceBase Interface)  {
		super(EventHandler, SettingsStore);
		m_Interface = Interface;
	}
	
	/**
	 * Similar to its companion method in CInputFilter, this gives
	 * the start handler an opportunity to draw itself and other
	 * relevant decorations during the production of a new frame.
	 * <p>
	 * Start handlers should ensure that this method can reliably
	 * terminate quickly, or performance will be greatly degraded.
	 * 
	 * @param View View to which we should draw decorations
	 * @return True if any decorating was done, false otherwise
	 */
	public abstract boolean DecorateView(CDasherView View);
	
	/**
	 * Fired during the start handler's parent input filter's
	 * Timer event. This is when the start handler should interact
	 * with the Model if necessary. Despite the availability of a
	 * View, no drawing should be done at this stage; if this event
	 * makes drawing necessary it should be stored in the class'
	 * state for now and drawn at the next call to DecorateView.
	 * 
	 * @param iTime Current system time as a unix timestamp
	 * @param m_DasherView View which we may use for co-ordinate transforms and to get input co-ordinates
	 * @param m_DasherModel Model which may be modified if we wish
	 */
	public abstract void Timer(long iTime, CDasherView m_DasherView, CDasherModel m_DasherModel);

	/**
	 * Event handler
	 * 
	 * @see CEventHandler
	 */
	public abstract void HandleEvent(CEvent Event);
	
}
