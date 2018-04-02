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
 * Dasher views represent the visualisation of a Dasher model on the screen.
 * <p>
 * There are really three roles played by CDasherView: providing high
 * level drawing functions, providing a mapping between Dasher
 * co-ordinates and screen co-ordinates and providing a mapping
 * between true and effective Dasher co-ordinates (eg for eyetracking
 * mode).
 * <p>
 * This class supplies only those functions which are independent
 * of the way in which we choose to represent the Model; the specifics
 * are decided in subclasses which decide exactly how a node should
 * be rendered. At present, the only subclass is DasherViewSquare
 * which draws square boxes for each node.
 * <p>
 * Most actual drawing functions will be passed on to an implementation
 * of CDasherScreen.
 */
public abstract class CDasherView extends CDasherComponent {
	

	/**
	 * Class representing a point on the screen.
	 */
	static public class Point {
		/**
		 * X co-ord
		 */
		public int x;
		/**
		 * Y co-ord
		 */
		public int y;
	}

	/*
	 * Class representing a point in Dasher space
	 */
	static public class DPoint {
		/**
		 * X co-ord
		 */
		public long x;
		/**
		 * Y co-ord
		 */
		public long y;
	}

	/**
	 * Rectangle in Dasher space
	 */
	static public class DRect {
		/**
		 * Larger y co-ord
		 */
		public long maxY;
		/**
		 * Smaller y co-ord
		 */
		public long minY;
		/**
		 * Smaller x co-ord
		 */
		public long minX;
		/**
		 * Larger x co-ord
		 */
		public long maxX;
	}

	/**
	 * Flag indicating whether we need to update our impression
	 * of the region in Dasher space which is currently visible.
	 */
	protected boolean m_bVisibleRegionValid;
	
	/**
	 * Screen we're currently using for drawing
	 */
	protected CDasherScreen m_Screen;
	
	/**
	 * Current input device
	 */
	protected CDasherInput m_Input;
	
	/**
	 * Delayed drawing helper. This allows us to draw Strings
	 * at the natural time, with the helper storing them and
	 * actually drawing all strings last.
	 */
	protected CDelayedDraw m_DelayDraw;
	
	/**
	 * Stores the current value of LP_MAX_Y for efficiency.
	 * <p>
	 * We listen for parameter change events to update this
	 * when necessary.
	 */
	protected long lpMaxY; // Caching result for dashery2screen
	
	/**
	 * Stores the current value of LP_REAL_ORIENTATION again because
	 * this parameter is requested many times during drawing, which
	 * can become exceedingly inefficient.
	 */
	protected int realOrientation; // Caching result for dasher2screen
	
	/**
	 * Current font size used for drawing.
	 */
	protected int lpFontSize;
	
	/**
	 * Creates a new View wrapping a specified screen. The Screen
	 * passed must not be null; as such, the View must be created
	 * after a Screen has been instantiated.
	 * <p>
	 * To this ends, the View will usually be created in response
	 * to the first call to InterfaceBase.ChangeScreen().
	 * <p>
	 * This constructor will also initialise fields which cache
	 * parameter values, which will subsequently be kept up to
	 * date by listening for parameter change events.
	 * 
	 * @param EventHandler Event handler with which to register ourselves
	 * @param SettingsStore Settings repository to use 
	 * @param DasherScreen Screen object to wrap.
	 */
	public CDasherView(CEventHandler EventHandler, CSettingsStore SettingsStore, CDasherScreen DasherScreen) {
		super(EventHandler, SettingsStore);
		m_Screen = DasherScreen;
		m_Input = null;
		
		lpMaxY = SettingsStore.GetLongParameter(Elp_parameters.LP_MAX_Y);
		realOrientation = (int)SettingsStore.GetLongParameter(Elp_parameters.LP_REAL_ORIENTATION);
		lpFontSize = (int)SettingsStore.GetLongParameter(Elp_parameters.LP_DASHER_FONTSIZE);
		
		// Value caching
	}
	
