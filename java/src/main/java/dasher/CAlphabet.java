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
import java.util.Collection;

/**
 * Represents an Alphabet, including a list of its symbols,
 * their appearance on screen and when typed, and their foreground
 * and background colours.
 * <p>
 * Note that the colours specified here are mere ordinal numbers;
 * the resolution of these into actual displayable colours is
 * the responsibility of CCustomColours.
 * <p>
 * This class is also responsible for translating a tract of text
 * into a sequence of logical symbol identifiers (see <code>
 * GetSymbols</code>)
 */

public class CAlphabet {
	
	/**
	 * This alphabet's default orientation. Valid values are
	 * specified by Opts.AlphabetTypes, and indicate
	 * left-to-right, right-to-left, top-to-bottom or
	 * bottom-to-top screen orientation.
	 */
	protected int m_Orientation;
	
	/**
	 * The symbol number of the new-paragraph character.
	 */
	protected int m_ParagraphSymbol;
	
	/**
	 * The symbol number of the space character.
	 */
	protected int m_SpaceSymbol;
	
	/**
	 * The symbol number of the control node.
	 * 
	 * Currently redundant as control mode is not implemented
	 * in Java.
	 */
	protected int m_ControlSymbol;
	
	//-- Added for Kanji Conversion 13 July 2005 by T.Kaburagi
	
	/**
	 * The symbol number of the Start Conversion symbol.
	 * 
	 * Current redundant as kanji conversion is not yet
	 * implemented in Java.
	 */
	protected int m_StartConversionSymbol;
	

	/**
	 * The symbol number of the End Conversion symbol.
	 * 
	 * Current redundant as kanji conversion is not yet
	 * implemented in Java.
	 */
	protected int m_EndConversionSymbol;
	
	/**
	 * Path of a file containing training text relevant
	 * to this alphabet.
	 */
	protected String m_TrainingFile;
	
	// Undocumented as the future of this isn't decided.
	protected String m_GameModeFile;
	
	/**
	 * Name of the colour scheme which this alphabet
	 * 'prefers' to use. This class does not enforce its
	 * use.
	 */
	protected String m_DefaultPalette;
		
	/**
	 * Symbols' representations when typed. These are Strings
	 * and not Characters because some alphabets may define
	 * multi-character symbols.
	 */
	protected ArrayList<String> m_Characters; // stores the characters
	
	/**
	 * Symbols' representations on screen. Usually this is the
	 * same as m_Characters, but some symbols are displayed
	 * with extra ornamentation on screen, such as the apostrophe
	 * in English, and most combining accent characters.
	 */
	protected ArrayList<String> m_Display;      // stores how the characters are visually represented in the Dasher nodes
	
	/**
	 * Symbols' background colours, used by CDasherView to display
	 * their nodes. These colour identifiers must be looked up
	 * by an instance of CCustomColours to retrieve actual RGB
	 * colours.
	 */
	protected ArrayList<Integer> m_Colours;       // stores the colour of the characters
	
	/**
	 * Stores the foreground colour of the symbols.
	 */
	protected ArrayList<String> m_Foreground;   // stores the colour of the character foreground
	
	/**
	 * Root of the group tree. Contains all other groups.
	 */
	public SGroupInfo m_BaseGroup = new SGroupInfo();
	
	/**
	 * Mapping from text to symbol numbers, for use in parsing
	 * training text.
	 */
	protected CAlphabetMap TextMap;
	
	/* CSFS: It would be good to fix the code duplication between these
	 * two constructors.
	 */
	
		
	/**
	 * Default constructor; produces an alphabet containing no
	 * characters. Characters may subsequently be added by
	 * calling AddChar. Special characters should be added
	 * by using AddParagraphSymbol, AddSpaceSymbol, and so forth.
	 */
	
	public CAlphabet() {
		m_Orientation = Opts.ScreenOrientations.LeftToRight;
		m_ControlSymbol = -1; 
				
		m_Characters = new ArrayList<String>();
		m_Display = new ArrayList<String>();
		m_Colours = new ArrayList<Integer>();
		m_Foreground = new ArrayList<String>();
		
		m_Characters.add("");
		m_Display.add("");
		m_Colours.add(-1);
		m_Foreground.add("");
		TextMap = new CAlphabetMap();
		
	}
	
