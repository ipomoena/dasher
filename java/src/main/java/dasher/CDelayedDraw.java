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

/**
 * Simple class permitting the delayed drawing of Strings when
 * drawing the DasherNodes to the screen.
 * <p>
 * The idea is that we can both<br>
 * a) Cause all text to appear above other drawn objects, and
 * b) Draw text at the same time as its Node, allowing the
 * drawing process to remain modular.
 * <p>
 * Essentially, all strings which are drawn with this class are
 * added to a list, which is emptied and drawn when Draw is called.
 */
public class CDelayedDraw {

	/**
	 * Queues up a piece of text for drawing. 
	 * 
	 * @param str String to draw
	 * @param x1 x co-ordinate of top-left corner of the string's bounding box
	 * @param y1 y co-ordinate of top-left corner of the string's bounding box
	 * @param Size Font size to use
	 */
	public void DelayDrawText(String str, int x1, int y1, long Size) {
		m_DrawTextString.add(new CTextString(str, x1, y1, Size));
	}
	
	/**
	 * Draws all queued up text to a given screen, and clears the queue.
	 *  
	 * @param screen Screen to which we should draw
	 */
	public void Draw(CDasherScreen screen) {
		int iSize = m_DrawTextString.size();
		
		for(int i = 0; i < iSize; i++) {
			CTextString draw = m_DrawTextString.get(i);
			screen.DrawString(draw.m_String, draw.m_x, draw.m_y, draw.m_iSize);
		}
		m_DrawTextString.clear();
		
	}
			
	/**
	 * Internal list of objects we are waiting to draw
	 */
	protected ArrayList <CTextString> m_DrawTextString = new ArrayList<CTextString>();
		
	/* CSFS: The class CTextSymbol and its associated ArrayList has been removed
	 * since it was redundant according to Eclipse.
	 */
	
	/**
	 * Class representing a String to be drawn plus information
	 * needed to draw it
	 */
	protected class CTextString {
		
		/**
		 * Default constructor
		 * 
		 * @param str String to draw
		 * @param x x co-ordinate of top-left corner of the string's bounding box
		 * @param y y co-ordinate of top-left corner of the string's bounding box
		 * @param iSize font size
		 */
		public CTextString(String str, int x, int y, long iSize) {
		
			m_String = (str);
			m_x = (x);
			m_y = (y);
			m_iSize = (iSize);
						
		} 
		/**
		 * String to be drawn
		 */
		String m_String;
		/**
		 * x co-ordinate of top-left corner of the string's bounding box
		 */
		int m_x;
		/**
		 * y co-ordinate of top-left corner of the string's bounding box
		 */
		int m_y;
		/**
		 * Font size
		 */
		long m_iSize;
	}
		
}