	/**
	 * This class responds to the following events:
	 * <p>
	 * <i>LP_REAL_ORIENTATION, LP_MAX_Y and LP_DASHER_FONTSIZE</i>:
	 * Updates our internally cached values of these parameters.
	 * <p>
	 * This method is called by the EventHandler when processing
	 * events.
	 * 
	 * @param event Event to handle
	 */
	public void HandleEvent(CEvent event) {
		if(event.m_iEventType == 1) { // Param-notify
			CParameterNotificationEvent evt = (CParameterNotificationEvent)event;
			if(evt.m_iParameter ==  Elp_parameters.LP_REAL_ORIENTATION) {
				realOrientation = (int)GetLongParameter(Elp_parameters.LP_REAL_ORIENTATION);
			}
			else if (evt.m_iParameter == Elp_parameters.LP_MAX_Y) {
				lpMaxY = GetLongParameter(Elp_parameters.LP_MAX_Y);
			}
			else if (evt.m_iParameter == Elp_parameters.LP_DASHER_FONTSIZE) {
				lpFontSize = (int)GetLongParameter(Elp_parameters.LP_DASHER_FONTSIZE);
			}
		}
	}
	
	/**
	 * Sets our screen. New drawing instructions will immediately
	 * being to be sent to the new one.
	 * 
	 * @param NewScreen New screen
	 */
	public void ChangeScreen(CDasherScreen NewScreen) {
		m_Screen = NewScreen;
	}
	
	/**
	 * Renders the entire model, starting at a given Node and given
	 * specific bounds in Dasher space.
	 * <p>
	 * At present this just passes all of its parameters to RenderNodes
	 * with the exception of bRedrawDisplay, which is ignored (the
	 * display is always redrawn).
	 * 
	 * @param Root Node at which to start the drawing process
	 * @param iRootMin Minimum bounding co-ordinate in Dasher space
	 * @param iRootMax Maximum bounding co-ordinate in Dasher space
	 * @param vNodeList Collection which will be filled with Nodes which were drawn
	 * @param vDeleteList Collection which will be filled with Nodes which were not drawn
	 * @param bRedrawDisplay Flag indicating whether we wish for a redraw (ignored; always redraws)
	 * @return True if a redraw took place (always the case at the moment) 
	 */
	public boolean Render(CDasherNode Root, long iRootMin, long iRootMax, Collection<CDasherNode> vNodeList, Collection<CDasherNode> vDeleteList, boolean bRedrawDisplay) {
		
		RenderNodes(Root, iRootMin, iRootMax, vNodeList, vDeleteList);
		return true;
	}
	
	/**
	 * Deferred to m_Input.
	 * 
	 * @see CDasherInput
	 * @return Number of co-ordinates, or 0 if there is no input device.
	 */
	public int GetCoordinateCount() {
		// TODO: Do we really need support for co-ordinate counts other than 2?
		if(m_Input != null)
			return m_Input.GetCoordinateCount();
			else
				return 0;
	}
	
	/**
	 * Deferred to m_Input
	 * 
	 * @param iN Number of co-ordinates
	 * @param Coordinates Array to fill with co-ordinates
	 * @return 0 if the returned co-ordinates are screen co-ordinates,
	 * 1 if Dasher co-ordinates. 0 will also be returned if there
	 * is currently no input device. 
	 */
	public int GetCoordinates(int iN, long[] Coordinates) {
		if(m_Input != null)
			return m_Input.GetCoordinates(iN, Coordinates);
			else
				return 0;
	}
	
	/**
	 * Determines whether the node falling between two specified
	 * y co-ordinates is visible.
	 * <p>
	 * Always returns true if not overridden.
	 * 
	 * @param y1 upper y co-ordinate
	 * @param y2 lower y co-ordinate
	 * @return True if visible, false if not.
	 */
	public boolean IsNodeVisible(long y1, long y2) { return true; };
	
	/**
	 * Sets our current reference input device and informs the
	 * new device of our maximal co-ordinates.
	 * <p>
	 * The input device will not be dereferenced or deleted, as
	 * the caller may be intending for the input device to
	 * switch frequently without destroying one which is 
	 * nominally out of use.
	 * 
	 * @param Input New input device
	 */
	public void SetInput(CDasherInput Input) {
		// TODO: Is it sensible to make this responsible for the input
		// device - I guess it makes sense for now
		
		// Don't delete the old input class; whoever is calling this method
		// might want to keep several Input class instances around and
		// change which one is currently driving dasher without deleting any
		
		m_Input = Input;
		
		// Tell the new object about maximum values
		
		long[] iMaxCoordinates = new long[2];
		
		iMaxCoordinates[0] = lpMaxY;
		iMaxCoordinates[1] = lpMaxY;
		
		m_Input.SetMaxCoordinates(2, iMaxCoordinates);
	}
	
