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

import java.io.InputStream;
import java.util.Collection;

import dasher.CDasherInterfaceBase;
import dasher.CEvent;
import dasher.CSettingsStore;

/**
 * Simple implementation of CDasherInterfaceBase providing minimal
 * facilities to support an Applet version of Dasher.
 */
public class JDasher extends CDasherInterfaceBase {

	/**
	 * Our mouse input
	 */
	private JMouseInput m_MouseInput;
	
	/**
	 * Host to notify of events and request redraws.
	 */
	private JDasherHost m_Host;
			
	/**
	 * Creates a new JDasher. We call CreateSettingsStore immediately
	 * after our super-constructor, create, wrap and register a new
	 * JMouseInput, run CreateInput and register our newly created
	 * input device wiht our host to hook it up to mouse events. 
	 * 
	 * @param host Host to report events and request mouse events
	 * and redraws.
	 */
	public JDasher(JDasherHost host) {
		super();
		CreateSettingsStore();
		m_MouseInput = new JMouseInput(m_EventHandler, m_SettingsStore);
		RegisterFactory(new dasher.CWrapperFactory(m_EventHandler, m_SettingsStore, m_MouseInput));
		CreateInput();
		
		m_Host = host;
		m_Host.regMouseMotionListener(m_MouseInput);
			
	}
	
	/**
	 * External event handler; simply passes the event to our
	 * host, typically a JDasherApplet.
	 * 
	 * @param Event Event to handle
	 */
	public void ExternalEventHandler(CEvent Event) {

		m_Host.handleEvent(Event);
	
	}

	/**
	 * Attempts to create a JSettings object; if a StoreUnavailableException
	 * is produced in the course of this, we fall back and produce a 
	 * standard CSettingsStore.
	 * <p>
	 * If a SettingsStore already exists, we ignore the call.
	 */
	public void CreateSettingsStore() {
				
		if(m_SettingsStore == null) {
		
			try {
				m_SettingsStore = new JSettings(m_EventHandler);
			}
			catch (StoreUnavailableException e) {
				// We can't use the registry/config file due to security problems.
				m_SettingsStore = new CSettingsStore(m_EventHandler);
			}
			
		
		
		}
	}
	
	/**
	 * Attempts to retrieve a stream pointing to a given file in this Applet's
	 * JAR file and recalls TrainStream on this stream.
	 * <p>
	 * If we can't retrieve said stream for some reason, 0 is
	 * returned.
	 * 
	 * @param Filename File to retrieve
	 * @param iTotalBytes Bytes to read
	 * @param iOffset Offset to start reading
	 * @return Number of bytes successfully read
	 */
	public int TrainFile(String Filename, int iTotalBytes, int iOffset) {
		
		java.io.InputStream in = this.getClass().getResourceAsStream(Filename);
		if(in == null) {
			System.out.printf("Couldn't retrieve resource stream for %s%n", Filename);
			return 0; // Failed to retrieve, possibly due to security.
		}
		
		return TrainStream(in, iTotalBytes, iOffset);
		
	}

	/**
	 * Attempts to retrieve a ResourceStream for a given file by
	 * retrieving it from our JAR file, and returns its available
	 * property which should indicate the file size.
	 * <p>
	 * In the event of failure of any sort, we return zero.
	 * 
	 * @param strFileName File whose size we wish to retrieve
	 * @return File size, or 0 on error.
	 */
	public int GetFileSize(String strFileName) {
		try {
			java.io.InputStream in = this.getClass().getResourceAsStream(strFileName);
			return in.available();
		}
		catch (Exception e) { // Including if 'in' was null (throwing NullPointerException)
			return 0;
		}
	}

	/**
	 * Populates a given Collection with a list of available
	 * alphabet files.
	 * <p>
	 * Due to the difficulty in enumerating the contents of a JAR
	 * file, at present this is hard coded to report a certain set.
	 * <p>
	 * Ideally this should be upgraded to report the true list of
	 * available files, potentially by reading some master XML file.
	 * 
	 * @param vFileList Collection to fill with a list of available alphabet files.
	 */	
	public void ScanAlphabetFiles(Collection<String> vFileList) {
		
		vFileList.add("alphabet.english.xml");
		vFileList.add("alphabet.englishC.xml");
		vFileList.add("alphabet.Thai.xml");
						
		/* CSFS: TODO replace this with an actual file enumerator.
		 */
		
	}

	/**
	 * Populates a given Collection with a list of available
	 * colour files.
	 * <p>
	 * Due to the difficulty in enumerating the contents of a JAR
	 * file, at present this is hard coded to report a certain set.
	 * <p>
	 * Ideally this should be upgraded to report the true list of
	 * available files, potentially by reading some master XML file.
	 * 
	 * @param vFileList Collection to fill with a list of available colour files.
	 */
	public void ScanColourFiles(Collection<String> vFileList) {

		vFileList.add("colour.euroasian.xml");
		vFileList.add("colour.rainbow.xml");
		vFileList.add("colour.euroasian-new.xml");
		vFileList.add("colour.thai.xml");
		
		/* CSFS: See above */
		
	}
	
	/**
	 * Attempts to retrieve a resource stream representing a given
	 * file by using the getResourceAsStream method.
	 * 
	 * @return InputStream pointing to the relevant file if possible,
	 * or null if not.
	 */
	public InputStream getResourceStream(String filename) {
		return getClass().getResourceAsStream(filename);
	}

	/**
	 * Sets our system path to "system.rc/".
	 */
	public void SetupPaths() {
		
		m_SettingsStore.SetStringParameter(dasher.Esp_parameters.SP_SYSTEM_LOC, "system.rc/");
		
	}

	
	/**
	 * Stub
	 */
	public void SetupUI() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Orders our host to redraw.
	 * 
	 * @param bChanged ignored
	 */
	public void Redraw(boolean bChanged) {
		m_Host.Redraw();
	}
	
	
	
}