	/**
	 * Produces a ready-made Alphabet from an AlphInfo class
	 * which specifies the alphabet. These are typically produced
	 * by an instance of CAlphIO by reading alphabet.*.xml files
	 * but may be producd by other means.
	 * <p>
	 * The alphabet produced will have special characters
	 * (such as the start conversion character) only if AlphabetInfo
	 * specifies their text as having non-zero length.
	 * 
	 * @param AlphabetInfo The AlphInfo class containing the
	 * 					   alphabet specification.
	 * @see CAlphIO
	 * @see CAlphIO.AlphInfo
	 */
	
	public CAlphabet(CAlphIO.AlphInfo AlphabetInfo) { 
		m_Orientation = Opts.ScreenOrientations.LeftToRight;
		m_ControlSymbol = (-1);
		
		int nSymbols = AlphabetInfo.m_iCharacters + 5;
		// +5 because of space, paragraph, control, start/end conversion.
		
		m_Characters = new ArrayList<String>(nSymbols);
		m_Display = new ArrayList<String>(nSymbols);
		m_Colours = new ArrayList<Integer>(nSymbols);
		m_Foreground = new ArrayList<String>(nSymbols);
		
		m_Characters.add("");
		m_Display.add("");
		m_Colours.add(-1);
		m_Foreground.add("");
		TextMap = new CAlphabetMap();
		
		m_StartConversionSymbol = -1;
		m_EndConversionSymbol = -1;
		
		// Set miscellaneous options
		
		SetOrientation(AlphabetInfo.Orientation);
		SetTrainingFile(AlphabetInfo.TrainingFile);
		SetGameModeFile(AlphabetInfo.GameModeFile);
		SetPalette(AlphabetInfo.PreferredColours);
		
				
		for(CAlphIO.character temp : AlphabetInfo.m_vCharacters) {
			AddChar(temp.Text, temp.Display, temp.Colour, temp.Foreground);
		}
			
		// Set Space character if requested
		
		if(AlphabetInfo.ParagraphCharacter.Text.length() != 0)
			AddParagraphSymbol(AlphabetInfo.ParagraphCharacter.Text, AlphabetInfo.ParagraphCharacter.Display, AlphabetInfo.ParagraphCharacter.Colour, AlphabetInfo.ParagraphCharacter.Foreground);
		
		
		if(AlphabetInfo.SpaceCharacter.Text.length() != 0)
			AddSpaceSymbol(AlphabetInfo.SpaceCharacter.Text, AlphabetInfo.SpaceCharacter.Display, AlphabetInfo.SpaceCharacter.Colour, AlphabetInfo.SpaceCharacter.Foreground);
		
		//-- Added for Kanji Conversion 13 July 2005 by T.Kaburagi START
		if(AlphabetInfo.StartConvertCharacter.Text.length() != 0)
			AddStartConversionSymbol(AlphabetInfo.StartConvertCharacter.Text, AlphabetInfo.StartConvertCharacter.Display, AlphabetInfo.StartConvertCharacter.Colour, AlphabetInfo.StartConvertCharacter.Foreground);
		
		if(AlphabetInfo.EndConvertCharacter.Text.length() != 0)
			AddEndConversionSymbol(AlphabetInfo.EndConvertCharacter.Text, AlphabetInfo.EndConvertCharacter.Display, AlphabetInfo.EndConvertCharacter.Colour, AlphabetInfo.EndConvertCharacter.Foreground);
		//-- Added for Kanji Conversion 13 July 2005 by T.Kaburagi END
		
		// DJW - now the control symbol is always a part of the alphabet
		// DasherModel knows whether or not to use it
		
		// FIXME - We really need to ensure that the control symbol is last in the alphabet with the current logic.
		
		if(AlphabetInfo.ControlCharacter.Display.length() != 0 && GetControlSymbol() == -1)
			AddControlSymbol(AlphabetInfo.ControlCharacter.Text, AlphabetInfo.ControlCharacter.Display, AlphabetInfo.ControlCharacter.Colour, AlphabetInfo.ControlCharacter.Foreground);
		
		// New group stuff
		
		m_BaseGroup = AlphabetInfo.m_BaseGroup;
		
	}
	