	/**
	 * Deferred to our screen.
	 *
	 * @see CDasherScreen
	 */
	public void Display() {
		m_Screen.Display();
	}
	
	/**
	 * Draws a polyline given a series of points in Dasher space.
	 * <p>
	 * Internally, we convert these to screen co-ordinates and pass
	 * the request on to our screen.
	 * <p>
	 * Specifying a line colour of -1 causes the default to be drawn.
	 * 
	 * @param x Array of points' x co-ordinates
	 * @param y Array of points' y co-ordinates
	 * @param n Number of points in the line
	 * @param iWidth Line width
	 * @param iColour Colour index (-1 means default colour)
	 */
	public void DasherPolyline(long[] x, long[] y, int n, int iWidth, int iColour) {
		
		CDasherView.Point[] ScreenPoints = new CDasherView.Point[n];
		
		for(int i = (0); i < n; ++i)
			ScreenPoints[i] = Dasher2Screen(x[i], y[i]);
		
		if(iColour != -1) {
			Screen().Polyline(ScreenPoints, n, iWidth, iColour);
		}
		else {
			Screen().Polyline(ScreenPoints, n, iWidth);
		}
	}
	
//	Draw a filled polygon specified in Dasher co-ordinates
	
	/**
	 * Draws a filled polygon given a series of points in Dasher space.
	 * <p>
	 * Internally, we convert these to screen co-ordinates and pass
	 * the request on to our screen.
	 * <p>
	 * Specifying a colour of -1 is not handled specially by
	 * this method but ought to be translated to some sensible
	 * default by the Screen.
	 * 
	 * @param x Array of points' x co-ordinates
	 * @param y Array of points' y co-ordinates
	 * @param n Number of points specified
	 * @param iColour Colour index (-1 means default colour)
	 */
	public void DasherPolygon(long[] x, long[] y, int n, int iColour) {
		
		CDasherView.Point[] ScreenPoints = new CDasherView.Point[n];
		
		for(int i = (0); i < n; ++i)
			ScreenPoints[i] = Dasher2Screen(x[i], y[i]);
		
		Screen().Polygon(ScreenPoints, n, iColour);
	}
	

	/**
	 * Draws a rectangle given its top-left and bottom-right co-ordinates
	 * in Dasher space.
	 * <p>
	 * The co-ordinates are translated to screen co-ordinates and
	 * the request passed on to the screen.
	 * 
	 * @param iLeft Left side x co-ordinate
	 * @param iTop Top y co-ordinate
	 * @param iRight Right side x co-ordinate
	 * @param iBottom Bottom y co-ordinate
	 * @param Color Fill colour
	 * @param iOutlineColour Outline colour
	 * @param ColorScheme Colour scheme to use (usually ignored in favour of colour indices now)
	 * @param bDrawOutline Should we draw an outline?
	 * @param bFill Filled?
	 * @param iThickness Outline thickness
	 */	
	public void DasherDrawRectangle(long iLeft, long iTop, long iRight, long iBottom, int Color, int iOutlineColour, EColorSchemes ColorScheme, boolean bDrawOutline, boolean bFill, int iThickness) {
		
		CDasherView.Point topleft = new CDasherView.Point();
		CDasherView.Point bottomright = new CDasherView.Point();
		
		topleft = Dasher2Screen(iLeft, iTop);
		bottomright = Dasher2Screen(iRight, iBottom);
		
		Screen().DrawRectangle(topleft.x, topleft.y, bottomright.x, bottomright.y, Color, iOutlineColour, ColorScheme, bDrawOutline, bFill, iThickness);
	}
	
//	/ Draw a rectangle centred on a given dasher co-ordinate, but with a size specified in screen co-ordinates (used for drawing the mouse blob)
	
