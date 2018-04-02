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

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

/**
 * 
 * Responsible for reading a given list of XML files, extracting
 * alphabet information, and creating a list of AlphInfo objects
 * for each.
 * <p>
 * Further, after doing so, responsible for returning an AlphInfo
 * object corresponding to a given alphabet name, and of giving
 * a full list of available alphabets.
 *
 */
public class CAlphIO {

	/* CSFS: I'm not exactly sure which of these strings are plain ASCII used internally
	 * and which are UTF-8. For now I've made mostly everything UTF-8 except for filenames.
	 */
	
	/* CSFS: I have added comments detailing which XML field corresponds
	 * to each variable.
	 */
	
	/**
	 * Pointer to the DasherInterfaceBase which requested this enumeration.
	 * This is only used for the purposes of trying Applet-style
	 * resource retrieval, and may safely be set to null if this
	 * is not required. 
	 */
	public CDasherInterfaceBase m_Interface;
	
	/**
	 * Path of system resource files, used in the course
	 * of locating DTD files where necessary.
	 */
	protected String SystemLocation;
	
	/**
	 * Path of user resource files, used in the course
	 * of locating DTD files where necessary.
	 */
	protected String UserLocation;
	
	/**
	 * Map from Strings to AlphInfo objects, used in getting
	 * an alphabet by name.
	 */
	protected HashMap <String, AlphInfo> Alphabets = new HashMap<String, AlphInfo>(); 
	// map short names (file names) to descriptions
	
	/**
	 * List of filenames to parse.
	 */
	protected ArrayList<String> Filenames;
	
	/**
	 * Map from String alphabet types to enumerated constants
	 * (members of Opts.AlphabetTypes).
	 */
	public HashMap <String, Integer> StoT = new HashMap<String, Integer>();
	
	/**
	 * Map from Opts.AlphabetTypes constants to their String names.
	 */
	public HashMap <Integer, String> TtoS = new HashMap<Integer, String>();
		
	/**
	 * Whether the loaded alphabet may be altered.
	 */
	public boolean LoadMutable;
	
	/**
	 * Parser to be used to import XML data.
	 */
	protected SAXParser parser;
	
	/**
	 * Simple struct representing an alphabet.
	 */
	static class AlphInfo {
		// Basic information
		/**
		 * Alphabet name
		 */
		String AlphID; // <alphabet name="[AlphID]">
		
		/**
		 * Whether this alphabet may be altered. At present this
		 * isn't used in Dasher, but may in the future be used
		 * to support user-created alphabets.
		 */
		boolean Mutable;  // If from user we may play. If from system defaults this is immutable. User should take a copy.
		
		// Complete description of the alphabet:
		/**
		 * Training file to be used to train a language model
		 * which uses this alphabet as its symbol-set.
		 */
		String TrainingFile;
		
		// Undocumented pending changes
		String GameModeFile;
		
		/**
		 * Preferred colour scheme of this alphabet.
		 */
		String PreferredColours;
		
		// FIXME: Redundant; should be removed.
		int Encoding;
		int Type; // <alphabet><encoding type="[Type]">
		// Needs converting to the appropriate member of
		// Opts.AlphabetTypes. The table for this is at the top of the
		// CPP file.
		
		/**
		 * Preferred orientation of this alphabet. Should be
		 * set to a member of Opts.ScreenOrientations.
		 */
		int Orientation; // <alphabet><orientation type="[Orientation]">
		// Needs converting from eg. "RL" to Opts.ScreenOrientations.RighttoLeft.
		
		/**
		 * Number of characters in this alphabet.
		 */
		int m_iCharacters;
		
		/**
		 * List of groups into which this alphabet's symbols
		 * are categorised.
		 */
		ArrayList<SGroupInfo> m_vGroups = new ArrayList<SGroupInfo>(); // Enumeration of <Group name="..." b="...">
		// Name attribute is currently ignored.
		// Potential "label" and "visible" attributes also. Visible defaults
		// to false for the first group, and true for all others.
		
