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

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/**
 * Helper class for the InterfaceBase which enumerates the modules
 * present in supplied module factories and returns said modules
 * by ID or by name on request.
 * <p>
 * A list of all available modules can also be supplied.
 */
public class CModuleManager {
	
	/**
	 * Map from modules' IDs to the modules themselves
	 */
	protected HashMap<Long, CModuleFactory> m_mapModules = new HashMap<Long, CModuleFactory>();
	
	/**
	 * Enumerates all modules provided by a given factory and stores
	 * them in our internal map.
	 * <p>
	 * These modules can henceforth be retrieved using GetModuleByName
	 * or GetModule.
	 * 
	 * @param Factory Factory to search for new modules
	 */
	public void RegisterFactory(CModuleFactory Factory) {
		Factory.Start();
		SFactoryInfo info = new SFactoryInfo();
		while(Factory.IsMore()) {
		
			/* CSFS: This wants to update three variables, so I have altered the call
			 * to return a small struct
			 */
			
			
			info = Factory.GetNext(info);
			
			m_mapModules.put(info.iID, Factory);
		}
	}
	
	/**
	 * Gets a module with the given identifier
	 * 
	 * @param iID ID of the required module
	 * @return Matching Module, or null if none was found.
	 */
	public CDasherModule GetModule(long iID) {
		// TODO: Error checking here
		
		return m_mapModules.get(iID).GetModule(iID);
	}
	
	/**
	 * Retrieves a module by name. The module must previously
	 * have been registered using RegisterFactory.
	 * 
	 * @param strName Name of the requested module
	 * @return Module with this name, or null if there is none.
	 */
	public CDasherModule GetModuleByName(String strName) {
		
		for(Map.Entry<Long, CModuleFactory> s : m_mapModules.entrySet()) {
			if (s.getValue().GetName(s.getKey()).equals(strName)) {
				return s.getValue().GetModule(s.getKey());
			}
		}
		
		return null;
	}
	
	/**
	 * Retrieves a list of all modules with a given type.
	 * <p>
	 * Available types:
	 * <p>
	 * 0 - Input device
	 * <p>
	 * 1 - Input filter
	 *  
	 * @param iType Type of modules to enumerate
	 * @param vList Collection to be filled with the names of available
	 * modules
	 */
	public void ListModules(int iType, Collection<String> vList) {
		for(Map.Entry<Long, CModuleFactory> s : m_mapModules.entrySet()) {
			if(s.getValue().GetType(s.getKey()) == iType) {
				vList.add(s.getValue().GetName(s.getKey()));
			}
		}
	}
	

}