	/**
	 * Draws a square with a given centre in Dasher co-ordinates
	 * and a given side-length in pixels (screen co-ordinates).
	 * 
	 * @param iDasherX Centre point x co-ordinate
	 * @param iDasherY Centre point y co-ordinate
	 * @param iSize Side length in pixels
	 * @param Color Fill colour index
	 * @param ColorScheme Colour scheme (usually ignored now in favour of colour index)
	 * @param bDrawOutline Whether to draw a hairline black outline
	 */
	public void DasherDrawCentredRectangle(long iDasherX, long iDasherY, int iSize, int Color, EColorSchemes ColorScheme, boolean bDrawOutline) {
		
		CDasherView.Point centre = new CDasherView.Point();
		
		centre = Dasher2Screen(iDasherX, iDasherY);
		
		Screen().DrawRectangle(centre.x - iSize, centre.y - iSize, centre.x + iSize, centre.y + iSize, Color, -1, ColorScheme, bDrawOutline, true, 1);
	}
	
	/**
	 * Draws a given string inside a specified box, the dimensions and co-ordinates
	 * of which are given in Dasher co-ordinates.
	 * <p>
	 * The actual specified bounding box is more or less taken as a guideline
	 * however, and many changes are made from that which is specified to the
	 * drawing command which is actually issued.
	 * <p>
	 * The most important change is that, using the mostleft parameter to
	 * indicate the right-hand edge of some ancestor's text, an effort is made
	 * to 'shove' this text to the right far enough that it does not overlap
	 * with our ancestor.
	 * <p>
	 * This feature is enabled only when bShove is true.
	 * <p>
	 * The font size to be used to draw text is currently hard coded
	 * so that text within 15 pixels of the y axis is drawn at size 11,
	 * between 15 and 30 pixels is drawn at size 14, and over 30 pixels
	 * is drawn at size 20.
	 * <p>
	 * The bounding box is recalculated by calling the Screen's TextSize
	 * method, which attempts to determine the dimensions of this string
	 * at a given size. This is then used to work out how far our children's
	 * text must be 'shoved' to avoid overlapping our own.
	 * <p>
	 * As such, this method is far from general, and should only
	 * be used to draw node labels. A new method will be required
	 * if the need arises to draw arbitrary strings in a specified
	 * location without adjustment.
	 * <p>
	 * This method will return a new 'mostleft' value for use when
	 * drawing our children; if the Screen's TextSize method is
	 * accurate, this ought to prevent our text from overlapping that
	 * of our children.
	 * <p>
	 * For the actual drawing of text, m_DelayDraw is used instead
	 * of passing the command directly to our Screen. This queue
	 * of text to draw will be emptied towards the end of the
	 * drawing cycle.
	 * 
	 * @param iAnchorX1 Left edge x co-ordinate
	 * @param iAnchorY1 Top edge y co-ordinate
	 * @param iAnchorX2 Right edge x co-ordinate
	 * @param iAnchorY2 Bottom edge y co-ordinate
	 * @param sDisplayText String to draw
	 * @param mostleft Co-ordinate of the right-most text drawn by our ancestor.
	 * @param bShove Should we try to avoid overlapping ancestor's text?
	 * @return New 'mostleft' value for drawing children
	 */
	public long DasherDrawText(long iAnchorX1, long iAnchorY1, long iAnchorX2, long iAnchorY2, String sDisplayText, long mostleft, boolean bShove) {
		
		// Don't draw text which will overlap with text in an ancestor.
		
		if(iAnchorX1 > mostleft)
			iAnchorX1 = mostleft;
		
		if(iAnchorX2 > mostleft)
			iAnchorX2 = mostleft;
		
		CDasherView.DRect VisRegion = VisibleRegion();
		
		iAnchorY1 = Math.min( VisRegion.maxY, Math.max( VisRegion.minY, iAnchorY1 ) );
		iAnchorY2 = Math.min( VisRegion.maxY, Math.max( VisRegion.minY, iAnchorY2 ) );
		
		CDasherView.Point ScreenAnchor1 = new CDasherView.Point();
		CDasherView.Point ScreenAnchor2 = new CDasherView.Point();
		// FIXME - Truncate here before converting - otherwise we risk integer overflow in screen coordinates
		
		ScreenAnchor1 = Dasher2Screen(iAnchorX1, iAnchorY1);
		ScreenAnchor2 = Dasher2Screen(iAnchorX2, iAnchorY2);
		
		// Truncate the ends of the anchor line to be on the screen - this
		// prevents us from loosing characters off the top and bottom of the
		// screen
		
		// TruncateToScreen(iScreenAnchorX1, iScreenAnchorY1);
		// TruncateToScreen(iScreenAnchorX2, iScreenAnchorY2);
		
		// Actual anchor point is the midpoint of the anchor line
		
		int iScreenAnchorX = ((ScreenAnchor1.x + ScreenAnchor2.x) / 2);
		int iScreenAnchorY = ((ScreenAnchor1.y + ScreenAnchor2.y) / 2);
		
		// Compute font size based on position
		int Size = lpFontSize;
		
		/* CSFS: BUGFIX: longs needed here, not ints. Fixed. */
		
		// FIXME - this could be much more elegant, and probably needs a
		// rethink anyway - behvaiour here is too dependent on screen size
		
		//long iLeftTimesFontSize = (lpMaxY) - ((iAnchorX1 + iAnchorX2)/ 2) *Size;
		
		//long limit1 = (lpMaxY * 19) / 20;
		//long limit2 = (lpMaxY * 159) / 160;
		
		int DistFromLeft = Dasher2Screen(0,0).x - iScreenAnchorX;
		
		int newlimit1 = 30;
		int newlimit2 = 15;
		
		if(DistFromLeft > newlimit1) {
			Size *= 20;
		}
		else if(DistFromLeft > newlimit2) { 
			Size *= 14;
		}
		else {
			Size *= 11;
		}

		int TextWidth, TextHeight;
		
		CDasherView.Point textDimensions = Screen().TextSize(sDisplayText, Size);
		TextHeight = textDimensions.y;
		TextWidth = textDimensions.x;
		// Poistion of text box relative to anchor depends on orientation
		
		int newleft2 = 0;
		int newtop2 = 0;
		int newright2 = 0;
		int newbottom2 = 0;
		
		switch ((int)(realOrientation)) {
		case (Opts.ScreenOrientations.LeftToRight):
			newleft2 = iScreenAnchorX;
		newtop2 = iScreenAnchorY - TextHeight / 2;
		newright2 = iScreenAnchorX + TextWidth;
		newbottom2 = iScreenAnchorY + TextHeight / 2;
		break;
		case (Opts.ScreenOrientations.RightToLeft):
			newleft2 = iScreenAnchorX - TextWidth;
		newtop2 = iScreenAnchorY - TextHeight / 2;
		newright2 = iScreenAnchorX;
		newbottom2 = iScreenAnchorY + TextHeight / 2;
		break;
		case (Opts.ScreenOrientations.TopToBottom):
			newleft2 = iScreenAnchorX - TextWidth / 2;
		newtop2 = iScreenAnchorY;
		newright2 = iScreenAnchorX + TextWidth / 2;
		newbottom2 = iScreenAnchorY + TextHeight;
		break;
		case (Opts.ScreenOrientations.BottomToTop):
			newleft2 = iScreenAnchorX - TextWidth / 2;
		newtop2 = iScreenAnchorY - TextHeight;
		newright2 = iScreenAnchorX + TextWidth / 2;
		newbottom2 = iScreenAnchorY;
		break;
		default:
			break;
		}
		
		// Update the value of mostleft to take into account the new text
		
		if(bShove) {
			CDasherView.DPoint newTopLeft;
			CDasherView.DPoint newBottomRight;
			
			newTopLeft = Screen2Dasher(newleft2, newtop2, false, true);
			newBottomRight = Screen2Dasher(newright2, newbottom2, false, true);
			
			mostleft = Math.min(newBottomRight.x, newTopLeft.x);
		}
		
		// Actually draw the text. We use DelayDrawText as the text should
		// be overlayed once all of the boxes have been drawn.
		
		m_DelayDraw.DelayDrawText(sDisplayText, newleft2, newtop2, Size);
		
		return mostleft;
		
		/* CSFS: This method was using a pointer to set mostleft; this seemed to be
		 * all it returned, so I've altered it to directly return mostleft.
		 */
	}
	