		/**
		 * Root group; contains all groups.
		 */
		SGroupInfo m_BaseGroup;
		
		/**
		 * List of characters in this alphabet, each of which
		 * is represented by a small character struct.
		 * 
		 * @see character
		 */
		ArrayList<character> m_vCharacters = new ArrayList<character>();
		
		/**
		 * Paragraph character for this alphabet.
		 */
		character ParagraphCharacter = new character();       // display and edit text of paragraph character. Use ("", "") if no paragraph character.
		
		/**
		 * Space character for this alphabet.
		 */
		character SpaceCharacter = new character();   // display and edit text of Space character. Typically (" ", "_"). Use ("", "") if no space character.
		
		/**
		 * Control character for this alphabet.
		 */
		character ControlCharacter = new character(); // display and edit text of Control character. Typically ("", "Control"). Use ("", "") if no control character.
		
		// Added for Kanji Conversion by T.Kaburagi 15 July 2005
		/**
		 * Start conversion character for this alphabet.
		 * (Used to convert Hiragana to Kanji)
		 */
		character StartConvertCharacter = new character();
		
		/**
		 * End conversion character for this alphabet.
		 * (Used to convert Hiragana to Kanji)
		 */
		character EndConvertCharacter = new character();
				
	}
	
	/**
	 * Simple struct representing a character in an alphabet.
	 * <p>
	 * The Display and Text attributes are usually the same,
	 * but are distinguished in that Display is used when drawing
	 * the symbol in a DasherNode on the screen, and Text when
	 * printing it as output.
	 * <p>
	 * An example of a characters with a difference is most
	 * combining accents, which commonly use a dotted circle or
	 * other decorative item to represent the letter with which
	 * they will combine, which of course does not appear when
	 * it is entered as text. 
	 */	
	static class character {
		/**
		 * String representation for display purposes.
		 */
		String Display = ""; // <s d="...">
		/**
		 * String representation for typing in the edit box.
		 */
		String Text = ""; // <s t="...">
		/**
		 * Background colour
		 */		
		int Colour; // <s b="..."> (b for Background)
		/**
		 * Foreground colour
		 */
		String Foreground = ""; // <s f="...">
		// Seems to represent only the name of a colour, which is always ASCII anyway.
	}
	
	/**
	 * Sole constructor. This will parse the list of files given in Fnames
	 * by attempting both ordinary file I/O and applet-style
	 * web retrieval. Once the constructor terminates, all XML
	 * files have been read and the object is ready to be queried
	 * for alphabet names.
	 *  
	 * @param SysLoc System data location, for retrieval of DTD files. Optional; if not supplied, this location will not be considered for DTD location.
	 * @param UserLoc User data location, for retrieval of DTD files. Optional; if not supplied, this location will not be considered for DTD location.
	 * @param Fnames Filenames to parse; these may be relative or absolute.
	 * @param Interface Reference to the InterfaceBase parent class for applet-style IO. Optional; if not supplied, applet-style IO will fail.
	 */
	public CAlphIO(String SysLoc, String UserLoc, ArrayList<String> Fnames, CDasherInterfaceBase Interface) {
		SystemLocation = SysLoc;
		UserLocation = UserLoc;
		Filenames = Fnames;
		LoadMutable = false;
		m_Interface = Interface;
		CreateDefault();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		
		try {
			parser = factory.newSAXParser();
		}
		catch(Exception e) {
			System.out.printf("Error creating SAX parser: %s%n", e);
			return;
		}
		
		/* CSFS: Method of translating between string representations
		 * and numerical indentifiers altered to use a HashMap; however
		 * this might not be optimal, will consider changing this later.
		 */
		
		TtoS.put(0, "None");
		TtoS.put(Opts.AlphabetTypes.Arabic, "Arabic");
		TtoS.put(Opts.AlphabetTypes.Baltic, "Baltic");
		TtoS.put(Opts.AlphabetTypes.CentralEurope, "CentralEurope");
		TtoS.put(Opts.AlphabetTypes.ChineseSimplified, "ChineseSimplified");
		TtoS.put(Opts.AlphabetTypes.ChineseTraditional, "ChineseTraditional");
		TtoS.put(Opts.AlphabetTypes.Cyrillic, "Cyrillic");
		TtoS.put(Opts.AlphabetTypes.Greek, "Greek");
		TtoS.put(Opts.AlphabetTypes.Hebrew, "Hebrew");
		TtoS.put(Opts.AlphabetTypes.Japanese, "Japanese");
		TtoS.put(Opts.AlphabetTypes.Korean, "Korean");
		TtoS.put(Opts.AlphabetTypes.Thai, "Thai");
		TtoS.put(Opts.AlphabetTypes.Turkish, "Turkish");
		TtoS.put(Opts.AlphabetTypes.VietNam, "VietNam");
		TtoS.put(Opts.AlphabetTypes.Western, "Western");
		
		for(Map.Entry<Integer, String> m : TtoS.entrySet()) {
			StoT.put(m.getValue(), m.getKey());
		}
	
		LoadMutable = false;
		ParseFile(SystemLocation + "alphabet.xml");
		if(Filenames.size() > 0) {
			for(int i = 0; i < Filenames.size(); i++) {
				ParseFile(SystemLocation + Filenames.get(i));
			}
		}
		LoadMutable = true;
		ParseFile(UserLocation + "alphabet.xml");
		if(Filenames.size() > 0) {
			for(int i = 0; i < Filenames.size(); i++) {
				ParseFile(UserLocation + Filenames.get(i));
			}
		} 
		
		
	}
	
