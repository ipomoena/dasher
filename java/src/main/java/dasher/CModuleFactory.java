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
 * A ModuleFactory is a factory capable of yielding one or more
 * distinct CDasherModules, each associated with a friendly name
 * and a unique identifier.
 */
public abstract class CModuleFactory {

	/**
	 * Called when a factory is to be registered with a ModuleManager
	 * and will shortly be asked for its modules. Any work involved
	 * in creating its modules or setting up its internal state
	 * should be done now.
	 *
	 */
	public abstract void Start();
	
	/**
	 * Are more modules available beyond those already yielded?
	 * 
	 * @return True if so, false otherwise
	 */
	public abstract boolean IsMore();
	
	/**
	 * Given information about a previous module, returns a struct
	 * describing the next module.
	 * <p>
	 * In order to retrieve the first factory, a new/blank SFactoryInfo
	 * should be passed in.
	 * <p>
	 * Null should never be passed as a parameter.
	 * 
	 * @param info SFactoryInfo describing the previous module or, to retrieve the first module, a blank SFactoryInfo. 
	 * @return SFactoryInfo describing the next module
	 */
	public abstract SFactoryInfo GetNext(SFactoryInfo info);
	
	/**
	 * Retrieves the module associated with a given unique identifier
	 *  
	 * @param iID UID of the required module
	 * @return Corresponding module, or null if no module belonging
	 * to this factory has this ID.
	 */
	public abstract CDasherModule GetModule(long iID);
	
	/**
	 * Gets the name of a module by UID.
	 * 
	 * @param iID UID of the requested module
	 * @return Name of the requested module, or "Not Implemented" if there
	 * is no such module
	 */
	public abstract String GetName(long iID);
	
	/**
	 * Gets the type of a given module by UID.
	 * 
	 * @param iID UID of the requested module
	 * @return Type of the given module, or 0 if there is no such module
	 */
	public abstract int GetType(long iID);
	
}