	/**
	 * Render our nodes, beginning at a given node with given upper
	 * and lower y co-ordinates.
	 * <p>
	 * Will also fill vNodeList with a list of nodes which were
	 * successfully drawn, and vDeleteList with a list of those
	 * whom we abandoned drawing because they were off screen,
	 * too small, etc.
	 * 
	 * @param Root Node at which to start drawing
	 * @param iRootMin Lower y co-ordinate of this node
	 * @param iRootMax Upper y co-ordinate of this node
	 * @param vNodeList Collection to be filled with drawn nodes
	 * @param vDeleteList Collection to be filled with undrawn nodes
	 */
	public abstract void RenderNodes(CDasherNode Root, long iRootMin, long iRootMax, Collection<CDasherNode> vNodeList, Collection<CDasherNode> vDeleteList);
	
	/**
	 * Converts a pair of input co-ordinates to dasher co-ordinates.
	 * <p>
	 * Also takes account of the type of input device we're using,
	 * so that one-dimensional devices and eyetrackers are corrected
	 * appropriately.
	 * <p><i>This method has been renamed from TapOnDisplay</i>
	 * 
	 * @return Dasher co-ordinates corresponding to this input point
	 */
	public abstract CDasherView.DPoint getInputDasherCoords();
	
	/**
	 * Converts a pair of input co-ordinates to dasher co-ordinates.
	 * <p>
	 * Also takes account of the type of input device we're using,
	 * so that one-dimensional devices and eyetrackers are corrected
	 * appropriately.
	 * <p>
	 * This last parameter is redundant.
	 * <p><i>This method has been renamed from TapOnDisplay</i>
	 * @param Added Redundant
	 * 
	 * @return Dasher co-ordinates corresponding to this input point
	 */
	public abstract CDasherView.DPoint getInputDasherCoords(ArrayList<CSymbolProb> Added);
	