	/**
	 * Parse a given XML file for alphabets. Any resulting alphabets
	 * will be added to the internal buffer ready for retrieval
	 * using GetInfo or GetAlphabets.
	 * 
	 * @param filename File to parse
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
		
		DefaultHandler handler = new AlphXMLHandler(Alphabets, this, SystemLocation, UserLocation);
		// Pass in the Alphabet HashMap so it can be modified
		try {
			parser.parse(XMLInput, handler);
		}
		catch (Exception e) {
			System.out.printf("Exception reading %s: %s%n", filename, e.toString());
			return; // Again, an invalid file should be treated as if it isn't there.
		}
				
	}
	
	/**
	 * Fills the passed Collection with the names of all available alphabets.
	 * 
	 * @param AlphabetList Collection to be filled. 
	 */
	public void GetAlphabets(java.util.Collection<String> AlphabetList) {
		
		/* CSFS: Changed from a C++ listIterator */
		
		AlphabetList.clear();
		for(Map.Entry<String, AlphInfo> m : Alphabets.entrySet()) {
			AlphabetList.add(m.getValue().AlphID);
		}
	}
	
	/**
	 * Retrieves the name of the default alphabet. At present this
	 * will return English with limited punctuation if available,
	 * or Default if not.
	 * 
	 * @return Name of a reasonable default alphabet.
	 */
	public String GetDefault() {
		if(Alphabets.containsKey("English with limited punctuation")) {
			return ("English with limited punctuation");
		}
		else {
			return ("Default");
		}
	}
	
	/**
	 * Returns an AlphInfo object representing the alphabet with
	 * a given name. In the event that it could not be found,
	 * the Default alphabet is returned instead. To ensure that
	 * this is not the case, check the available alphabets first
	 * using GetAlphabets().
	 * 
	 * @param AlphID Name of the alphabet to be retrieved.
	 * @return Either the asked alphabet, or the default.
	 */
	public AlphInfo GetInfo(String AlphID) {
		if(Alphabets.containsKey(AlphID)) {
			// if we have the alphabet they ask for, return it
			return Alphabets.get(AlphID);
		}
		else {
			// otherwise, give them default - it's better than nothing
			return Alphabets.get("Default");
		}
	}
	
