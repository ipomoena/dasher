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
 * A Module is a richer extension of a Component. It supplies all
 * the facilities of a Component, including registration for event
 * listening and access to a settings store, and adds some module-specific
 * functions, including:
 * <p>
 * <ul><li>Naming. Modules each have a name which is decided when
 * the class is constructed, in order to facilitate GetModuleByName.
 * <li>Type and ID, to classify modules without necessarily knowing
 * their class.
 * <li>Reference counting. When the count reaches zero, the module
 * will automatically unregister itself from the EventHandler,
 * allowing the module to be garbage collected.
 */
public class CDasherModule extends CDasherComponent {
	
	/**
	 * This module's unique identifier
	 */
	private long m_iID;
	
	/**
	 * This module's type number
	 */
	private int m_iType;
	
	/**
	 * Current reference count for this module
	 */
	private int m_iRefCount;
	
	/**
	 * Module name
	 */
	private String m_szName;
	
	/**
	 * Creates a new module, passing the appropriate parameters
	 * to DasherComponent's constructor. In order for this module
	 * to be used by the interface it should be wrapped in a 
	 * CWrapperFactory and then registered with the ModuleManager.
	 * 
	 * @param EventHandler EventHandler with which this module
	 * should register itself
	 * @param SettingsStore SettingsStore to use
	 * @param iID Unique ID
	 * @param iType Type number
	 * @param szName Friendly, preferably unique, name
	 */
	public CDasherModule(CEventHandler EventHandler, CSettingsStore SettingsStore, long iID, int iType, String szName) {
		super(EventHandler, SettingsStore);
		
		m_iID = iID;
		m_iType = iType;
		m_iRefCount = 0;
		m_szName = szName;
	}
	
	/**
	 * Gets this module's unique ID
	 * 
	 * @return UID
	 */
	public long GetID() {
		return m_iID;
	}
	
	/**
	 * Gets this module's type ID
	 * 
	 * @return Type
	 */
	public int GetType() {
		return m_iType;
	}
	
	/**
	 * Gets this module's name
	 * 
	 * @return Name
	 */
	public String GetName() {
		return m_szName;
	}
	
	/**
	 * Increments this module's reference count
	 *
	 */
	public void Ref() {
		++m_iRefCount;
	}
	
	/**
	 * Decrements this module's reference count; if this becomes
	 * zero, unregisters the module with the event handler so
	 * that it can be garbage collected.
	 *
	 */
	public void Unref() {
		--m_iRefCount;
		
		/* CSFS: In the original C++, this read 'delete this'.
		 * Since in Java we can't do that, I just unregister the component
		 * with the manager (inherited method from CDasherComponent).
		 * Therefore we *must* drop the reference at the other end
		 * such that the module will be garbage collected.
		 */
		
		if(m_iRefCount == 0) { 
			this.UnregisterComponent();
		}
		
	}
	
}
