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

import java.awt.*;
import java.util.HashMap;
import java.awt.geom.Rectangle2D;

import dasher.CCustomColours;
import dasher.CDasherView;

import dasher.EColorSchemes;

/**
 * This implementation of JDasherScreen is an extension of the Swing
 * basic object JPanel, and works by drawing a frame of Dasher onto
 * the panel whenever its paintComponent method is called.
 * <p>
 * For a general description of the Screen's contract and specifications of
 * what its methods ought to do, see CDasherScreen. Only details of the implementation
 * will be noted here.
 */
public class JDasherScreen implements dasher.CDasherScreen {

	/**
	 * Graphics context in which to draw 
	 */
	private Graphics paint;
	
	/**
	 * Dasher instance which draws onto this Screen
	 */
	private JDasher m_Dasher;
	
	/**
	 * Custom colour scheme against which colour indices are resolved
	 */
	private CCustomColours m_Colours;
		
	/**
	 * Map of known sizes of differing characters at different font sizes.
	 */
	private HashMap<TextSize, CDasherView.Point> TextSizes;
	
	/**
	 * Map from font sizes to the Font objects used to draw them.
	 */
	private HashMap<Long, DasherFont> DrawFonts;
	
	/**
	 * Width of this screen in pixels
	 */
	protected int m_iWidth;
	
	/**
	 * Height of this screen in pixels
	 */
	protected int m_iHeight;
	
	/**
	 * Creates a new Screen tied to a given Dasher and with given dimensions
	 * 
	 * @param Dasher Dasher which is displaying using this screen.
	 * @param width Width of this screen
	 * @param height Height of this screen
	 */
	public JDasherScreen(JDasher Dasher, int width, int height) {
		
		m_Dasher = Dasher;
		
		/* We need this back-reference so that we can request new frames to be drawn
		 * when a paint event arises.
		 */
		
		m_iWidth = width;
		m_iHeight = height;
				
		TextSizes = new HashMap<TextSize, CDasherView.Point>();
		DrawFonts = new HashMap<Long, DasherFont>();
		
	}
	
	
	public int GetWidth() {
		
		return m_iWidth;
		
	}
	
	
	public int GetHeight() {
		
		return m_iHeight;
		
	}
	
	/**
	 * Clears the screen using the clearRect method
	 */
	public void Blank() {
		
		paint.setColor(Color.WHITE);
		paint.fillRect(0,0,m_iWidth, m_iHeight);
	}

	public void setSize(int width, int height) {
		m_iWidth = width;
		m_iHeight = height;
	}
	
	/**
	 * Method ignored; as we're drawing to Swing's provided surface, the actual displaying of the image will
	 * be taken care of for us.
	 */
	public void Display() {
		/* No need to do anything; we've been drawing to the content
		 * surface all along, and Swing will take care of showing it.
		 */
	}

	public void DrawCircle(int iCX, int iCY, int iR, int iColour, boolean bFill) {
		
		/* CSFS: This is probably still wrong for reasons described in
		 * DrawRectangle. I've left it alone for the time being since
		 * it's very rarely used by Dasher.
		 */
		
		setColour(iColour);
		
		if(bFill) {
			paint.fillOval(iCX - iR, iCY - iR, iR, iR);		
		}
		else {
			paint.drawOval(iCX - iR, iCY - iR, iR, iR);
		}
	}

	
	/** Dasher specifies its co-ordinates like
	 * <pre>
	 * y2---------x2
	 * |           |
	 * |           |
	 * x1,y1-------/</pre>
	 * <p>Whereas Java wants
	 * <pre>
	 * 			width
	 * x,y---------------\
	 * |				 |
	 * |				 | height
	 * |				 |
	 * \-----------------/
	 * </pre><p>
	 * Therefore, I use (x1, y2) as the point to feed to Java, and calculate
	 * height and width.
	 * 
	 */
	
	public void DrawRectangle(int x1, int y1, int x2, int y2, int Color, int iOutlineColour, EColorSchemes ColorScheme, boolean bDrawOutline, boolean bFill, int iThickness) {
				
		if(bFill) {
			setColour(Color);
			paint.fillRect(x1, y2, x2 - x1, y1 - y2);
		}
		
		if(bDrawOutline) {
			
			setColour(iOutlineColour);

			paint.drawRect(x1, y2, x2 - x1, y1 - y2); // xtop, yleft, width, height
		}

		
	}

	
	/** Here be more trouble! Dasher is specifying its strings by
	 * the co-ordinates of the top-left corner of a rectangle in which
	 * the text will be drawn. So,
	 * <pre>
	 *   x,y------------\
	 *   | Some String  |
	 *   \--------------/
	 *  </pre><p>
	 *   Java's DrawString method however specifies the baseline of the
	 *   first character, which is the bottom in the case of non-descenders
	 *   such as a and b, but is not for descenders such as g.
	 *   <p>
	 *   Therefore we must figure out the height and adjust x appropriately
	 *   before drawing the string.
	 *   <p>
	 *   It's important to note also that some work in this direction is done
	 *   by DasherView calling the Screen's TextSize method. This supplies
	 *   a height and width of a given string in pixels, using a HashMap to
	 *   do so efficiently.
	 *
	 */
	
