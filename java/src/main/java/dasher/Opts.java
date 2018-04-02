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
 * List of miscellaneous constants, some of which are probably
 * now redundant, inclduing those describing encodings, as Java
 * will always use UTF16 internally.
 */
public class Opts {
	
	// Encodings
	
	public static final int UserDefault = -1; 
	public static final int AlphabetDefault = -2; 
	public static final int UTF8 = 65001;
	public static final int UTF16LE = 1200;
	public static final int UTF16BE = 1201;
	
	/**
	 * List of available screen orientations. The "Alphabet"
	 * option means that the Alphabet's preferred orientation
	 * is used.
	 */
	class ScreenOrientations {
		public static final int Alphabet = -2; 
		public static final int LeftToRight = 0;
		public static final int RightToLeft = 1;
		public static final int TopToBottom = 2;
		public static final int BottomToTop = 3;
	}
	
	/**
	 * This appears to enumerate codepages, which is also very
	 * much useless in Java.
	 */
	class AlphabetTypes { 
		public static final int MyNone = 0;
		public static final int Arabic = 1256;
		public static final int Baltic = 1257;
		public static final int CentralEurope = 1250;
		public static final int ChineseSimplified = 936;
		public static final int ChineseTraditional = 950;
		public static final int Cyrillic = 1251;
		public static final int Greek = 1253;
		public static final int Hebrew = 1255;
		public static final int Japanese = 932;
		public static final int Korean = 949;
		public static final int Thai = 874;
		public static final int Turkish = 1254;
		public static final int VietNam = 1258;
		public static final int Western = 1252; }
}
