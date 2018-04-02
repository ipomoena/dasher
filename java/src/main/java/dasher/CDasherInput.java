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
 * DasherInput is the base class for all modules which provide
 * some sort of co-ordinate input.
 * <p>
 * To serve as a co-ordinate source, a device must be able to
 * provide synchronous or quasi-synchronous readout of its
 * current co-ordinates, such that a call to GetCoordinates
 * will return almost instantaneously.
 * <p>
 * Devices are free to return their co-ordinates relative
 * to the screen, using pixels as their unit of measure, or
 * relative to the Dasher world, using Dasher co-ordinates.
 * 
 * @see CDasherModule
 */
abstract public class CDasherInput extends CDasherModule {
	
	/**
	 * Sole constructor. Simply recalls CDasherModule's constructor.
	 * 
	 * @param EventHandler Event handler with which we wish to register ourselves
	 * @param SettingsStore Settings store for parameter getting/setting
	 * @param iID This device's ID
	 * @param iType This device's type number
	 * @param szName Friendly name for this device, to be used with GetModuleByName
	 */
	public CDasherInput(CEventHandler EventHandler, CSettingsStore SettingsStore, long iID, int iType, String szName) { 
		super(EventHandler, SettingsStore, iID, iType, szName);
	}
	
	/**
	 * Sets the maximum co-ordinates which the device should
	 * return. These are given as Dasher-world co-ordinates and
	 * should be translated if necessary.
	 * 
	 * @param iN Number of co-ordinates specified
	 * @param iDasherMax Maximum co-ordinates.
	 */
	public void SetMaxCoordinates(int iN, long[] iDasherMax) {}
	
	/**
	 * Called to request that the device report the current co-ordinates.
	 * 
	 * @param iN Number of co-ordinates expected.
	 * @param Coordinates Fill with the current co-ordinates
	 * @return 0 if those reported are screen co-ordinates, or 1 if they are Dasher world co-ordinates.
	 */
	public abstract int GetCoordinates(int iN, long[] Coordinates);
	
	/// Get the number of co-ordinates that this device supplies
	///

	/**
	 * Gets the number of co-ordinates this device returns.
	 * <p>
	 * At present, only 1- and 2-axis devices are accepted, but
	 * this may be easily upgraded in the future.
	 * 
	 * @return Number of co-ordinates supplied by this device.
	 */
	public abstract int GetCoordinateCount();
	
	/**
	 * Activates the input device. If threads must be started
	 * to support the device, this is the place to do it.
	 */
	public void Activate() {};
	
	/**
	 * Deactivates the device; resources should be freed at this point.
	 *
	 */
	public void Deactivate() {};
	
}