	/* CSFS: this method used to have an extra parameter, pNumDeleted, which defaulted to zero.
	 * Since no method ever actually passes this parameter, I've deleted it.
	 */
	
	/**
	 * Should convert a given screen co-ordinate to dasher co-ordinates.
	 * 
	 * @param iInputX Screen x co-ordinate  
	 * @param iInputY Screen y co-ordinate
	 * @param b1D Indicates whether these co-ordinates come from a 1D device
	 * @param bNonlinearity If true, we may wish to correct for a non-linearity in one or other co-ordinate system
	 * @return Dasher co-ordinates
	 */
	public abstract CDasherView.DPoint Screen2Dasher(int iInputX, int iInputY, boolean b1D, boolean bNonlinearity);
	
	/**
	 * Should convert a given Dasher co-ordinate to its equivalent
	 * screen co-ordinates.
	 * 
	 * @param iDasherX Dasher x co-ordinate
	 * @param iDasherY Dasher y co-ordinate
	 * @return Screen co-ordinates
	 */
	public abstract CDasherView.Point Dasher2Screen(long iDasherX, long iDasherY);
		

	/**
	 * Converts input device co-ordinates to the appropriate, not
	 * necessarily equivalent, Dasher co-ordinates.
	 * 
	 * @param iInputX Input x co-ordinate
	 * @param iInputY Input y co-ordinate
	 * @param iType Input co-ordinate scheme: 0 for Screen or 1 for Dasher co-ordintes
	 * @param iMode Input device mode; 0 for normal, 1 for 1D and 2 for eyetracker
	 * @return Dasher co-ordinates corresponding to given input co-ordinates
	 */
	protected abstract CDasherView.DPoint Input2Dasher(int iInputX, int iInputY, int iType, int iMode);
	
	/**
	 * Stub; Returns 0
	 * 
	 * @return 0
	 */
	public int GetAutoOffset() {
	    return 0;
	}
	
	/**
	 * Draws a square aligned to the y axis and between given y co-ordinates
	 * 
	 * @param iDasherMin Bottom y co-ordinate
	 * @param iDasherMax Top y co-ordinate
	 * @param bActive Affects colouring
	 */
	public abstract void NewDrawGoTo(long iDasherMin, long iDasherMax, boolean bActive);
	
