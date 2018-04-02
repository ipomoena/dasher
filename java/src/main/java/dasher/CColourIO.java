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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * Responsible for reading a given list of XML files, extracting
 * colour scheme information, and creating a list of ColourInfo objects
 * for each.
 * <p>
 * Further, after doing so, responsible for returning a ColourInfo
 * object corresponding to a given alphabet name, and of giving
 * a full list of available colour schemes.
 *
 */
public class CColourIO {

	/**
	 * The current system data location, for locating DTDs.
	 */
	protected String SystemLocation;
	
	/**
	 * The current user data location, for locating DTDs.
	 */
	protected String UserLocation;
	
	/**
	 * Map from colour scheme names to their ColourInfo objects.
	 */
	protected HashMap<String, ColourInfo> Colours = new HashMap<String,ColourInfo>(); // map short names (file names) to descriptions
	
	/**
	 * Files which are to be parsed.
	 */
	protected ArrayList<String> Filenames;
	
	/**
	 * Flag indicating whether or not the current colour scheme may be modified.
	 */
	public boolean LoadMutable;
	
	/**
	 * Interface which will be used for Applet style I/O.
	 */
	public CDasherInterfaceBase m_Interface;
	
	/**
	 * SAXParser used to read XML files.
	 */
	protected SAXParser parser;
	
	/**
	 * Simple struct which represents a colour scheme.
	 */
	public static class ColourInfo {
	    // Basic information
		/**
		 * Friendly name of this colour-scheme.
		 */
	    String ColourID;
	    
	    /**
	     * Can this scheme be modified?
	     */
	    boolean Mutable;               // If from user we may play. If from system defaults this is immutable. User should take a copy.

	    // Complete description of the colour:
	    /**
	     * Array of scheme's defined colours' red values.
	     */
	    ArrayList<Integer> Reds = new ArrayList<Integer>();
	    
	    /**
	     * Array of scheme's defined colours' green values.
	     */
	    ArrayList<Integer> Greens = new ArrayList<Integer>();
	    
	    /**
	     * Array of scheme's defined colours' blue values.
	     */
	    ArrayList<Integer> Blues = new ArrayList<Integer>();
	}
	
	/**
	 * Sole constructor. Parses a given list of XML files; once
	 * the constructor terminates, the class is ready to give
	 * a list of available colour schemes and return ColourInfo
	 * objects requested by name.
	 * 
	 * @param SysLoc System data location to search for DTD files. 
	 * @param UserLoc User data location to search for DTD files.
	 * @param Fnames List of files to parse. 
	 * @param dib Interface against which we can perform applet-style IO. Optional; if null, applet-style IO will not be attempted.
	 */
	public CColourIO(String SysLoc, String UserLoc, ArrayList<String> Fnames, CDasherInterfaceBase dib) {
		
		SystemLocation = SysLoc;
		UserLocation = UserLoc;
		Filenames = Fnames;
		LoadMutable = false;
		
		m_Interface = dib;
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			parser = factory.newSAXParser();
		}
		catch(Exception e) {
			System.out.printf("Exception creating XML parser in CColourIO: %s%n", e);
		}
		
		CreateDefault();
		