	/**
	 * Converts a string of text into a list of symbol indentifiers.
	 * Typically used in the course of training a language model,
	 * this fills the Symbols Collection with a seqeunce of integer
	 * symbol identifiers. Each character is individually
	 * used as a key into TextMap to determine its symbol number;
	 * as such their is no support for multi-character symbols
	 * at present. 
	 * 
	 * @param Symbols Collection to be filled with symbol identifiers.
	 * @param Input String to be converted.
	 * @param IsMore Redundant parameter; used to signal that the last
	 * 			 	 input character may be an incomplete UTF-8
	 * 				 character, but redundant since UTF-16 is now
	 * 				 used internally.
	 */
	
	public void GetSymbols(Collection<Integer> Symbols, String Input, boolean IsMore) {
		
		for(char c : Input.toCharArray()) {
			Symbols.add((TextMap.Get(new Character(c).toString())).symbol);
		}
	
	}
	
	/**
	 * Adds a new character to this alphabet. The new character will
	 * be assigned the next free symbol index.
	 * 
	 * @param NewCharacter Representation of this character as to be shown in textual output.
	 * @param Display Representation to be shown on screen.
	 * @param Colour Background colour index to be used in drawing nodes containing this character. 
	 * @param Foreground Foreground colour to be used drawing the text..
	 */
	
	public void AddChar(String NewCharacter, String Display, int Colour, String Foreground) {
		m_Characters.add(NewCharacter);
		m_Display.add(Display);
		m_Colours.add(Colour);
		m_Foreground.add(Foreground);
		
		int ThisSymbol = m_Characters.size() - 1;
		TextMap.Add(NewCharacter, ThisSymbol);
	}
	
	/**
	 * Adds a paragraph symbol to the alphabet. Since there is only
	 * one identifier for the paragraph symbol, if multiple
	 * paragraph symbols are added all but the latest will
	 * lose their status and will become ordinary symbols.
	 * 
	 * @param NewCharacter Representation of this character as to be shown in textual output.
	 * @param Display Representation to be shown on screen.
	 * @param Colour Background colour index to be used in drawing nodes containing this character. 
	 * @param Foreground Foreground colour to be used drawing the text..
	 */
	
	public void AddParagraphSymbol(String NewCharacter, String Display, int Colour, String Foreground) {
		AddChar(NewCharacter, Display, Colour, Foreground);
		m_ParagraphSymbol = GetNumberSymbols() - 1;
	}
	
	/**
	 * Adds a space symbol to the alphabet. Since there is only
	 * one identifier for the space symbol, if multiple space
	 * symbols are added all but the latest will lose their
	 * status and will become ordinary symbols.
	 * 
	 * @param NewCharacter Representation of this character as to be shown in textual output.
	 * @param Display Representation to be shown on screen.
	 * @param Colour Background colour index to be used in drawing nodes containing this character. 
	 * @param Foreground Foreground colour to be used drawing the text..
	 */
	
	public void AddSpaceSymbol(String NewCharacter, String Display, int Colour, String Foreground) {
		AddChar(NewCharacter, Display, Colour, Foreground);
		m_SpaceSymbol = GetNumberSymbols() - 1;
	}
	
	/**
	 * Adds a control symbol to the alphabet. Since there is only
	 * one identifier for the control symbol, if multiple control
	 * symbols are added all but the latest will lose their
	 * status and will become ordinary symbols.
	 * 
	 * @param NewCharacter Redundant; the Control character is not typed.
	 * @param Display Representation to be shown on screen. (typically 'Control')
	 * @param Colour Background colour index to be used in drawing nodes containing this character. 
	 * @param Foreground Foreground colour to be used drawing the text..
	 */
	