	public void DrawString(String string, int x1, int y1, long Size) {
				
		
		int thisOffset;
		
		if(DrawFonts.containsKey(Size)) {
			paint.setFont(DrawFonts.get(Size).font);
			thisOffset = DrawFonts.get(Size).drawOffset;
		}
		else {
			Font newFont = new Font("sans", 0, (int)Size);
			paint.setFont(newFont);
			FontMetrics fm = paint.getFontMetrics();
			thisOffset = fm.getAscent();
			
			DasherFont newDasherFont = new DasherFont(newFont, thisOffset, (int)Size);
			
			DrawFonts.put(Size, newDasherFont);
		}
			
		
		
		/* CSFS: Since it is necessary to generate lots of fonts in the course
		 * of Dashing AND I need to store a drawing offset for each one, 
		 * (although I may address this later by modifying the drawing code),
		 * I've adapted the hashmap method found in the GetFont method in
		 * Screen.inl to save work.
		 */
				
		paint.setColor(Color.BLACK);
		paint.drawString(string, x1, y1 + (thisOffset / 2));
	}
	
	
	
	public void Polygon(CDasherView.Point[] Points, int Number, int Color) {
		
		Polygon(Points, Number, Color, 0);
		
	}

	public void Polygon(CDasherView.Point[] Points, int Number, int Color, int iWidth) {
		
		setColour(Color);
		int[] xs = new int[Points.length];
		int[] ys = new int[Points.length];
		for(int i = 0; i < xs.length; i++) {
			xs[i] = Points[i].x;
			ys[i] = Points[i].y;
		}
		
		paint.fillPolygon(xs, ys, xs.length);
		
		
	}

	
	
	public void Polyline(CDasherView.Point[] Points, int Number, int iWidth) {
		
		Polyline(Points, Number, iWidth, 0);
		
	}

	public void Polyline(CDasherView.Point[] Points, int Number, int iWidth, int Colour) {
		
		setColour(Colour);
		int[] xs = new int[Points.length];
		int[] ys = new int[Points.length];
		for(int i = 0; i < xs.length; i++) {
			xs[i] = Points[i].x;
			ys[i] = Points[i].y;
		}
		
		paint.drawPolygon(xs, ys, xs.length);
		
		
	}

	
	public void SetColourScheme(CCustomColours ColourScheme) {
		
		m_Colours = ColourScheme;
		
	}

	
	/**
	 * Graphics.getFontMetrics().getStringBounds is used to determine a probable text size.
	 * <p>
	 * Results, referenced by a struct containing information about both the character and font size concerned,
	 * are stored in a HashMap for quick access in the future.
	 * <p>
	 * At present, StringBounds' returned answer is augmented by one pixel in the x direction in the interests
	 * of readability.
	 * 
	 * @param string String whose size we want to determine
	 * @param Size Font size to use
	 * 
	 * @return Point defining its size.
	 */
	public CDasherView.Point TextSize(String string, int Size) {
		
		TextSize testValue = new TextSize();
		testValue.glyph = string;
		testValue.size = Size;
		
		if(TextSizes.containsKey(testValue)) {
			return TextSizes.get(testValue);
		}
		else {
			paint.setFont(paint.getFont().deriveFont((float)Size));
			Rectangle2D newsize = paint.getFontMetrics().getStringBounds(string ,paint);
			CDasherView.Point newpoint = new CDasherView.Point();
			newpoint.x = (int)newsize.getWidth() + 1;
			newpoint.y = (int)newsize.getHeight();
			
			TextSizes.put(testValue, newpoint);
			
			// System.out.printf("Glyph %s at size %d (%d) has dimensions (%dx%d)%n", string, Size, paint.getFont().getSize(), newpoint.x, newpoint.y);
	
			return newpoint;
			
			
		}
		
	}

	/**
	 * Sets the current Graphics context's colour by querying m_Colours for its RGB values.
	 * <p>
	 * A colour of -1 is read as being the same as colour 3.
	 * 
	 * @param iColour Colour to set
	 */
	private void setColour(int iColour) {
		if(iColour == -1) iColour = 3; // Special value used in Dasher, seems to mean 3.
		paint.setColor(new Color(m_Colours.GetRed(iColour), m_Colours.GetGreen(iColour), m_Colours.GetBlue(iColour)));
	}

	public void SendMarker(int iMarker) {
	
		// Stub: This method is, for the time being, useless since Display() and Blank() serve the same purpose.
		
	}

	/**
	 * Sets our Graphics context and calls NewFrame; Dasher will do the rest.
	 */
	public void drawToComponent(Graphics g) {
		
		paint = g;
		m_Dasher.NewFrame(System.currentTimeMillis());
		
	}

}

/**
 * Small struct representing a Font and detailing both its size
 * and the offset required to draw it using top-left co-ordinates
 * as opposed to Java's baseline co-ordinates.
 */
class DasherFont {
	/**
	 * Font
	 */
	Font font;
	
	/**
	 * Vertical offset from baseline to top-left corner in this font
	 */
	int drawOffset;
	
	/**
	 * Font size
	 */
	int size;
	
	public DasherFont(Font i_Font, int i_drawOffset, int iSize) {
		font = i_Font;
		drawOffset = i_drawOffset;
		size = iSize;
	}
}

/**
 * Small struct used to store a glyph and font-size pair in the
 * character-to-drawn-size map.
 */
class TextSize {
	/**
	 * Character(s)
	 */
	String glyph;
	
	/**
	 * Font size
	 */
	int size;
	
	/**
	 * Returns true if the String and font size both match.
	 */
	public boolean equals(Object otherone) {
		if(otherone == null) {
			return false;
		}
		if(otherone instanceof TextSize) {
			TextSize theother = (TextSize)otherone;
			return (this.glyph.equals(theother.glyph) && this.size == theother.size);
		}
		else {
			return false;
		}
	}
	
	/**
	 * Overridden to use the String's hashCode plus the size.
	 */
	public int hashCode() {
		return this.glyph.hashCode() + size;
	}
}