	/**
	 * Registers a new AlphInfo object as a valid alphabet, which
	 * will henceforth be included in enumerations of available
	 * alphabets.
	 * 
	 * @param NewInfo New alphabet
	 */
	public void SetInfo(AlphInfo NewInfo) {
		Alphabets.put(NewInfo.AlphID, NewInfo);
		Save(NewInfo.AlphID);
	}
	
	/**
	 * Removes a given alphabet; it will no longer appear in
	 * enumerations of alphabets. If it does not exist,
	 * this method will return without error.
	 * 
	 * @param AlphID
	 */
	public void Delete(String AlphID) {
		Alphabets.remove(AlphID);
	}
	
	/**
	 * Stub. At present Dasher does not permit users to
	 * specify their own alphabets, but in the case that this
	 * were introduced, this method would write out an XML
	 * document for the new alphabet.
	 * 
	 * @param AlphID Name of alphabet to save.
	 */
	public void Save(String AlphID) {
		// stub, for now.
	}
	
	/**
	 * Creates the default alphabet and stores as an available
	 * alphabet. This will be returned in the case that a requested
	 * alphabet cannot be retrieved; at present it is essentially
	 * lower-case english with no punctuation or numerals.
	 * <p>
	 * The constructor calls this method prior to attempting
	 * to read XML files; it should not need to be called
	 * more than once unless the Default is deleted.
	 *
	 */
	protected void CreateDefault() {
		// TODO I appreciate these strings should probably be in a resource file.
		// Not urgent though as this is not intended to be used. It's just a
		// last ditch effort in case file I/O totally fails.
		AlphInfo Default = new AlphInfo();
		Default.AlphID = "Default";
		Default.Type = Opts.AlphabetTypes.Western;
		Default.Mutable = false;
		Default.Orientation = Opts.ScreenOrientations.LeftToRight;
		Default.ParagraphCharacter.Display = "¶";
		Default.ParagraphCharacter.Text = "\r\n";
		Default.SpaceCharacter.Display = "_";
		Default.SpaceCharacter.Text = " ";
		Default.SpaceCharacter.Colour = 9;
		Default.ControlCharacter.Display = "Control";
		Default.ControlCharacter.Text = "";
		Default.ControlCharacter.Colour = 8;
		Default.TrainingFile = "training_english_GB.txt";
		Default.GameModeFile = "gamemode_english_GB.txt";
		Default.PreferredColours = "Default";
		String Chars = "abcdefghijklmnopqrstuvwxyz";
		
		Default.m_BaseGroup = null;
		character temp;
		
		for(Character c : Chars.toCharArray()) {
			temp = new character();
			temp.Text = c.toString();
			temp.Display = c.toString();
			temp.Colour = 10;
			Default.m_vCharacters.add(temp);
		}
		
		Alphabets.put("Default", Default);
	}
	
}

/**
 * 
 * SAX XML handler which populates AlphInfo objects and adds them
 * to the AlphIO class list of available alphabets.
 *
 */
class AlphXMLHandler extends DefaultHandler {
	
	protected HashMap<String, CAlphIO.AlphInfo> Alphs;
	protected CAlphIO m_Parent;
	protected CAlphIO.AlphInfo currentAlph;
	protected String currentTag;
	protected SGroupInfo currentGroup;
	protected boolean bFirstGroup;
	
	protected String systemLoc, userLoc;
	
	public AlphXMLHandler(HashMap<String, CAlphIO.AlphInfo> i_Alph, CAlphIO parent, String sysloc, String userloc) {
		Alphs = i_Alph;
		m_Parent = parent; 
		
		userLoc = userloc;
		systemLoc = sysloc;
	}

