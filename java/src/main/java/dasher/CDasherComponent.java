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
 * DasherComponent is the base class of most functional units of
 * Dasher, and serves to provide a number of common functions and
 * services to its children. These include:
 * <p><ul><li>The ability to register oneself as an event listener
 * and so receive notification when other components dispatch events.
 * <li>The ability to dispatch events, of which all other Components will
 * be notified.
 * <li>The ability to get and set global settings, using some child of CSettingsStore.
 * </ul>
 * <p>
 * Generally speaking, a single EventHandler and SettingsStore should be
 * created during Dasher's initialisation, and a reference to both
 * passed to each created DasherComponent. However, there is no
 * reason why in future there could not be multiple subgroups
 * of components which are not connected, and by virtue of
 * having seperate EventHandlers and SettingsStores would not
 * hear each other's events, or be effected by each other's parameter changes.
 * <p>
 * It is, however, not recommended that any two components should
 * share a SettingsStore without also sharing an EventHandler,
 * as the SettingsStore raises an Event to notify whenever a parameter
 * is changed, a behaviour which components may depend upon.
 */
public class CDasherComponent {

	/**
	 * This Component's EventHandler
	 */
	protected CEventHandler m_EventHandler;
	
	/**
	 * This Component's SettingsStore
	 */
	protected CSettingsStore m_SettingsStore;
	
	/**
	 * Sole constructor. Registers this component as an event listener,
	 * such that HandleEvent will be called whenever any other componenet
	 * raises an event.
	 * 
	 * @param EventHandler Event handler with which to register. Must not be null.
	 * @param SettingsStore Settings store to be used to get and set parameters. Must not be null.
	 */
	public CDasherComponent(CEventHandler EventHandler, CSettingsStore SettingsStore) {
	  m_EventHandler = EventHandler;
	  m_SettingsStore = SettingsStore;

	  if (m_EventHandler != null)
	    m_EventHandler.RegisterListener(this);

	}
	
	/**
	 * Unregisters this component with the EventHandler, preventing
	 * the receipt of any further events, though new ones can still
	 * be dispatched. Typically this is called on destruction of the object.
	 *
	 */
	public void UnregisterComponent() {
		if (m_EventHandler != null)
		    m_EventHandler.UnregisterListener(this);
	}
	
	/**
	 * Dispatches a new event, which will be passed to the HandleEvent of all
	 * other components registered with the EventHandler.
	 * 
	 * @param Event Event we wish to pass to other components.
	 */
	public void InsertEvent(CEvent Event) {
		  m_EventHandler.InsertEvent(Event);
	}

	/**
	 * Called by the EventHandler to notify this component of an
	 * event raised by another. Modifications of the Event object
	 * will be seen by subsequent listeners and by the originator.
	 * No garuntees are made as to the order in which components
	 * will receive events.
	 * 
	 * @param pEvent Event received.
	 */
	public void HandleEvent(CEvent pEvent) {}
	
	/**
	 * Retreives the value of a given global boolean parameter.
	 * <p>This request is actioned by calling the same method on m_SettingsStore
	 * 
	 * @param iParameter Parameter to retrieve
	 * @return Boolean value of this parameter
	 */
	public boolean GetBoolParameter(Ebp_parameters iParameter) {
		return m_SettingsStore.GetBoolParameter(iParameter);
	}
	
	/**
	 * Retreives the value of a given global long parameter.
	 * <p>This request is actioned by calling the same method on m_SettingsStore
	 * 
	 * @param iParameter Parameter to retrieve
	 * @return Long value of this parameter
	 */
	public long GetLongParameter(Elp_parameters iParameter) {
		  return m_SettingsStore.GetLongParameter(iParameter);
	}
	
	/**
	 * Retreives the value of a given global string parameter.
	 * <p>This request is actioned by calling the same method on m_SettingsStore
	 * 
	 * @param iParameter Parameter to retrieve
	 * @return String value of this parameter
	 */
	public String GetStringParameter(Esp_parameters iParameter) {
		  return m_SettingsStore.GetStringParameter(iParameter);
	}
	
	/**
	 * Sets the value of a given boolean parameter by calling the
	 * SettingsStore's SetBoolParameter method.
	 * 
	 * @param iParameter Parameter to set
	 * @param bValue New value for this parameter
	 */
	public void SetBoolParameter(Ebp_parameters iParameter, boolean bValue) {
		  m_SettingsStore.SetBoolParameter(iParameter, bValue);
	}
	
	/**
	 * Sets the value of a given long parameter by calling the
	 * SettingsStore's SetBoolParameter method.
	 * 
	 * @param iParameter Parameter to set
	 * @param lValue New value for this parameter
	 */
	public void SetLongParameter(Elp_parameters iParameter, long lValue) {
		  m_SettingsStore.SetLongParameter(iParameter, lValue);
	}
	
	/**
	 * Sets the value of a given string parameter by calling the
	 * SettingsStore's SetBoolParameter method.
	 * 
	 * @param iParameter Parameter to set
	 * @param sValue New value for this parameter
	 */
	public void SetStringParameter(Esp_parameters iParameter, String sValue) {
		  m_SettingsStore.SetStringParameter(iParameter, sValue);
	}

	/**
	 * Determines the type of some parameter; this is exactly
	 * the same as checking whether it is an instance of
	 * Ebp_parameters (boolean), Elp_parameters (long) or Esp_parameters (string).
	 */
	public int GetParameterType(EParameters iParameter) {
		  if (m_SettingsStore != null)
		    return m_SettingsStore.GetParameterType(iParameter);
		  return 3; //
	}
	
	/**
	 * Retrieves the friendly name of a given parameter.
	 * <p>Useful for dumping a complete human-readable configuration.
	 * 
	 * @param iParameter Parameter we wish to identify
	 * @return Friendly name for this parameter
	 */
	public String GetParameterName(EParameters iParameter) {
		  if (m_SettingsStore != null)
		    return m_SettingsStore.GetParameterName(iParameter);
		  return "";
	}
	
}
