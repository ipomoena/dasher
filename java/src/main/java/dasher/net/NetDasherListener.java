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

package dasher.net;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import java.util.Timer;
import java.util.TimerTask;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Main class powering NetDasher. This class listens for connections and,
 * whenever one is received, spawns a NetDasherCommandInterpreterThread to
 * determine what the client wants, and action their request.
 * <p>
 * Alphabet and Colour XML files are also read on startup to produce
 * central AlphIO and ColourIO objects, which are passed in to
 * every created Dasher; this way we avoid the tedious work of
 * repeatedly parsing the same data for each instance of Dasher we create.
 * <p>
 * Essentially the goal is to create an instance of Dasher, attached to
 * a NetScreen, for each client, and to pass the user's clicks and other
 * requests to the appropriate instance of Dasher as connections come in.
 * <p>
 * Since connections are conducted by HTTP for most clients and forwarded
 * by a simple PHP script, there are no persistent connections -- there is
 * only one connection per request and vice versa.
 */
public class NetDasherListener {

	/**
	 * HashMap of session identifiers to instances of Dasher, passed to
	 * each CommandInterpreterThread. 
	 */
	private static java.util.concurrent.ConcurrentHashMap<Integer, NetDashConnectionHandler> Dashers;
	
	/**
	 * Location to look for alphabet and colour XML files, as well
	 * as training texts.
	 */
	private static String dataLocation;
	
	/**
	 * DOM parser factory for XML.
	 */
	private static DocumentBuilderFactory dbf;
	
	/**
	 * Timer which runs a NetDashGC thread every 60 seconds
	 */
	private static Timer GCScheduler;
	
	/**
	 * Next free session identifier
	 */
	private static idWrapper nextID;
	
	
	public static void ScanAlphabetFiles(ArrayList<String> vFileList) {
		
		ScanFilesByRegex(vFileList, "alphabet.*.xml");
		
	}

	
	public static void ScanColourFiles(ArrayList<String> vFileList) {
		
		ScanFilesByRegex(vFileList, "colour.*.xml");
		
	}
	
	/**
	 * Delegate method used by both ScanAlphabetFiles and ScanColourFiles.
	 * <p>
	 * Fills its first parameter with all files in our systemLocation
	 * which match the given regex.
	 * 
	 * @param vFileList List to fill with matching files
	 * @param regex Regular expression determining what to add
	 */
	public static void ScanFilesByRegex(ArrayList<String> vFileList, String regex) {
		
		FileFilter alphFilter = FilterFactory.makeFilter(regex);
		
		try {
			File sysDir = new File(dataLocation);
			File[] sysFiles = sysDir.listFiles(alphFilter);
			
			if(sysFiles != null) {
				for(File f : sysFiles) {
					vFileList.add(f.getName());
				}
			}
		}
		catch(Exception e) {
			// Do nothing
		}
		
		/*try {
			File usrDir = new File(GetStringParameter(dasher.Esp_parameters.SP_USER_LOC));
			File[] usrFiles = usrDir.listFiles(alphFilter);
			
			if(usrFiles != null) {
				for(File f : usrFiles) {
					vFileList.add(f.getName());
				}
			}
		}
		catch(Exception e) {
			// Do nothing
		}*/
	}
	
	/**
	 * Creates our listening socket, schedules the garbage collector
	 * thread to run every 60 seconds, creates instances of
	 * CAlphIO and CColourIO to read in the appropriate XML
	 * data, and finally enters a tight loop of accepting connections
	 * and spawning NetDasherCommandInterpreterThreads to deal with them. 
	 * <p>
	 * The first argument is taken as the data location where XML files
	 * and training texts may be found.
	 */
	public static void main(String[] args) {
		
		ServerSocket DasherListen;
		Dashers = new java.util.concurrent.ConcurrentHashMap<Integer, NetDashConnectionHandler>();
		
		GCScheduler = new Timer();
		nextID = new idWrapper();
				
		dbf = DocumentBuilderFactory.newInstance();
		
		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("-d")) {
				if(i + 1 == args.length) {
					break;
				}
				else {
					dataLocation = args[i+1];
					
					if(dataLocation.charAt(dataLocation.length() - 1) != '/' && dataLocation.charAt(dataLocation.length() - 1) != '\\') {
						dataLocation = dataLocation + "\\";
					}
				}
			}
		}
		
		System.out.printf("Pre-reading alphabet and colour-scheme XML files (data path: %s)%n", dataLocation);
		
		ArrayList<String> alphFiles = new ArrayList<String>();
		ScanFilesByRegex(alphFiles, "alphabet.*.xml");		
		dasher.CAlphIO centralAlphIO = new dasher.CAlphIO(dataLocation, "", alphFiles, null);
		
		ArrayList<String> colourFiles = new ArrayList<String>();
		ScanFilesByRegex(colourFiles, "colour.*.xml");
		dasher.CColourIO centralColourIO = new dasher.CColourIO(dataLocation, "", colourFiles, null);
		
		System.out.printf("Pre-reading completed.%n");
		
		TimerTask GCTask = new NetDashGC(Dashers); 
		
		GCScheduler.schedule(GCTask, 60000, 60000);
		
		try {
			DasherListen = new ServerSocket(2112);
		}
		catch(IOException e) {
			System.out.printf("Could not listen on port 2112: %s%n", e);
			return;
		}
			
		System.out.printf("Listening on port 2112.%n");
		
		while(true) {
			
			Socket DasherConnection;
			
			try {
				DasherConnection = DasherListen.accept();
				DasherConnection.setSoLinger(true, 1);
			}
			catch(IOException e) {
				System.out.printf("Could not accept a connection: %s%n", e);
				continue; // No connection to hang up; let's try again.
			}
			
			NetDasherCommandInterpreterThread newThread = new NetDasherCommandInterpreterThread(DasherConnection, Dashers, nextID, dbf, dataLocation, centralAlphIO, centralColourIO);
			
			newThread.start();
		}
	}
}

/**
 * Responsible for supplying free IDs to use as session IDs.
 * <p>
 * At present, simply returns a linear sequence and does not
 * re-use abandoned session IDs.
 */
class idWrapper {
	int nextID = 0;
	
	public int getFreeID() {
		return nextID++;
	}
}