	public void startElement(String namespaceURI, String simpleName, String qualName, Attributes tagAttributes) throws SAXException {
		
		String tagName = (simpleName.equals("") ? qualName : simpleName);
		
		if(tagName == "alphabet") {
			/* A new alphabet is beginnning. Initialise the data structure
			 * and fill it with default values. */
			
			currentAlph = new CAlphIO.AlphInfo();
			currentAlph.Mutable = m_Parent.LoadMutable;
		    currentAlph.SpaceCharacter.Colour = -1;
		    currentAlph.ParagraphCharacter.Colour = -1;
		    currentAlph.ControlCharacter.Colour = -1;
		    currentAlph.StartConvertCharacter.Text = "";
		    currentAlph.EndConvertCharacter.Text = "";
		    currentAlph.m_iCharacters = 1; // Start at 1 as 0 is the root node symbol
		    currentAlph.m_BaseGroup = null;
		    
		    bFirstGroup = true;
		    
		    /* Find the 'name' attribute */
		    for(int i = 0; i < tagAttributes.getLength(); i++) {
		    	String attributeName = (tagAttributes.getLocalName(i).equals("") ? tagAttributes.getQName(i) : tagAttributes.getLocalName(i));
		    	if(attributeName == "name") {
		    		currentAlph.AlphID = tagAttributes.getValue(i);
		    	}
		    }
		    
		}
		
		else if(tagName == "orientation") {
			for(int i = 0; i < tagAttributes.getLength(); i++) {
				String attributeName = (tagAttributes.getLocalName(i).equals("") ? tagAttributes.getQName(i) : tagAttributes.getLocalName(i));
				if(attributeName == "type") {
					String orient = tagAttributes.getValue(i);
					if(orient == "RL") {
						currentAlph.Orientation = Opts.ScreenOrientations.RightToLeft;
					}
					else if(orient == "TB") {
						currentAlph.Orientation = Opts.ScreenOrientations.TopToBottom;
					}
					else if(orient == "BT") {
						currentAlph.Orientation = Opts.ScreenOrientations.BottomToTop;
					}
					else {
						currentAlph.Orientation = Opts.ScreenOrientations.LeftToRight;
					}
				}
			}
		}
		
		else if(tagName == "encoding") {
			for(int i = 0; i < tagAttributes.getLength(); i++) {
				String attributeName = (tagAttributes.getLocalName(i).equals("") ? tagAttributes.getQName(i) : tagAttributes.getLocalName(i));
				if(attributeName == "type") {
					currentAlph.Encoding = m_Parent.StoT.get(tagAttributes.getValue(i));
				}
			}
		}
		
		else if(tagName == "palette") {
			currentTag = "palette"; // will be handled by characters routine
		}
		
		else if(tagName == "train") {
			currentTag = "train"; // Likewise
		}
		
		else if(tagName == "paragraph") {
			for(int i = 0; i < tagAttributes.getLength(); i++) {
				String attributeName = (tagAttributes.getLocalName(i).equals("") ? tagAttributes.getQName(i) : tagAttributes.getLocalName(i));
				if(attributeName == "d") {
					currentAlph.ParagraphCharacter.Display = tagAttributes.getValue(i);
					currentAlph.ParagraphCharacter.Text = String.format("%n");
					
					/* CSFS: This slightly odd route is used because the traditional method,
					 * which is to read the system property 'line.seperator' is in fact
					 * forbidden for applets! Why it's potentially dangerous to establish
					 * how to terminate lines, I'm not sure.
					 */
					
				}
				if(attributeName == "b") {
					currentAlph.ParagraphCharacter.Colour = Integer.parseInt(tagAttributes.getValue(i));
				}
				if(attributeName == "f") {
					currentAlph.ParagraphCharacter.Foreground = tagAttributes.getValue(i);
				}
			}	
		}
		
		else if(tagName == "paragraph") {
			for(int i = 0; i < tagAttributes.getLength(); i++) {
				String attributeName = (tagAttributes.getLocalName(i).equals("") ? tagAttributes.getQName(i) : tagAttributes.getLocalName(i));
				if(attributeName == "d") {
					currentAlph.ParagraphCharacter.Display = tagAttributes.getValue(i);
					currentAlph.ParagraphCharacter.Text = System.getProperty("line.seperator");
				}
				if(attributeName == "b") {
					currentAlph.ParagraphCharacter.Colour = Integer.parseInt(tagAttributes.getValue(i));
				}
				if(attributeName == "f") {
					currentAlph.ParagraphCharacter.Foreground = tagAttributes.getValue(i);
				}
			}	
		}
		
		else if(tagName == "space") {
			for(int i = 0; i < tagAttributes.getLength(); i++) {
				String attributeName = (tagAttributes.getLocalName(i).equals("") ? tagAttributes.getQName(i) : tagAttributes.getLocalName(i));
				if(attributeName == "d") {
					currentAlph.SpaceCharacter.Display = tagAttributes.getValue(i);
				}
				if(attributeName == "t") {
					currentAlph.SpaceCharacter.Text = tagAttributes.getValue(i);
				}
				if(attributeName == "b") {
					currentAlph.SpaceCharacter.Colour = Integer.parseInt(tagAttributes.getValue(i));
				}
				if(attributeName == "f") {
					currentAlph.SpaceCharacter.Foreground = tagAttributes.getValue(i);
				}
			}	
		}
		
		else if(tagName == "control") {
			for(int i = 0; i < tagAttributes.getLength(); i++) {
				String attributeName = (tagAttributes.getLocalName(i).equals("") ? tagAttributes.getQName(i) : tagAttributes.getLocalName(i));
				if(attributeName == "d") {
					currentAlph.ControlCharacter.Display = tagAttributes.getValue(i);
				}
				if(attributeName == "t") {
					currentAlph.ControlCharacter.Text = tagAttributes.getValue(i);
				}
				if(attributeName == "b") {
					currentAlph.ControlCharacter.Colour = Integer.parseInt(tagAttributes.getValue(i));
				}
				if(attributeName == "f") {
					currentAlph.ControlCharacter.Foreground = tagAttributes.getValue(i);
				}
			}	
		}
		
		else if(tagName == "convert") {
			for(int i = 0; i < tagAttributes.getLength(); i++) {
				String attributeName = (tagAttributes.getLocalName(i).equals("") ? tagAttributes.getQName(i) : tagAttributes.getLocalName(i));
				if(attributeName == "d") {
					currentAlph.StartConvertCharacter.Display = tagAttributes.getValue(i);
				}
				if(attributeName == "t") {
					currentAlph.StartConvertCharacter.Text = tagAttributes.getValue(i);
				}
				if(attributeName == "b") {
					currentAlph.StartConvertCharacter.Colour = Integer.parseInt(tagAttributes.getValue(i));
				}
				if(attributeName == "f") {
					currentAlph.StartConvertCharacter.Foreground = tagAttributes.getValue(i);
				}
			}	
		}
		
		else if(tagName == "protect") {
			for(int i = 0; i < tagAttributes.getLength(); i++) {
				String attributeName = (tagAttributes.getLocalName(i).equals("") ? tagAttributes.getQName(i) : tagAttributes.getLocalName(i));
				if(attributeName == "d") {
					currentAlph.EndConvertCharacter.Display = tagAttributes.getValue(i);
				}
				if(attributeName == "t") {
					currentAlph.EndConvertCharacter.Text = tagAttributes.getValue(i);
				}
				if(attributeName == "b") {
					currentAlph.EndConvertCharacter.Colour = Integer.parseInt(tagAttributes.getValue(i));
				}
				if(attributeName == "f") {
					currentAlph.EndConvertCharacter.Foreground = tagAttributes.getValue(i);
				}
			}	
		}
		
		else if(tagName == "group") {
			
			currentGroup = new SGroupInfo();
			
			if(bFirstGroup) {
				currentGroup.bVisible = false;
				bFirstGroup = false;
			}
			else {
				currentGroup.bVisible = true;
			}
			
			currentGroup.strLabel = "";
			currentGroup.iColour = 0;
			
			for(int i = 0; i < tagAttributes.getLength(); i++) {
				String attributeName = (tagAttributes.getLocalName(i).equals("") ? tagAttributes.getQName(i) : tagAttributes.getLocalName(i));
				if(attributeName == "b") {
					currentGroup.iColour = Integer.parseInt(tagAttributes.getValue(i));
				}
				if(attributeName == "visible") {
					if(tagAttributes.getValue(i).equals("yes") || tagAttributes.getValue(i).equals("on")) {
						currentGroup.bVisible = true;
					}
					else if(tagAttributes.getValue(i).equals("no") || tagAttributes.getValue(i).equals("off")) {						
						currentGroup.bVisible = false;
					}
				}
				if(attributeName == "label") {
					currentGroup.strLabel = tagAttributes.getValue(i);
				}
			}
			
		    currentGroup.iStart = currentAlph.m_iCharacters;

		    currentGroup.Child = null;

		    if(currentAlph.m_vGroups.size() > 0) {
		      currentGroup.Next = currentAlph.m_vGroups.get(currentAlph.m_vGroups.size() - 1).Child;
		      currentAlph.m_vGroups.get(currentAlph.m_vGroups.size() - 1).Child = currentGroup;
		    }
		    else {
		      currentGroup.Next = currentAlph.m_BaseGroup;
		      currentAlph.m_BaseGroup = currentGroup;
		    }
		    
		    currentAlph.m_vGroups.add(currentGroup);
		}
				
		else if(tagName == "s") {
			CAlphIO.character newChar = new CAlphIO.character();
			newChar.Colour = -1;
			++currentAlph.m_iCharacters;
			
			for(int i = 0; i < tagAttributes.getLength(); i++) {
				String attributeName = (tagAttributes.getLocalName(i).equals("") ? tagAttributes.getQName(i) : tagAttributes.getLocalName(i));
				if(attributeName == "d") {
					newChar.Display = tagAttributes.getValue(i);
				}
				if(attributeName == "t") {
					newChar.Text = tagAttributes.getValue(i);
				}
				if(attributeName == "b") {
					newChar.Colour = Integer.parseInt(tagAttributes.getValue(i));
				}
				if(attributeName == "f") {
					newChar.Foreground = tagAttributes.getValue(i);
				}
			}
			
			currentAlph.m_vCharacters.add(newChar);
		}
	}
	
