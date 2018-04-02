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
 * Input filters are a key part of the Dasher infrastructure.
 * <p>
 * They are responsible for taking raw input co-ordinates and
 * modifying the DasherModel in some way if appropriate.
 * <p>
 * They are also notified of all key presses and are given a chance
 * to apply graphical decorations towards the end of the drawing
 * cycle.
 * <p>
 * The most important method is Timer, which will be fired at the
 * beginning of each new frame. This is responsible for obtaining
 * input co-ordinates from the View, typically using the GetCoordinates
 * method, and interacting with the model in some way, typically
 * by calling Tap_on_display.
 * <p>
 * Timer will always be called every frame, regardless of the filter
 * in use, except for when Dasher is paused or locked.
 * <p>
 * Being Modules, InputFilters have an Activate and Deactivate
 * method which is intended to allow the allocation and deallocation
 * of temporary helper objects, helper threads, network connections
 * and so forth.
 * <p>
 * It is very important that the Timer method should return as near
 * instantaneously as possible, as this will be called tens of times
 * per second.
 * <p>
 * Input filters may also interact with the main Dasher interface,
 * typically to start and stop Dasher in response to key presses.
 */
public class CInputFilter extends CDasherModule {
	
	/**
	 * Interface which this InputFilter may control
	 */	
	protected CDasherInterfaceBase m_Interface;
	
	/**
	 * Creates a new filter. See DasherModule for information
	 * on registering this with the Interface as a usable
	 * module.
	 * 
	 * @param EventHandler Event handler with which we should register ourselves
	 * @param SettingsStore Settings repository to use
	 * @param Interface Interface which this filter may control
	 * @param iID Unique identifier for this Module
	 * @param iType Module type (see DasherModule for a list)
	 * @param szName Friendly name (ideally unique)
	 * @see CDasherModule
	 */
	public CInputFilter(CEventHandler EventHandler, CSettingsStore SettingsStore, CDasherInterfaceBase Interface, long iID, int iType, String szName)
    { 
      	super(EventHandler, SettingsStore, iID, iType, szName);
      	m_Interface = Interface;
    }
	
	/**
	 * Ignores all events; to be overridden by subclasses if desired.
	 */
	public void HandleEvent(CEvent Event) {};
	
	/**
	 * Should draw any decorations applicable to this filter.
	 * <p>
	 * For example, DefaultFilter might draw a line indicating
	 * the mouse position at this stage.
	 * <p>
	 * If we wish to allow any helper classes to do their own
	 * decorating, we should pass the View to them at this stage.
	 * 
	 * @param View View to which we should draw our decorations.
	 * @return True if anything has been drawn, false otherwise.
	 */
	public boolean DecorateView(CDasherView View) { return false; };
	
	/**
	 * Notifies the filter of a key-down event at a given time,
	 * and allows it to make changes to the Model but not the View
	 * in response to this change. If we wish to display things
	 * in response to a key event, we must store state in the meantime
	 * and wait for the next frame. 
	 * 
	 * @param Time System time at which the key event took place, as a unix timestamp.
	 * @param iId ID of the key pressed (see DasherInterfaceBase.KeyDown for a list)
	 * @param Model DasherModel which may be modified in response to this keypress
	 */
	public void KeyDown(long Time, int iId, CDasherModel Model) {};
	
	/**
	 * Notifies the filter of a key-up event at a given time,
	 * and allows it to make changes to the Model but not the View
	 * in response to this change. If we wish to display things
	 * in response to a key event, we must store state in the meantime
	 * and wait for the next frame. 
	 * 
	 * @param Time System time at which the key event took place, as a unix timestamp.
	 * @param iId ID of the key pressed (see DasherInterfaceBase.KeyDown for a list)
	 * @param Model DasherModel which may be modified in response to this keypress
	 */
	public void KeyUp(long Time, int iId, CDasherModel Model) {};
	
	/**
	 * Requests that the input filter should modify the indicated Model.
	 * The View is passed so that input co-ordinates can be resolved
	 * to Dasher co-ordinates prior to interacting with the Model.
	 * <p>
	 * Nothing should be drawn at this stage in spite of the availability
	 * of the View; drawing should take place during the call to DecorateView.
	 * <p>
	 * See DefaultFilter for a typical example of a Timer method.
	 * 
	 * @param Time System time as a unix timestamp
	 * @param m_DasherView View against which co-ordinates should be resolved
	 * @param m_DasherModel Model which we may manipulate in response to this event
	 * @return True if the model was changed, false otherwise.
	 */
	public boolean Timer(long Time, CDasherView m_DasherView, CDasherModel m_DasherModel) { return false; };
	
	/**
	 * Activates this filter; now is the time to start helper threads,
	 * allocate resources, and so forth.
	 *
	 */
	public void Activate() {};
	
	/**
	 * Deactivates this filter; threads should be stopped and resources
	 * which can be freed should be.
	 *
	 */
	public void Deactivate() {};
	
	
}