	/**
	 * Gets our current screen
	 * 
	 * @return Current screen
	 */
	public CDasherScreen Screen() {
	    return m_Screen;
	}
	
	/**
	 * Stub
	 *
	 */
	public void ResetSum() {
	}
	
	/**
	 * Stub
	 *
	 */
	public void ResetSumCounter() {
	}
	
	/**
	 * Stub
	 *
	 */
	public void ResetYAutoOffset() {
	}
	
	/**
	 * Applies auto speed control given the user's current input
	 * position in Dasher co-ordinates.
	 * 
	 * @param iDasherX x co-ordinate
	 * @param iDasherY y co-ordinate
	 * @param dFrameRate Current frame rate in FPS
	 */
	public void SpeedControl(long iDasherX, long iDasherY, double dFrameRate) {};
	
	/**
	 * Applies any desired co-ordinate nonlinearity in the X direction.
	 * <p>
	 * Both input and output are co-ordinates in Dasher space.
	 * <p>
	 * Returns 0 in this base class; must be overridden if used.
	 * 
	 * @param x raw X co-ordinate
	 * @return New x co-ordinate
	 */
	public double applyXMapping(double x) {return 0.0;};   
	
	/**
	 * Applies any desired co-ordinate nonlinearity in the Y direction.
	 * <p>
	 * Both input and output are co-ordinates in Dasher space.
	 * <p>
	 * Returns 0 in this base class; must be overridden if used.
	 * 
	 * @param x raw Y co-ordinate
	 * @return New y co-ordinate
	 */
	public double ymap(double x) {return 0.0;}; 
	
	/**
	 * Returns a rectangle indicating the area in Dasher space
	 * which is current visible.
	 * 
	 * @return Visible region
	 */
	public abstract CDasherView.DRect VisibleRegion();
	
	/**
	 * Draws the crosshair
	 * 
	 * @param sx X co-ordinate at which to draw the crossbeam
	 */
	protected abstract void Crosshair(long sx);
	
	// Old screen-to-dasher and vice versa mappings.
	
	/*public CDasherView.Point MapScreen(int DrawX, int DrawY) {
		
		CDasherView.Point retval = new CDasherView.Point();
		
		retval.x = DrawX;
		retval.y = DrawY;
		
		switch (realOrientation) {
		case (Opts.ScreenOrientations.LeftToRight):
			break;
		case (Opts.ScreenOrientations.RightToLeft):
			retval.x = Screen().GetWidth() - retval.x;
		break;
		case (Opts.ScreenOrientations.TopToBottom):{
			int Swapper = (retval.x * Screen().GetHeight()) / Screen().GetWidth();
			retval.x = (DrawY * Screen().GetWidth()) / Screen().GetHeight();
			retval.y = Swapper;
			break;
		}
		case (Opts.ScreenOrientations.BottomToTop):{
			// Note rotation by 90 degrees not reversible like others
			int Swapper = Screen().GetHeight() - (retval.x * Screen().GetHeight()) / Screen().GetWidth();
			retval.x = (retval.y * Screen().GetWidth()) / Screen().GetHeight();
			retval.y = Swapper;
			break;
		}
		default:
			break;
		}
		return retval;
	}*/
	
	/*public void UnMapScreen(int MouseX, int MouseY) {
				
		CDasherView.Point retval = new CDasherView.Point();
		retval.x = MouseX;
		retval.y = MouseY;
		
		switch (realOrientation) {
		case (Opts.ScreenOrientations.LeftToRight):
			break;
		case (Opts.ScreenOrientations.RightToLeft):
			retval.x = Screen().GetWidth() - retval.x;
			break;
		case (Opts.ScreenOrientations.TopToBottom):{
			int Swapper = (retval.x * Screen().GetHeight()) / Screen().GetWidth();
			retval.x = (retval.y * Screen().GetWidth()) / Screen().GetHeight();;
			retval.y = Swapper;
			break;
		}
		case (Opts.ScreenOrientations.BottomToTop):{
			int Swapper = (retval.x * Screen().GetHeight()) / Screen().GetWidth();
			retval.x = ((Screen().GetHeight() - retval.y) * Screen().GetWidth()) / Screen().GetHeight();
			retval.y = Swapper;
			break;
		}
		default:
			break;
		}
	}*/
}
