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

package dasher.applet;

import java.security.AccessControlException;
import java.util.prefs.Preferences;

import dasher.CEventHandler;
import dasher.CParameterNotFoundException;
import dasher.CSettingsStore;

/**
 * Extension of CSettingsStore which uses a Java Preferences
 * object to store settings between sessions.
 * <p>
 * This boils down to using the registry in Windows, and a hidden
 * file in the user's profile on Unix-style systems including
 * MacOSX.
 * <p>
 * In the current unsigned applet setup this will not get used,
 * as a security exception is thrown on trying to instantiate
 * Preferences and a plain old CSettingsStore will be used in lieu.
 * <p>
 * Should JDasher ever get converted to a standalone Java application
 * or signed applet, however, this will spring into life and provide
 * persistent settings.
 * <p>
 * I have not documented these functions, as they all implement
 * precisely the behaviour specified in CSettingsStore.
 * <p>
 * The only noteworthy feature is that the Constructor will throw
 * a StoreUnavailableException if unable to open the registry
 * and/or user profile.
 */
public class JSettings extends CSettingsStore {

	private Preferences appSettings;
	
	public JSettings(CEventHandler handler) throws StoreUnavailableException {
		
		super(handler, false);
		
		try {
			appSettings = Preferences.userRoot();
		}
		catch(AccessControlException e) {
			throw new StoreUnavailableException();
		}
		
		LoadPersistent();
			
	}
	
	
	protected boolean LoadBoolSetting(String Key) throws CParameterNotFoundException {
	
		/* CSFS: This annoying hack is to work around the fact that any
		 * failure to return a value is responded to by returning the default!
		 */
		
		try {
			int retval = appSettings.getInt("JDasher/B_" + Key, 2);
			if(retval == 0) return false;
			else if(retval == 1) return true;
			else throw new CParameterNotFoundException(Key);
		}
		catch (AccessControlException e) {
			throw new CParameterNotFoundException(Key);
		}
		
	}


	protected long LoadLongSetting(String Key) throws CParameterNotFoundException {
		
		try {
			long retval = appSettings.getLong("JDasher/L_" + Key, -999);
			if (retval == -999) throw new CParameterNotFoundException(Key);
			else return retval;
		}
		catch (AccessControlException e) {
			throw new CParameterNotFoundException(Key);
		}
	}


	protected String LoadStringSetting(String Key) throws CParameterNotFoundException {
	
		try {
			String retval = appSettings.get("JDasher/S_" + Key, "Parameter Error");
			if(retval.equals("Parameter Error")) throw new CParameterNotFoundException("Key");
			else return retval;
		}
		catch (AccessControlException e) {
			throw new CParameterNotFoundException(Key);
		}
	}


	protected void SaveSetting(String Key, boolean Value) {
		int val;
		if (Value == false) {
			val = 0;
		}
		else {
			val = 1;
		}
		try {
			appSettings.putInt("JDasher/B_" + Key, val);
		}
		catch (AccessControlException e) {
			return; // Fail silently; we will use the default next time
		}
	}


	protected void SaveSetting(String Key, long Value) {
		try {
			appSettings.putLong("JDasher/L_" + Key, Value);
		}
	
		catch (AccessControlException e) {
			return; // Fail silently; we will use the default next time
		}	
	}


	protected void SaveSetting(String Key, String Value) {
		try {
			appSettings.put("JDasher/S_" + Key, Value);
		}
		catch (AccessControlException e) {
			return; // Fail silently; we will use the default next time
		}
	}
		
}

/**
 * Exception indicating that JSettings was unable to initialise
 * due to the Preferences object's backing store being unavailable,
 * probably due to security limitations.
 */
class StoreUnavailableException extends Exception {
	// No properties
}
