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
 * A simple ModuleFactory which simply wraps a pre-existing DasherModule
 * and returns it as the sole module it can produce.
 */
public class CWrapperFactory extends CModuleFactory {
	
	/**
	 * Module wrapped by this factory
	 */
	protected CDasherModule m_Module;
	
	/**
	 * Index of the next module to retrieve
	 */
	protected int m_iCurrentPos;
	
	/**
	 * Creates a new WrapperFactory wrapping a specified Module.
	 *  
	 * @param EventHandler EventHandler with which to register ourselves
	 * @param SettingsStore Settings repository to use
	 * @param Module Module to wrap
	 */
	public CWrapperFactory(CEventHandler EventHandler, CSettingsStore SettingsStore, CDasherModule Module) {
		m_Module = Module;
		m_Module.Ref();
		
		m_iCurrentPos = 0;
	}
	
	/**
	 * Unref's our module and deletes its reference.
	 *
	 */
	public void DestroyFactory() {
		m_Module.Unref();
		m_Module = null;
	}
	
	/**
	 * Sets m_iCurrentPost to 0
	 */
	public void Start() {
		m_iCurrentPos = 0;
	}
	
	/**
	 * Returns true if no Modules have been retrieved yet
	 */
	public boolean IsMore() {
		return (m_iCurrentPos < 1);
	}
	
	/**
	 * Returns the details of our module with the name "Unknown Module"
	 * if this is the first call made to this method, or a copy of
	 * the SFactoryInfo fed in if not.
	 * 
	 * @param info Previous module details
	 * @return Next module details
	 */
	public SFactoryInfo GetNext(SFactoryInfo info) {
		
		/* CSFS: BUGFIX: This was returning its retval within the if block, which isn't correct */
		
		SFactoryInfo retval = new SFactoryInfo();
		retval.iID = info.iID;
		retval.iType = info.iType;
		retval.strName = info.strName;
		if(m_iCurrentPos == 0) {
			
			retval.iID = m_Module.GetID();
			retval.iType = m_Module.GetType();
			retval.strName = "Unknown Module";
		}
		++m_iCurrentPos;
		return retval;
		
	}
	
	/**
	 * Returns our wrapped module if the ID is correct,
	 * or null otherwise.
	 */
	public CDasherModule GetModule(long iID) {
		if(iID == m_Module.GetID())
			return m_Module;
		else
			return null;
	}
	
	/**
	 * Returns our wrapped module's name if the ID is correct,
	 * or "Not implemented" otherwise.
	 */
	public String GetName(long iID) {
		if(iID == m_Module.GetID())
			return m_Module.GetName();
			else
				return "Not implemented"; // TODO: Is this the right thing to do here?
	}
	
	/**
	 * Returns our wrapped module's type if the ID is correct,
	 * or 0 otherwise.
	 */
	public int GetType(long iID) {
		if(iID == m_Module.GetID())
			return m_Module.GetType();
			else
				return 0; // TODO: Is this the right thing to do here?
	}
}
