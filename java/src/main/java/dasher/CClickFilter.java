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
 * This is an InputFilter implementation which accepts mouse clicks
 * and causes Dasher to zoom to the location of successive clicks.
 * <p>
 * The filter does not pay any attention to the mouse position
 * except for when the user clicks the display, and does not
 * decorate the display in any way.
 * <p>
 * In order to zoom smoothly to a given location, it invokes
 * CDasherModel.ScheduleZoom, which interpolates a number of points
 * between the current crosshair location and the point clicked by
 * the user, and jumps to these points on each successive frame.
 * <p>
 * This filter registers itself with the name <i>Click Mode</i>.
 */

public class CClickFilter extends CInputFilter {

	/**
	 * Current DasherView, to be used in determining mouse co-ordinates.
	 */
	private CDasherView DasherView;
	
	/**
	 * Sole constructor. Calls the CInputFilter constructor with a type of 7,
	 * an ID of 1, and the name <i>Click Mode</i>.
	 * 
	 * @param EventHandler Event handler.
	 * @param SettingsStore Settings repository.
	 * @param Interface Interface with which the filter should be registered.
	 */
	public CClickFilter(CEventHandler EventHandler, CSettingsStore SettingsStore, CDasherInterfaceBase Interface) {
	  super(EventHandler, SettingsStore, Interface, 7, 1, "Click Mode");
	  DasherView = null;
	}

	/**
	 * This filter does not decorate the view.
	 * 
	 * @param View Unused, may be null.
	 * @return False, indicating no work done.
	 */
	public boolean DecorateView(CDasherView View) {
	  return false;
	}

	/**
	 * Timer simply calls the DasherModel's Tap_on_display method,
	 * causing it to move forward a frame if there is currently
	 * a zoom scheduled. In the event that no further destination
	 * is scheduled (ie. we are stationary and the user has not
	 * clicked a new destination), nothing is done.
	 * 
	 * @param Time Current system time as a Unix timestamp
	 * @param m_DasherView View to be used for co-ordinate transforms
	 * @param m_DasherModel Model which will be instructed to advance a frame
	 * @return True if the model has changed, false otherwise. 
	 */
	public boolean Timer(long Time, CDasherView m_DasherView, CDasherModel m_DasherModel) {
	  // FIXME - REALLY, REALLY horrible - bleh
	  DasherView = m_DasherView;
	  boolean retval = m_DasherModel.Tap_on_display(0, 0, Time, null);
	  if(m_DasherModel.ScheduledSteps() == 0) {
		  while(m_DasherModel.CheckForNewRoot(DasherView)) {
			  // Do nothing. This allows the root to get reparented as many times as
			  // are necessary before we pause.
		  }
		  
		  SetBoolParameter(Ebp_parameters.BP_DASHER_PAUSED, true);
	  }
	  return retval;
	}

	/**
	 * KeyDown is to be called by the Interface when the user
	 * presses a key or clicks the mouse. ClickFilter responds to:
	 * 
	 * <b>Left mouse button</b>: Schedules a zoom to the clicked location.
	 * 
	 * @param iTime Current system time as a UNIX timestamp.
	 * @param iId Key/button identifier.
	 * @param Model DasherModel which should be zoomed in response to clicks.
	 */
	public void KeyDown(long iTime, int iId, CDasherModel Model) {

	  switch(iId) {
	  case 100: // Mouse clicks
	    if(DasherView != null) {
	      SetBoolParameter(Ebp_parameters.BP_DASHER_PAUSED, false);
	    	
	      CDasherView.DPoint retval = DasherView.getInputDasherCoords();
	      Model.ScheduleZoom(retval.x, retval.y);
	    }
	    break;
	  }
	}

	/**
	 * This class ignores all events.
	 * 
	 * @param Event Event to be processed.
	 */
	public void HandleEvent(CEvent Event) {
	}
	
}