		LoadMutable = false;
		ParseFile(SystemLocation + "colour.xml");
		if(Filenames.size() > 0) {
			for(int i = 0; i < Filenames.size(); i++) {
				ParseFile(SystemLocation + Filenames.get(i));
			}
		}
		LoadMutable = true;
		ParseFile(UserLocation + "colour.xml");
		if(Filenames.size() > 0) {
			for(int i = 0; i < Filenames.size(); i++) {
				ParseFile(UserLocation + Filenames.get(i));
			}
		}
	}
	
	/**
	 * Parse a named XML file. Colour-schemes found, if any,
	 * will be imported and may be enumerated with GetColours
	 * or retrieved using GetInfo. If the specified XML file
	 * cannot be read, the method prints the exception raised
	 * but does not throw an exception.
	 * <p>
	 * This method will attempt to retrieve the file first by
	 * ordinary file I/O, and then if this fails, will attempt
	 * to retrieve a resource stream, allowing it to find XML
	 * files stored in a JAR.
	 * 
	 * @param filename Relative or absolute path to the file to be parsed.
	 */
	public void ParseFile(String filename) {
		
		InputStream FileInput;
		try {
			FileInput = new FileInputStream(filename);
			// Try ordinary IO
		}
		catch(Exception e) {
			try {
				FileInput = m_Interface.getResourceStream(filename);
				// Try applet-style IO
			}
			catch(Exception ex) {
				return; // If the file cannot be retrieved, act as if it does not exist at all.
			}
		}
		
		InputSource XMLInput = new InputSource(FileInput);
		
		DefaultHandler handler = new ColourXMLHandler(Colours, this, SystemLocation, UserLocation);
		// Back-pointer passed so it can modify our colours ArrayList.
		
		
		
		try {
			parser.parse(XMLInput, handler);
		}
		catch (Exception e) {
			System.out.printf("Exception reading %s: %s%n", filename, e.toString());
			return; // Again, an invalid file should be treated as if it isn't there.
		}
		
	}
	
	/**
	 * Fills a given Collection with the names of all available
	 * colour schemes.
	 * <p>
	 * Colour schemes can be made available by being parsed from
	 * an XML file by way of the ParseFile method (or through
	 * the constructor), or by the manual creation of a ColourInfo
	 * object which must then be saved using SetInfo.
	 * 
	 * @param ColourList Collection to be filled with available colours.
	 */
	public void GetColours(Collection<String> ColourList) {
		ColourList.clear();
		
		/* CSFS: Rewritten from the old version which used
		 * C++ list-iterators.
		 */
		
		for(Map.Entry<String, ColourInfo> m : Colours.entrySet()) {
			ColourList.add(m.getValue().ColourID);
		}
		
	}
	
	/**
	 * Retrieves the ColourInfo object associated with a given colour scheme.
	 * <p>
	 * If the specified name cannot be found, the Default scheme
	 * will be retrieved. To avoid this happening, make sure that
	 * only schemes enumerated by GetColours are requested.
	 * <p>
	 * Schemes' names may consist of any Unicode character in the
	 * Basic Multilingual Plane.
	 * 
	 * @param ColourID Name of the colour scheme to retrieve.
	 * @return ColourInfo object representing the named scheme, or the Default if this is not possible.
	 */
	public ColourInfo GetInfo(String ColourID) {
		if(ColourID == "")            // return Default if no colour scheme is specified
			return Colours.get("Default");
		else {
			if(Colours.containsKey(ColourID)) {
				return Colours.get(ColourID);
			}
			else {
				// if we don't have the colour scheme they asked for, return default
				return Colours.get("Default");
			}
		}
	}
	
	/**
	 * Adds a ColourInfo object to the list of those available.
	 * At present, schemes cannot be manually stored in Dasher,
	 * but this may change in the future if user-defined schemes
	 * become a possibility.
	 * 
	 * @param NewInfo Colour scheme to save. Must not be null.
	 */
	public void SetInfo(ColourInfo NewInfo) {
		Colours.put(NewInfo.ColourID, NewInfo);
		Save(NewInfo.ColourID);
	}
	
	/**
	 * Removes a colour scheme by name. This removes the stated
	 * scheme from the list of those available; it will no longer
	 * be returned by GetColours or be retrievable by GetInfo.
	 * 
	 * @param ColourID Name of the scheme to remove. If the given name
	 * does not exist the method will return without error.
	 */
	public void Delete(String ColourID) {
		Colours.remove(ColourID);
		Save("");
	}
	
	/**
	 * Stub method. In the future, this may save a given scheme
	 * as an XML file, or in some other format. This should be
	 * implemented if user-defined or mutable colour schemes are
	 * implemented.
	 * <p>
	 * The class must already know about the scheme to be able
	 * to save it.
	 * 
	 * @param name Name of the scheme to save.
	 */
	public void Save(String name) {
		
		// Stub
		
	}
	
	/**
	 * Creates and stores a default colour scheme, named simply
	 * <i>Default</i>. This is the scheme which will be returned
	 * in the event of a non-existent scheme being requested,
	 * and can always be assumed to be available.
	 *
	 */
	protected void CreateDefault() {
		  
		ColourInfo Default = new ColourInfo();
		
		Default.ColourID = "Default";
		Default.Mutable = false;
		Default.Reds.add(255);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(218);
		Default.Greens.add(218);
		Default.Blues.add(218);
		Default.Reds.add(0);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(80);
		Default.Greens.add(80);
		Default.Blues.add(80);
		Default.Reds.add(235);
		Default.Greens.add(235);
		Default.Blues.add(235);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(180);
		Default.Greens.add(238);
		Default.Blues.add(180);
		Default.Reds.add(155);
		Default.Greens.add(205);
		Default.Blues.add(155);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(255);
		Default.Greens.add(255);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(200);
		Default.Blues.add(0);
		Default.Reds.add(255);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(255);
		Default.Greens.add(255);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(200);
		Default.Blues.add(0);
		Default.Reds.add(255);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(255);
		Default.Greens.add(255);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(200);
		Default.Blues.add(0);
		Default.Reds.add(255);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(255);
		Default.Greens.add(255);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(200);
		Default.Blues.add(0);
		Default.Reds.add(255);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(255);
		Default.Greens.add(255);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(200);
		Default.Blues.add(0);
		Default.Reds.add(255);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(255);
		Default.Greens.add(255);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(200);
		Default.Blues.add(0);
		Default.Reds.add(255);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(255);
		Default.Greens.add(255);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(0);
		Default.Greens.add(0);
		Default.Blues.add(0);
		Default.Reds.add(80);
		Default.Greens.add(80);
		Default.Blues.add(80);
		Default.Reds.add(255);
		Default.Greens.add(255);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(255);
		Default.Greens.add(174);
		Default.Blues.add(185);
		Default.Reds.add(255);
		Default.Greens.add(187);
		Default.Blues.add(255);
		Default.Reds.add(135);
		Default.Greens.add(206);
		Default.Blues.add(255);
		Default.Reds.add(0);
		Default.Greens.add(255);
		Default.Blues.add(0);
		Default.Reds.add(240);
		Default.Greens.add(240);
		Default.Blues.add(0);
		Default.Reds.add(255);
		Default.Greens.add(0);
		Default.Blues.add(0);
		
		Colours.put("Default", Default);
	}
	
}