	public void AddControlSymbol(String NewCharacter, String Display, int Colour, String Foreground) {
		AddChar(NewCharacter, Display, Colour, Foreground);
		m_ControlSymbol = GetNumberSymbols() - 1;
	}
	
	/**
	 * Adds a start-conversion symbol top the alphabet. Since there is only
	 * one identifier for the symbol, if multiple start-conversion
	 * symbols are added all but the latest will lose their
	 * status and will become ordinary symbols.
	 * 
	 * @param NewCharacter Redundant; the conversion symbol is not a typed character in the ordinary sense.
	 * @param Display Representation to be shown on screen.
	 * @param Colour Background colour index to be used in drawing nodes containing this character. 
	 * @param Foreground Foreground colour to be used drawing the text..
	 */	
	
	public void AddStartConversionSymbol(String NewCharacter, String Display, int Colour, String Foreground) {
		AddChar(NewCharacter, Display, Colour, Foreground);
		m_StartConversionSymbol = GetNumberSymbols() - 1;
	}
	
	/**
	 * Adds an end-conversion symbol top the alphabet. Since there is only
	 * one identifier for the end-conversion symbol, if multiple end-conversion
	 * symbols are added all but the latest will lose their
	 * status and will become ordinary symbols.
	 * 
	 * @param NewCharacter Redundant; the end-conversion symbol is not typed in the ordinary sense.
	 * @param Display Representation to be shown on screen.
	 * @param Colour Background colour index to be used in drawing nodes containing this character. 
	 * @param Foreground Foreground colour to be used drawing the text..
	 */
	
	public void AddEndConversionSymbol(String NewCharacter, String Display, int Colour, String Foreground) {
		AddChar(NewCharacter, Display, Colour, Foreground);
		m_EndConversionSymbol = GetNumberSymbols() - 1;
	}

	/**
	 * Retrieves the text colour to be used in drawing the specified
	 * symbol. In the case that a foreground colour was not specified
	 * (and so the m_Foreground list contains "" at this index)
	 * the default colour 4 is returned.
	 * 
	 * @param Symbol Symbol whose colour is to be looked up.
	 * @return Colour identifier to be used drawing this text.
	 */
	
	public int GetTextColour(int Symbol) {
		String TextColour = m_Foreground.get(Symbol);
		if(!"".equals(TextColour)) {
			return (Integer.parseInt(TextColour)); // CSFS: This used to use atoi.
		}
		else {
			return 4;
		}
	}
	
	/**
	 * Gets number of symbols in this alphabet, including special characters.
	 * 
	 * @return Symbol count in this Alphabet.
	 */
	
	public int GetNumberSymbols() {
		return m_Characters.size();
	}
	
	/**
	 * Gets the number of symbols in this alphabet which are not
	 * 'special symbols' (eg the Control symbol). In reality
	 * this is the same as GetNumberSymbols() - 1.
	 * <p>
	 * This method needs improving to correctly recognise the
	 * start conversion symbol.
	 * 
	 * @return Textual symbol count in this alphabet.
	 */
	 
	public int GetNumberTextSymbols() {
		return m_Characters.size() - 1;
	} 
	
	/**
	 * Gets the orientation associated with this alphabet.
	 * Allowable values are enumerated by Opts.ScreenOrientations.
	 * 
	 * @return This Alphabet's preferred orientation. 
	 */
	
	public int GetOrientation() {
		return m_Orientation;
	}
	
	/**
	 * Gets the training file to be used training a language
	 * model which uses this alphabet.
	 * 
	 * @return Path to the specified training file.
	 */
	
	public String GetTrainingFile() {
		return m_TrainingFile;
	}
	
	// Undocumented pending changes to this.
	
	public String GetGameModeFile() {
		return m_GameModeFile;
	}
	
	/**
	 * Gets the name of the colour scheme preferred by this
	 * alphabet.
	 * 
	 * @return Preferred colour scheme name.
	 */
	
	public String GetPalette() {
		return m_DefaultPalette;
	}
	
	/**
	 * Gets the array of display texts. This should not be used
	 * to make changes to the alphabet.
	 * 
	 * @return Reference to the array of characters' display texts.
	 */
	