	public void endElement(String namespaceURI, String simpleName, String qualName) {
		String tagName = (simpleName.equals("") ? qualName : simpleName);
		
		if(tagName == "alphabet") {
			Alphs.put(currentAlph.AlphID, currentAlph);
		}
		
		else if(tagName == "palette") {
			currentTag = "";
		}
		
		else if(tagName == "train") {
			currentTag = "";
		}
		// Both of these are to prevent the parser from dumping unwanted CDATA
		// once the tags we're interested in have been closed.

		else if(tagName == "group") {
			currentAlph.m_vGroups.get(currentAlph.m_vGroups.size() - 1).iEnd = currentAlph.m_iCharacters;
			currentAlph.m_vGroups.remove(currentAlph.m_vGroups.get(currentAlph.m_vGroups.size() - 1));
		}
		

		
	}

	public void characters(char[] chars, int start, int length) throws SAXException {
		
		if(currentTag == "palette") {
			currentAlph.PreferredColours = new String(chars, start, length);
		}
		
		if(currentTag == "train") {
			currentAlph.TrainingFile = new String(chars, start, length);
		}
		
	}

	public InputSource resolveEntity(String publicName, String systemName) throws IOException, SAXException {
		
		if(systemName.contains("alphabet.dtd")) {
			try {
				return new InputSource(new FileInputStream(systemLoc + "alphabet.dtd"));
			}
			catch(Exception e) {
				try {
					return new InputSource(new FileInputStream(userLoc + "alphabet.dtd"));
				}
				catch(Exception ex) {
					return new InputSource(m_Parent.m_Interface.getResourceStream(systemLoc + "alphabet.dtd"));
				}
			}
		}
		else {
			return null;
		}
		
		
		/* CSFS: This is here because SAX will by default look in a system location
		 * first, which throws a security exception when running as an Applet.
		 */
	
	}
	
}