class ColourXMLHandler extends DefaultHandler {
	protected HashMap<String, CColourIO.ColourInfo> Colours;
	protected CColourIO m_Parent;
	
	protected String userLoc;
	protected String sysLoc;
	
	protected CColourIO.ColourInfo currentColour;
		
	public ColourXMLHandler(HashMap<String, CColourIO.ColourInfo> Cols, CColourIO parent, String sys, String usr) {
		Colours = Cols;
		m_Parent = parent;
		
		sysLoc = sys;
		userLoc = usr;
	}
	
	public void startElement(String namespaceURI, String simpleName, String qualName, Attributes tagAttributes) throws SAXException {
		
		String tagName = (simpleName.equals("") ? qualName : simpleName);
		
		if(tagName == "palette") {
			currentColour = new CColourIO.ColourInfo();
			currentColour.Mutable = m_Parent.LoadMutable;
			for(int i = 0; i < tagAttributes.getLength(); i++) {
		    	String attributeName = (tagAttributes.getLocalName(i).equals("") ? tagAttributes.getQName(i) : tagAttributes.getLocalName(i));
		    	if(attributeName == "name") {
		    		currentColour.ColourID = tagAttributes.getValue(i); 
		    	}
			}
		}
		else if(tagName == "colour") {
			for(int i = 0; i < tagAttributes.getLength(); i++) {
		    	String attributeName = (tagAttributes.getLocalName(i).equals("") ? tagAttributes.getQName(i) : tagAttributes.getLocalName(i));
		    	if(attributeName == "r") {
		    		currentColour.Reds.add(Integer.parseInt(tagAttributes.getValue(i)));
		    	}
		    	if(attributeName == "g") {
		    		currentColour.Greens.add(Integer.parseInt(tagAttributes.getValue(i)));
		    	}
		    	if(attributeName == "b") {
		    		currentColour.Blues.add(Integer.parseInt(tagAttributes.getValue(i)));
		    	}
			}
		}
	}
	
	public void endElement(String namespaceURI, String simpleName, String qualName) {
		String tagName = (simpleName.equals("") ? qualName : simpleName);
		
		if(tagName == "palette") {
			Colours.put(currentColour.ColourID, currentColour);
		}
	
	}
	
	public InputSource resolveEntity(String publicName, String systemName) throws IOException, SAXException {
		
		if(systemName.contains("colour.dtd")) {
			try {
				return new InputSource(new FileInputStream(sysLoc + "colour.dtd"));
			}
			catch(Exception e) {
				try {
					return new InputSource(new FileInputStream(userLoc + "colour.dtd"));
				}
				catch(Exception ex) {
					return new InputSource(m_Parent.m_Interface.getResourceStream(sysLoc + "colour.dtd"));
				}
			}
		}
		else {
			return null;
		}
		
		/* CSFS: This is here because SAX will by default look in a system location
		 * first, which throws a security exception when running as an Applet.
		 * All other requests are deferred to SAX.
		 */
		
		
	}
	
}