	public ArrayList<String> GetDisplayTexts() {
		return m_Display;
		// Another method for efficiency, allowing an outside class to
		// get a handle to our display text list. This shouldn't be bad
		// as they only get a reference and do not copy data.
	}
	
	/**
	 * Retrieves the String which should be displayed to represent a given character.
	 * 
	 * @param i Index of the character to be looked up.
	 * @return Display string for this character.
	 */
	
	public String GetDisplayText(int i) {
		return m_Display.get(i);
	}
	
	/**
	 * Retrieves the typed character representation of a given symbol.
	 * 
	 * @param i Symbol to look up
	 * @return Character which should be typed in the edit box (for example) to represent it.
	 */
	
	public String GetText(int i) {
		return m_Characters.get(i);
	} 
	
	/**
	 * Gets a reference to the array of background colours used
	 * when drawing symbols. The returned array should not be
	 * modified, and GetColour(i) should be used where possible;
	 * this is for increased efficiency when iteration over the
	 * array is desired.
	 * 
	 * @return Array of background colours.
	 */
	
	public ArrayList<Integer> GetColours() {
		// Allows another class to obtain a reference to the colour map.
		// This is in order to increase the efficiency of PopulateChildrenWithSymbol.
		
		return m_Colours;
	}
	
	/**
	 * Retrieves the background colour for a given symbol.
	 * 
	 * @param i Symbol indentifier
	 * @return Background colour for this symbol.
	 */
	
	public int GetColour(int i) {
		return m_Colours.get(i);
	}

	/**
	 * Retrieves the foreground (text) colour for a given symbol.
	 * 
	 * @param i Symbol identifier.
	 * @return Foreground colour for this symbol.
	 */
	
	public String GetForeground(int i) {
	      return m_Foreground.get(i);
	} 

	/**
	 * Sets the preferred orientation for this alphabet.
	 * 
	 * @param Orientation New orientation.
	 */
	
	public void SetOrientation(int Orientation) {
		m_Orientation = Orientation;
	}
		
	/**
	 * Sets the training file to be used training a language
	 * model which uses this alphabet.
	 * 
	 * @param TrainingFile New training file location.
	 */
	
	public void SetTrainingFile(String TrainingFile) {
		m_TrainingFile = TrainingFile;
	}
	
	// Undocumented pending changes.
	
	public void SetGameModeFile(String GameModeFile) {
		m_GameModeFile = GameModeFile;
	}
	
	/**
	 * Sets this alphabet's preferred colour scheme.
	 * 
	 * @param Palette Name of the new scheme.
	 */
	
	public void SetPalette(String Palette) {
		m_DefaultPalette = Palette;
	}
	
	/**
	 * Gets the instance of CAlphabetMap which maps Strings
	 * to symbols in this Alphabet.
	 * 
	 * @return TextMap
	 */
	
	public CAlphabetMap GetAlphabetMap() {
		return TextMap;
	}
	
	/**
	 * Gets the index of this alphabet's paragraph symbol.
	 * 
	 * @return index of paragraph symbol.
	 */
	
	public int GetParagraphSymbol()  {
		return m_ParagraphSymbol;
	}
	
	/**
	 * Gets the index of this alphabet's space symbol.
	 * 
	 * @return index of space symbol.
	 */
	
	public int GetSpaceSymbol()  {
		return m_SpaceSymbol;
	}
	
	/**
	 * Gets the index of this alphabet's control symbol.
	 * 
	 * @return index of control symbol, or -1 if there is none.
	 */
	
	public int GetControlSymbol()  {
		return m_ControlSymbol;
	}
	
	/**
	 * Gets the index of this alphabet's start-conversion symbol.
	 * 
	 * @return index of start-conversion symbol, or -1 if there is none.
	 */
	
	public int GetStartConversionSymbol()  {
		return m_StartConversionSymbol;
	}
	
	/**
	 * Gets the index of this alphabet's end-conversion symbol.
	 * 
	 * @return index of end-conversion symbol, or -1 if there is none.
	 */
	
	public int GetEndConversionSymbol()  {
		return m_EndConversionSymbol;
	}
	
}
