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
 * An implementation (currently the one and only) of DasherView.
 * <p>
 * This draws all DasherNodes as a square aligned to the Y axis
 * and with sizes dependent on their probability.
 * <p>
 * The horizontal and vertical scales are both subject to non-linearity;
 * the x axis is linear at first and logarithmic after a given
 * point, and the y axis is linear in all places but with different
 * decreasing magnification factors towards the edges.
 */
public class CDasherViewSquare extends CDasherView {

	/**
	 * Helper class which applies and unapplies the y-axis
	 * non-linearity.
	 */
	protected class Cymap {
		
		/**
		 * Compression factor
		 */
		protected long m_Y1;
		
		/**
		 * Y co-ordinate above which to apply compression
		 */
		protected long m_Y2;
		
		/**
		 * Y co-ordinate below which to apply compression
		 */
		protected long m_Y3;
		
		/**
		 * Calculates m_Y1, 2 and 3 based on the total length
		 * of the y axis and hard coded scaling factors which
		 * state that objects should be scaled down a factor
		 * of 4 when within 5% of the outer edges.
		 * 
		 * @param iScale Total y-axis length
		 */
		public Cymap(long iScale) {
			double dY1 = 0.25;
			double dY2 = 0.95;
			double dY3 = 0.05;
			
			m_Y2 = (long)(dY2 * iScale);
			m_Y3 = (long)(dY3 * iScale);
			m_Y1 = (long)(1.0 / dY1);
		}
		
		/**
		 * Converts a y co-ordinate according to this schema
		 * 
		 * @param y Raw y co-ordinate
		 * @return Converted y co-ordinate
		 */
		public long map(long y) {
			if(y > m_Y2)
				return m_Y2 + (y - m_Y2) / m_Y1;
			else if(y < m_Y3)
				return m_Y3 + (y - m_Y3) / m_Y1;
			else
				return y;
		}
		
		/**
		 * Restores a y co-ordinate by unapplying non-linearity
		 * 
		 * @param ydash Converted y co-ordinate
		 * @return Original, raw y co-ordinate
		 */
		public long unmap(long ydash) {
			if(ydash > m_Y2)
				return (ydash - m_Y2) * m_Y1 + m_Y2;
			else if(ydash < m_Y3)
				return (ydash - m_Y3) * m_Y1 + m_Y3;
			else
				return ydash;
		}
	}
	
	/**
	 * Small class for returning dashery2screen values, which
	 * compute two new y co-ordinates and a size attribute.
	 */
	class D2Yret {
		/**
		 * y co-ordinate 1
		 */
		int s1;
		/**
		 * y co-ordinate 2
		 */
		int s2;
		/**
		 * Size
		 */
		int size;
	}
	
	/**
	 * Scale factor applied to the logarithmic portion of the
	 * x-axis nonlinearity. Formerly known as m_dXmpa.
	 */
	protected double m_dXMappingLogarithmicScaleFactor;
	
	/**
	 * X co-ordinate at which we switch from linear to logarithmic
	 * magnification. Formerly known as m_dXmpb.
	 */
	protected double m_dXMappingLogLinearBoundary;
	
	/**
	 * Scale factor applied to the linear portion of the 
	 * x-axis nonlinearity. Formerly known as m_dXmpc.
	 */	
	protected double m_dXMappingLinearScaleFactor;
		
	/**
	 * Height of our current screen in pixels
	 */
	protected int CanvasY;
	
	/**
	 * Nominally an offset which will be auto-calibrated
	 * and applied to input co-ordinates. At present this is
	 * always zero.
	 */	
	protected int m_yAutoOffset;
	
	/**
	 * Helper for y mapping.
	 */
	protected Cymap m_ymap;
	
	
	// Cached values for scaling
	/**
	 * Cached values for scaling
	 */
	protected long iLRScaleFactorX;
	
	/**
	 * Cached values for scaling
	 */
	protected long iLRScaleFactorY;
	
	/**
	 * Cached values for scaling
	 */
	protected long iTBScaleFactorX;
	
	/**
	 * Cached values for scaling
	 */
	protected long iTBScaleFactorY;
	
	/**
	 * The factor that scale factors are multipled by
	 */  
	protected long m_iScalingFactor;
	
	/**
	 * Top-left corner of the current visible region
	 */
	protected CDasherView.DPoint m_iDasherMin;
	
	/**
	 * Bottom-right corner of the current visible region
	 */
	protected CDasherView.DPoint m_iDasherMax;
	
	/**
	 * Cache of LP_TRUNCATION
	 */
	protected int lpTruncation;
	
	/**
	 * Cache of LP_TRUNCATION_TYPE
	 */
	protected int lpTruncationType;
	
	/**
	 * Cache of LP_NORAMLIZATON
	 */
	protected int lpNormalisation;
	
	/**
	 * Sole constructor. Creates a new view wrapping a specified
	 * screen, fires the ChangeScreen method to configure itself
	 * around said screen, creates a DelayedDraw object for
	 * String drawing, preloads our cached parameter values
	 * and sets certain hard-coded scaling factors, such as those
	 * used in the x-axis nonlinearity.
	 * <p>
	 * The specific values at present are:
	 * <p>
	 * Multiply all linear scaled values by 0.9, switch to logarithmic
	 * scaling from the midpoint of the axis, and multiply all log
	 * scaled values by 0.2.
	 * 
	 * @param EventHandler Event handler with which to register ourselves
	 * @param SettingsStore Settings repository to use
	 * @param DasherScreen Screen to wrap and use for primitive drawing
	 */
	public CDasherViewSquare(CEventHandler EventHandler, CSettingsStore SettingsStore, CDasherScreen DasherScreen)  {
		
		super(EventHandler, SettingsStore, DasherScreen);
		
		 m_iDasherMin = new CDasherView.DPoint();
		 m_iDasherMax = new CDasherView.DPoint();
		
		// TODO - AutoOffset should be part of the eyetracker input filter
		// Make sure that the auto calibration is set to zero berfore we start
		
		m_yAutoOffset = 0;
		
		m_DelayDraw = new CDelayedDraw();
		ChangeScreen(DasherScreen);
		
		// TODO - Make these parameters
		// tweak these if you know what you are doing
		m_dXMappingLogarithmicScaleFactor = 0.2;                // these are for the x non-linearity
		m_dXMappingLogLinearBoundary = 0.5;
		m_dXMappingLinearScaleFactor = 0.9;
				
		m_ymap = new Cymap(lpMaxY);
		
		m_bVisibleRegionValid = false;
		
		lpTruncation = (int)SettingsStore.GetLongParameter(Elp_parameters.LP_TRUNCATION);
		lpTruncationType = (int)SettingsStore.GetLongParameter(Elp_parameters.LP_TRUNCATIONTYPE);
		lpNormalisation = (int)SettingsStore.GetLongParameter(Elp_parameters.LP_NORMALIZATION);
		
		// These results are cached to make co-ordinate transformations quicker.
		// We ought not to be caught out, as I have registered the class
		// to watch for changes to these parameters.
		
	}
	
	/**
	 * Method is called by the EventHandler when dispatching
	 * events.
	 * <p>
	 * This class responds to the follow parameter-change events:
	 * <p>
	 * <i>LP_REAL_ORIENTATION</i>: Invalidates our current visible region.
	 * <p>
	 * <i>LP_TRUNCATION, LP_TRUNCATION_TYPE, LP_NORMALIZATION</i>:
	 * Updates internally cached values of these parameters.
	 */
	public void HandleEvent(CEvent Event) {
		// Let the parent class do its stuff
		super.HandleEvent(Event);
		
		// And then interpret events for ourself
		if(Event.m_iEventType == 1) {
			CParameterNotificationEvent Evt = (CParameterNotificationEvent)Event;
			
			if (Evt.m_iParameter == Elp_parameters.LP_REAL_ORIENTATION) {
				m_bVisibleRegionValid = false;
			}
			else if (Evt.m_iParameter == Elp_parameters.LP_TRUNCATION) {
				lpTruncation = (int)GetLongParameter(Elp_parameters.LP_TRUNCATION);
			}
			else if (Evt.m_iParameter == Elp_parameters.LP_TRUNCATIONTYPE) {
				lpTruncationType = (int)GetLongParameter(Elp_parameters.LP_TRUNCATIONTYPE);
			}
			else if (Evt.m_iParameter == Elp_parameters.LP_NORMALIZATION) {
				lpNormalisation = (int)GetLongParameter(Elp_parameters.LP_NORMALIZATION);
			}
		}
	}
	
	/**
	 * Draws a tree of nodes beginning at a given root.
	 * <p>
	 * This function will call our screen's Blank method, and then call
	 * RecursiveRender on the Node we wish to draw. Once the RecursiveRender
	 * is finished, it will call m_DelayDraw's Draw method to draw
	 * all strings which have been queued up.
	 * <p>
	 * Finally, it will draw the Crosshair.
	 * 
	 * @param Root Node at which to begin drawing
	 * @param iRootMin Bottom co-ordinate of the root node (in Dasher space)
	 * @param iRootMax Top co-ordinate of the root node (in Dasher space)
	 * @param vNodeList Collection which will be filled with drawn Nodes
	 * @param vDeleteList Collection which will be filled with undrawable Nodes
	 */
	public void RenderNodes(CDasherNode Root, long iRootMin, long iRootMax, Collection<CDasherNode> vNodeList, Collection<CDasherNode> vDeleteList) {
		
		Screen().Blank();
		
		CDasherView.DRect visreg = VisibleRegion();
		
		try  {
			RecursiveRender(Root, iRootMin, iRootMax, (int)visreg.maxX, vNodeList, vDeleteList);
		}
		catch(NodeCannotBeDrawnException e) {
			// Do nothing
		}
		
		// DelayDraw the text nodes
		m_DelayDraw.Draw(Screen());
		
		Crosshair(GetLongParameter(Elp_parameters.LP_OX));  // add crosshair
	}
	
	
	/* CSFS: Heavily modified to get the new mostleft value out. I'm fairly sure this
	 * obeys the same semantics as the C++ version but this needs to be tested.
	 */
	
	/**
	 * Recursively renders a tree of Nodes beginning at a given root, based on the following procedure:
	 * <p>
	 * <ol><li>Calls RenderNode on this node, drawing it
	 * <li>Calls RenderGroups on this node, drawing the grouping boxes for our children
	 * <li>Recurses on each of our children in turn
	 * </ol><p>
	 * Of course, if our children are not yet present, we return
	 * after step one.
	 * <p>
	 * This method also takes care of filling our Collections of drawable
	 * and undrawable nodes, by catching NodeCannotBeDrawnExceptions in the latter
	 * case.
	 * <p>
	 * 'Shoving,' the process which keeps a child node's text from overlapping
	 * that of its parent, is also co-ordinated here; the new mostleft
	 * value returned by RenderNode is passed into the recursive
	 * calls which draw our children.
	 * 
	 * @param Render Node at which to begin rendering
	 * @param y1 Top y co-ordinate of this Node
	 * @param y2 Bottom y co-ordinate of this Node
	 * @param mostleft Shoving parameter; see above
	 * @param vNodeList Collection to fill with drawn, childless Nodes
	 * @param vDeleteList Collection to fill with undrawable Nodes
	 */
	public void RecursiveRender(CDasherNode Render, long y1, long y2, int mostleft, Collection<CDasherNode> vNodeList, Collection<CDasherNode> vDeleteList) throws NodeCannotBeDrawnException {
		
		// This method takes mostleft by VALUE.
		
		/* Step 1: Render *this* node */
		try {
		
		mostleft = RenderNode(Render.Colour(), Render.ColorScheme(), y1, y2, mostleft, Render.m_strDisplayText, Render.m_bShove);
			
		}
		/* If this node was not drawable, mark it for deletion */
		catch(NodeCannotBeDrawnException e) {
			vDeleteList.add(Render);
			Render.Kill();
			return;
		}
						
		/* If this node hasn't any children (yet), we're done */
		if(Render.ChildCount() == 0) {
			vNodeList.add(Render);
			return;
		}
		
		// Render groups
		/* Step 2: Render the large grouping boxes to which this node's
		 * children belong. For example, draw the yellow box for the
		 * capitals.
		 */
		RenderGroups(Render, y1, y2, mostleft);
		
		/* Step 3: Draw our child nodes */
		int norm = lpNormalisation;
		
		for(CDasherNode i : Render.GetChildren()) {
			
			long Range = y2 - y1;
			long newy1 = y1 + (Range * i.Lbnd()) / norm;
			long newy2 = y1 + (Range * i.Hbnd()) / norm;
			
			// FIXME - make the threshold a parameter
			
			if((newy2 - newy1 > 50) || (i.Alive())) {
				i.Alive(true);
				RecursiveRender(i, newy1, newy2, mostleft, vNodeList, vDeleteList);
				
			}
		}
	}
	
	/* CSFS: Modified to return mostleft. */
	
	/**
	 * Draws a given Node's grouping boxes, into which its Children
	 * are arranged. The simply calls RecursiveRenderGroups on each top-level
	 * group in turn, which does the actual drawing.
	 * 
	 * @param Render Node whose grouping boxes we wish to draw
	 * @param y1 Top y co-ordinate of this Node
	 * @param y2 Bottom y co-ordinate of this Node
	 * @param mostleft Shoving parameter; indicates how far group labels should be displaced
	 * to the right in order to avoid overlapping parents' text.
	 */
	public void RenderGroups(CDasherNode Render, long y1, long y2, int mostleft) {
		
		// This method takes mostleft by VALUE.
		
		SGroupInfo CurrentGroup = (Render.m_BaseGroup);
		
		while(CurrentGroup != null) {
			RecursiveRenderGroups(CurrentGroup, Render, y1, y2, mostleft);
			CurrentGroup = CurrentGroup.Next;
		}
	}
	
	/**
	 * Renders a given Group and all of its child groups by recursion.
	 * <p>
	 * RenderNode is used to do the actual work of drawing the group
	 * boxes, and their sizes are determined by consulting the
	 * child list which this group delimits. 
	 * 
	 * @param CurrentGroup Group at which to start drawing
	 * @param Node Node of which this group is a child
	 * @param y1 Top Dasher co-ordinate of this group
	 * @param y2 Bottom Dasher co-ordinate of this group
	 * @param mostleft Shoving parameter; indicates how far group labels should be displaced
	 * to the right in order to avoid overlapping parents' text.
	 */	
	public void RecursiveRenderGroups(SGroupInfo CurrentGroup, CDasherNode Node, long y1, long y2, int mostleft) {
		
		// This method takes mostleft by VALUE.
		
		if(CurrentGroup.bVisible) {
			long range = y2 - y1;
			
			int lower = (CurrentGroup.iStart);
			int upper = (CurrentGroup.iEnd);
			
			long lbnd = Node.Children().get(lower).Lbnd();
			long hbnd = Node.Children().get(upper - 1).Hbnd();
			
			long newy1 = y1 + (range * lbnd) / lpNormalisation;
			long newy2 = y1 + (range * hbnd) / lpNormalisation;
			try {
				mostleft = RenderNode(CurrentGroup.iColour, EColorSchemes.Groups, newy1, newy2, mostleft, CurrentGroup.strLabel, true);
			}
			catch(NodeCannotBeDrawnException e) {
				// Do nothing
			}
			
		}
		
		// Iterate through child groups
		SGroupInfo CurrentChild = (CurrentGroup.Child);
		
		while(CurrentChild != null) {
			RecursiveRenderGroups(CurrentChild, Node, y1, y2, mostleft);
			CurrentChild = CurrentChild.Next;
		}
	}
	
	
	
	/**
	 * Determines whether a given Node can be drawn, and if so, draws it.
	 * <p>
	 * If the Node is undrawable because it's off the screen
	 * or is too small to sensibly draw, a NodeCannotBeDrawnException
	 * is thrown.
	 * <p>
	 * The drawing instruction is clipped to the visible region first,
	 * so the Screen should not be ordered to draw anything outside
	 * its bounds.
	 * <p>
	 * If truncation is enabled, it is applied here; if not, we
	 * simply call DasherDrawRectangle to form the Node box.
	 * <p>
	 * Lastly, the Node's text is added to m_DelayDraw to be
	 * drawn to the screen at the end of the drawing sequence.
	 * <p>
	 * This also generates a new mostleft value for shoving purposes,
	 * which is returned to our caller.
	 * 
	 * @param Color Colour index to use for the node's background
	 * @param ColorScheme ColourScheme to use; usually ignored in favour of a colour index
	 * @param y1 Top y co-ordinate of this Node (Dasher space)
	 * @param y2 Bottom y co-ordinate of this Node (Dasher space)
	 * @param mostleft Shoving parameter; indicates how far node labels should be displaced
	 * to the right in order to avoid overlapping parents' text.
	 * @param sDisplayText Text to draw in this Node
	 * @param bShove Should we shove (report how far our text juts out, 
	 * to help our caller avoid overlapping it)
	 * @return New value of mostleft (or that which was passed in, if we
	 * do not) 
	 * @throws NodeCannotBeDrawnException if this Node cannot be drawn!
	 */
	public int RenderNode(int Color, EColorSchemes ColorScheme, long y1, long y2, int mostleft, String sDisplayText, boolean bShove) throws NodeCannotBeDrawnException {
		
		/*
		 * IMPORTANT: This method takes mostleft by REFERENCE in the original
		 * C++; as such it returns its new value here.
		 */
		
		if (!(y2 >= y1)) { return mostleft; }
		
		// TODO - Get sensible limits here (to allow for non-linearities)
		CDasherView.DRect visreg = VisibleRegion();
		
		// TODO - use new versions of functions
		
		//int top = Dasher2Screen(0, y1).y;
		//int bottom = Dasher2Screen(0, y2).y; 
		long iSize = m_ymap.map(y2) - m_ymap.map(y1);
		
		// Actual height in pixels
		int iHeight = (int)((iSize * CanvasY) / (int) lpMaxY);
		
		if(iHeight <= 1)
			{ throw new NodeCannotBeDrawnException(); }                  // We're too small to render
		
		if((y1 > visreg.maxY) || (y2 < visreg.minY)){
			{ throw new NodeCannotBeDrawnException(); }                // We're entirely off screen, so don't render.
		}
		
		long iDasherSize = (y2 - y1);
		
		// FIXME - get rid of pointless assignment below
		
		if(lpTruncation == 0) {        // Regular squares
			DasherDrawRectangle(Math.min(iDasherSize,visreg.maxX), Math.min(y2,visreg.maxY), 0, Math.max(y1,visreg.minY), Color, -1, ColorScheme, GetBoolParameter(Ebp_parameters.BP_OUTLINE_MODE), true, 1);
		}
		else {
			int iDasherY = (int)lpMaxY;
			
			int iSpacing = iDasherY / 128;       // FIXME - assuming that this is an integer below
			
			int iXStart = 0;
			
			switch (lpTruncationType) {
			case 1:
				iXStart = (int)(iSize - iSize * lpTruncation / 200);
				break;
			case 2:
				iXStart = (int)(iSize - iSize * lpTruncation / 100);
				break;
			}
			
			int iTipMin = (int)((y2 - y1) * lpTruncation / (200) + y1);
			int iTipMax = (int)(y2 - (y2 - y1) * lpTruncation / (200));
			
			int iLowerMin = (int)(((y1 + 1) / iSpacing) * iSpacing);
			int iLowerMax = (((iTipMin - 1) / iSpacing) * iSpacing);
			
			int iUpperMin = (((iTipMax + 1) / iSpacing) * iSpacing);
			int iUpperMax = (int)(((y2 - 1) / iSpacing) * iSpacing);
			
			if(iLowerMin < 0)
				iLowerMin = 0;
			
			if(iLowerMax < 0)
				iLowerMax = 0;
			
			if(iUpperMin < 0)
				iUpperMin = 0;
			
			if(iUpperMax < 0)
				iUpperMax = 0;
			
			if(iLowerMin > iDasherY)
				iLowerMin = iDasherY;
			
			if(iLowerMax > iDasherY)
				iLowerMax = iDasherY;
			
			if(iUpperMin > iDasherY)
				iUpperMin = iDasherY;
			
			if(iUpperMax > iDasherY)
				iUpperMax = iDasherY;
			
			while(iLowerMin < y1)
				iLowerMin += iSpacing;
			
			while(iLowerMax > iTipMin)
				iLowerMax -= iSpacing;
			
			while(iUpperMin < iTipMax)
				iUpperMin += iSpacing;
			
			while(iUpperMax > y2)
				iUpperMax -= iSpacing;
			
			int iLowerCount = ((iLowerMax - iLowerMin) / iSpacing + 1);
			int iUpperCount = ((iUpperMax - iUpperMin) / iSpacing + 1);
			
			if(iLowerCount < 0)
				iLowerCount = 0;
			
			if(iUpperCount < 0)
				iUpperCount = 0;
			
			int iTotalCount = (int)(iLowerCount + iUpperCount + 6);
			
			long[] x = new long[iTotalCount];
			long[] y = new long[iTotalCount];
			
			// Weird duplication here is to make truncated squares possible too
			
			x[0] = 0;
			y[0] = y1;
			x[1] = iXStart;
			y[1] = y1;
			
			x[iLowerCount + 2] = iDasherSize;
			y[iLowerCount + 2] = iTipMin;
			x[iLowerCount + 3] = iDasherSize;
			y[iLowerCount + 3] = iTipMax;
			
			x[iTotalCount - 2] = iXStart;
			y[iTotalCount - 2] = y2;
			x[iTotalCount - 1] = 0;
			y[iTotalCount - 1] = y2;
			
			for(int i = (0); i < iLowerCount; ++i) {
				x[i + 2] = (iLowerMin + i * iSpacing - y1) * (iDasherSize - iXStart) / (iTipMin - y1) + iXStart;
				y[i + 2] = iLowerMin + i * iSpacing;
			}
			
			for(int j = (0); j < iUpperCount; ++j) {
				x[j + iLowerCount + 4] = (y2 - (iUpperMin + j * iSpacing)) * (iDasherSize - iXStart) / (y2 - iTipMax) + iXStart;
				y[j + iLowerCount + 4] = iUpperMin + j * iSpacing;
			}
			
			DasherPolygon(x, y, iTotalCount, Color);
			
		}
		
		long iDasherAnchorX = (iDasherSize);
		
		if( sDisplayText != null && sDisplayText.length() > 0 )
			mostleft = (int)DasherDrawText(iDasherAnchorX, y1, iDasherAnchorX, y2, sDisplayText, mostleft, bShove);
		
		/* CSFS: Method altered to use a Byte ArrayList for UTF-8 characters instead of a String. */
		
		{ return mostleft; }
	}

	
	/**
	 * Determines whether a node falls within our current visible
	 * region. This is determined by the simple expedient of calling
	 * VisibleRegion and comparing the passed co-ordinates.
	 * 
	 *  @param y1 Node's top y co-ordinate
	 *  @param y2 Node's bottom y co-ordinate
	 *  @return True if falls within visible region, False otherwise
	 */
	public boolean IsNodeVisible(long y1, long y2) {
		
		CDasherView.DRect visreg = VisibleRegion();
		
		return (y1 > visreg.minY) || (y2 < visreg.maxY ) || (y2-y1 < visreg.maxX);
	}
	
	/** 
	 * Convert screen co-ordinates to dasher co-ordinates. This doesn't
	 * include the nonlinear mapping for eyetracking mode etc - it is
	 * just the inverse of the mapping used to calculate the screen
	 * positions of boxes etc.
	 * 
	 * @param iInputX Screen x co-ordinate
	 * @param iInputY Screen y co-ordinate
	 * @param b1D Should we use a simpler transform
	 * for 1D devices?
	 * @param bNonlinearity Should we use unapplyXMapping and m_ymap.unmap
	 * to reverse an applied nonlinearity?
	 * @return Point in Dasher space equivalent to the given Screen point
	 */	
	public CDasherView.DPoint Screen2Dasher(int iInputX, int iInputY, boolean b1D, boolean bNonlinearity) {
		
		// Things we're likely to need:
		
		/* CSFS: Replacement return value since the C++ version uses pass-by-ref */
		
		CDasherView.DPoint retval = new CDasherView.DPoint();
		
		long iDasherWidth = lpMaxY;
		long iDasherHeight = lpMaxY;
		
		int iScreenWidth = Screen().GetWidth();
		int iScreenHeight = Screen().GetHeight();
		
		if( b1D ) { // Special case for 1D mode...
			retval.x = iInputX * iDasherWidth / iScreenWidth;
			retval.y = iInputY * iDasherHeight / iScreenHeight;
			return retval;
		}
		
		int eOrientation = realOrientation;
		
		long iScaleFactorX;
		long iScaleFactorY;
		
		CDasherView.DPoint scale = GetScaleFactor(eOrientation); // FIXME
		
		iScaleFactorX = scale.x;
		iScaleFactorY = scale.y;
		
		switch(eOrientation) {
		case Opts.ScreenOrientations.LeftToRight:
			retval.x = iDasherWidth / 2 - ( iInputX - iScreenWidth / 2 ) * m_iScalingFactor / iScaleFactorX;
		retval.y = iDasherHeight / 2 + ( iInputY - iScreenHeight / 2 ) * m_iScalingFactor / iScaleFactorY;
		break;
		case Opts.ScreenOrientations.RightToLeft:
			retval.x = (iDasherWidth / 2 + ( iInputX - iScreenWidth / 2 ) * m_iScalingFactor/ iScaleFactorX);
		retval.y = (iDasherHeight / 2 + ( iInputY - iScreenHeight / 2 ) * m_iScalingFactor/ iScaleFactorY);
		break;
		case Opts.ScreenOrientations.TopToBottom:
			retval.x = (iDasherWidth / 2 - ( iInputY - iScreenHeight / 2 ) * m_iScalingFactor/ iScaleFactorY);
		retval.y = (iDasherHeight / 2 + ( iInputX - iScreenWidth / 2 ) * m_iScalingFactor/ iScaleFactorX);
		break;
		case Opts.ScreenOrientations.BottomToTop:
			retval.x = (iDasherWidth / 2 + ( iInputY - iScreenHeight / 2 ) * m_iScalingFactor/ iScaleFactorY);
		retval.y = (iDasherHeight / 2 + ( iInputX - iScreenWidth / 2 ) * m_iScalingFactor/ iScaleFactorX);
		break;
		}
		
		// FIXME - disabled to avoid floating point
		if( bNonlinearity ) {
			retval.x = (long)(unapplyXMapping(retval.x / (double)lpMaxY) * lpMaxY);
			retval.y = (long)m_ymap.unmap(retval.y);
		}
		
		return retval;
	}
	
	/**
	 * Computes a set of scaling factors for use in transforming
	 * screen to Dasher co-ordinates and vice versa. This should
	 * be re-run any time the Screen's height or width are liable
	 * to have changed, or if we wish to change the size of the
	 * Dasher world's co-ordinate space.
	 */
	public void SetScaleFactor()
	{
		long iDasherWidth = lpMaxY;
		long iDasherHeight = iDasherWidth;
		
		int iScreenWidth = Screen().GetWidth();
		int iScreenHeight = Screen().GetHeight();
		
		// Try doing this a different way:
		
		long iDasherMargin = ( 300 ); // Make this a parameter
		
		long iMinX = ( 0-iDasherMargin );
		long iMaxX = ( iDasherWidth + iDasherMargin );
		long iMinY = ( 0 );
		long iMaxY = ( iDasherHeight );
		
		double dLRHScaleFactor;
		double dLRVScaleFactor;
		double dTBHScaleFactor;
		double dTBVScaleFactor;
		
		dLRHScaleFactor = iScreenWidth / (double)( iMaxX - iMinX );
		dLRVScaleFactor = iScreenHeight / (double)( iMaxY - iMinY );
		dTBHScaleFactor = iScreenWidth / (double)( iMaxY - iMinY );
		dTBVScaleFactor = iScreenHeight / (double)( iMaxX - iMinX );
		
		iLRScaleFactorX = (long)(Math.max(Math.min(dLRHScaleFactor, dLRVScaleFactor), dLRHScaleFactor / 4.0) * m_iScalingFactor);
		iLRScaleFactorY = (long)(Math.max(Math.min(dLRHScaleFactor, dLRVScaleFactor), dLRVScaleFactor / 4.0) * m_iScalingFactor);
		iTBScaleFactorX = (long)(Math.max(Math.min(dTBHScaleFactor, dTBVScaleFactor), dTBVScaleFactor / 4.0) * m_iScalingFactor);
		iTBScaleFactorY = (long)(Math.max(Math.min(dTBHScaleFactor, dTBVScaleFactor), dTBHScaleFactor / 4.0) * m_iScalingFactor);
	}
	
	/**
	 * Retrieves the x and y scaling factors relevant to our
	 * current orientation -- so iLRScaleFactorX and Y if we're
	 * in LeftToRight or RightToLeft modes, or their TB equivalents
	 * otherwise.
	 * 
	 * @param eOrientation Current orientation
	 * @return Dasher-world co-ordinate containing the width scaling
	 * factor as its x, and the height factor as its y.
	 */
	public CDasherView.DPoint GetScaleFactor(int eOrientation) {
		CDasherView.DPoint retval = new CDasherView.DPoint();
		
		if(( eOrientation == Opts.ScreenOrientations.LeftToRight ) || ( eOrientation == Opts.ScreenOrientations.RightToLeft )) {
			retval.x = iLRScaleFactorX;
			retval.y = iLRScaleFactorY;
		} else {
			retval.x = iTBScaleFactorX;
			retval.y = iTBScaleFactorY;
		}
		
		return retval;
	}
	
	/**
	 * Converts Dasher co-ordinates to the Screen co-ordinate
	 * indicating the same location.
	 * <p>
	 * Applies non-linearities to the Dasher co-ordinates using
	 * applyXMapping and m_ymap.map, before scaling appropriate
	 * to our current screen orientation.
	 * 
	 * @param iDasherX Dasher x co-ordinate
	 * @param iDasherY Dasher y co-ordinate
	 * @return Screen point corresponding to this location
	 */
	public CDasherView.Point Dasher2Screen(long iDasherX, long iDasherY) {
		
		CDasherView.Point retval = new CDasherView.Point();
		// Apply the nonlinearities
		
		
		// FIXME
		iDasherX = (long)(applyXMapping(iDasherX / (double)(lpMaxY)) * lpMaxY);
		iDasherY = m_ymap.map(iDasherY);
		
		
		// Things we're likely to need:
		
		long iDasherWidth = lpMaxY;
		long iDasherHeight = lpMaxY;
		
		int iScreenWidth = Screen().GetWidth();
		int iScreenHeight = Screen().GetHeight();
		
		long iScaleFactorX;
		long iScaleFactorY;
		
		CDasherView.DPoint scale = GetScaleFactor(realOrientation);
		iScaleFactorX = scale.x;
		iScaleFactorY = scale.y;
		
		switch( realOrientation ) {
		case Opts.ScreenOrientations.LeftToRight:
			retval.x = (int)(iScreenWidth / 2 - ( iDasherX - iDasherWidth / 2 ) * iScaleFactorX / m_iScalingFactor);
			retval.y = (int)(iScreenHeight / 2 + ( iDasherY - iDasherHeight / 2 ) * iScaleFactorY / m_iScalingFactor);
		break;
		case Opts.ScreenOrientations.RightToLeft:
			retval.x = (int)(iScreenWidth / 2 + ( iDasherX - iDasherWidth / 2 ) * iScaleFactorX / m_iScalingFactor);
			retval.y = (int)(iScreenHeight / 2 + ( iDasherY - iDasherHeight / 2 ) * iScaleFactorY / m_iScalingFactor);
		break;
		case Opts.ScreenOrientations.TopToBottom:
			retval.x = (int)(iScreenWidth / 2 + ( iDasherY - iDasherHeight / 2 ) * iScaleFactorX / m_iScalingFactor);
			retval.y = (int)(iScreenHeight / 2 - ( iDasherX - iDasherWidth / 2 ) * iScaleFactorY / m_iScalingFactor);
		break;
		case Opts.ScreenOrientations.BottomToTop:
			retval.x = (int)(iScreenWidth / 2 + ( iDasherY - iDasherHeight / 2 ) * iScaleFactorX / m_iScalingFactor);
			retval.y = (int)(iScreenHeight / 2 + ( iDasherX - iDasherWidth / 2 ) * iScaleFactorY / m_iScalingFactor);
		break;
		}
		
		return retval;
	}
	
	/**
	 * Produces a rectangle showing the region of Dasher space
	 * which is currently visible on screen.
	 * <p>
	 * This is accomplished by the simple expedient of running
	 * Screen2Dasher against the minimum and maximum screen
	 * co-ordinates.
	 * <p>
	 * This function takes our current orientation into account
	 * in producing its answer.
	 * 
	 * @return Rectangle indicating the visible region.
	 */
	public CDasherView.DRect VisibleRegion() {
		
		CDasherView.DRect retval = new CDasherView.DRect();
				
		if(!m_bVisibleRegionValid) {
			
					
			switch( realOrientation ) {
			case Opts.ScreenOrientations.LeftToRight:
				m_iDasherMin = Screen2Dasher(Screen().GetWidth(),0,false,true);
				m_iDasherMax = Screen2Dasher(0,Screen().GetHeight(),false,true);
			break;
			case Opts.ScreenOrientations.RightToLeft:
				m_iDasherMin = Screen2Dasher(0,0,false,true);
				m_iDasherMax = Screen2Dasher(Screen().GetWidth(),Screen().GetHeight(),false,true);
			break;
			case Opts.ScreenOrientations.TopToBottom:
				m_iDasherMin = Screen2Dasher(0,Screen().GetHeight(),false,true);
				m_iDasherMax = Screen2Dasher(Screen().GetWidth(),0,false,true);
			break;
			case Opts.ScreenOrientations.BottomToTop:
				m_iDasherMin = Screen2Dasher(0,0,false,true);
				m_iDasherMax = Screen2Dasher(Screen().GetWidth(),Screen().GetHeight(),false,true);
			break;
			}
			
			m_bVisibleRegionValid = true;
		}
		
		retval.maxX = m_iDasherMax.x;
		retval.maxY = m_iDasherMax.y;
		retval.minX = m_iDasherMin.x;
		retval.minY = m_iDasherMin.y;
		
		return retval;
	}
	

	/* CSFS: Removed functions which returned a single aspect of the visible region;
	 * it didn't seem any routine made use of them anymore.
	 */

	
	/** 
	 * Convert abstract 'input coordinates', which may or may not
	 * correspond to actual screen positions, depending on the settings,
	 * into dasher co-ordinates. Modes are:
	 * <p>
	 * 0 = Direct (ie mouse)<br>
	 * 1 = 1D<br>
	 * 2 = Eyetracker
	 * <p>
	 * This should be done once initially, then we work in Dasher
	 * co-ordinates for everything else. Input co-ordinates will be
	 * assumed to range over the extent of the screen.
	 * <p>
	 * Internally, we essentially
	 * <ol><li>Apply m_yAutoOffset to the y co-ordinate if this is an Eyetracker
	 * <li>Feed screen co-ordinates through Screen2Dasher with the
	 * appropriate flags (1D and Nonlinear as necessary)
	 * <li>Apply y co-ordinate scaling if this is a 1D device.
	 * </ol>
	 * 
	 * @param iInputX Input x co-ordinate
	 * @param iInputY Input y co-ordinate
	 * @param iType 0 if input co-ordinates are in pixels
	 * or 1 if in Dasher co-ordinates
	 * @param iMode See table above
	 * @return Dasher world point corresponding but not necessarily
	 * in the same place as this input point.
	 */
	public CDasherView.DPoint Input2Dasher(int iInputX, int iInputY, int iType, int iMode) {
		
		// FIXME - need to incorporate one-button mode?
		// First convert the supplied co-ordinates to 'linear' Dasher co-ordinates
			
		CDasherView.DPoint retval = new CDasherView.DPoint();
		
		switch (iType) {
		case 0:
			// Raw secreen coordinates
			
			// TODO - autocalibration should be at the level of the eyetracker filter
			if(iMode == 2) {
				// First apply the autocalibration offset
				iInputY += (int) (m_yAutoOffset);   // FIXME - we need more flexible autocalibration to work with orientations other than left-to-right
			}
			
			if( iMode == 0 )
				retval = Screen2Dasher( iInputX, iInputY, false, true );
			else if( iMode == 1 )
				retval = Screen2Dasher( iInputX, iInputY, true, false );
			else
				retval = Screen2Dasher( iInputX, iInputY, false, true );
			break;
		case 1:
			// Raw dasher coordinates
			
			retval.x = iInputX;
			retval.y = iInputY;
			break;
		default:
			// ERROR
			break;
		}
		
		// Apply y scaling
		
		// TODO: Check that this is still doing something vaguely sensible - I think it isn't
		
		if(iMode == 1 ) {
			if( GetLongParameter(Elp_parameters.LP_YSCALE) > 0 ) {
				
				double dYScale;
				
				if(( realOrientation == Opts.ScreenOrientations.LeftToRight ) || ( realOrientation == Opts.ScreenOrientations.RightToLeft )) {
					dYScale = Screen().GetHeight() / (double)(GetLongParameter(Elp_parameters.LP_YSCALE));
				}
				else {
					dYScale = Screen().GetWidth() / (double)(GetLongParameter(Elp_parameters.LP_YSCALE));
				}
				
				retval.y = (long)((retval.y - lpMaxY/2) * dYScale + lpMaxY/2);
			}
		}
		
		return retval;
	}
	

	/**
	 * Truncate a set of co-ordinates so that they are on the screen
	 *   
	 * @param iX Screen x co-ordinate
	 * @param iY Screen y co-ordinate
	 * @return Truncated point
	 */
	public CDasherView.Point TruncateToScreen(int iX, int iY) {
		
		// I think that this function is now obsolete
		
		CDasherView.Point retval = new CDasherView.Point();
		
		retval.x = iX;
		retval.y = iY;
		
		if(iX < 0)
			retval.x = 0;
		if(iX > Screen().GetWidth())
			retval.x = Screen().GetWidth();
			
			if(iY < 0)
				retval.y = 0;
			if(iY > Screen().GetHeight())
				retval.y = Screen().GetHeight();
		
		return retval;
	}
	
	/**
	 * Calls getInputDasherCoords(mousex, mousey, null)
	 */
	public CDasherView.DPoint getInputDasherCoords() {
		return getInputDasherCoords(null);
	}
	
	/**
	 * Gets the point in the Dasher world currently pointed
	 * to by our input device.
	 * <p>
	 * Internally this boils down to calling GetCoordinates
	 * with the appropriate flags and then feeding the results
	 * through Input2Dasher.
	 * @param Added Ignored, may be null
	 */
	public CDasherView.DPoint getInputDasherCoords(ArrayList<CSymbolProb> Added) {
		
		/* CSFS: int * parameter "pNumDeleted" removed as according to notes in
		 * CDasherView.
		 */ 	
	
		
		// FIXME - rename this something more appropriate (all this really should do is convert the coordinates)
		
		// NOTE - we now ignore the values which are actually passed to the display
		
		// FIXME - Actually turn autocalibration on and off!
		// FIXME - AutoCalibrate should use Dasher co-ordinates, not raw mouse co-ordinates?
		// FIXME - Have I broken this by moving it before the offset is applied?
		// FIXME - put ymap stuff back in 
		
		// FIXME - optimise this
		
		int iCoordinateCount = (GetCoordinateCount());
		
		long[] Coordinates = new long[iCoordinateCount];
		
		int iType = (GetCoordinates(iCoordinateCount, Coordinates));
		
		int mousex, mousey;
		
		if(iCoordinateCount == 1) {
			mousex = 0;
			mousey = (int)Coordinates[0];
		}
		else {
			mousex = (int)Coordinates[0];
			mousey = (int)Coordinates[1];
		}
		
		// TODO: Mode probably isn't being used any more
		
		// Convert the input co-ordinates to dasher co-ordinates
		
		int mode;
		
		if(GetBoolParameter(Ebp_parameters.BP_NUMBER_DIMENSIONS))
			mode = 1;
		else if(GetBoolParameter(Ebp_parameters.BP_EYETRACKER_MODE))
			mode = 2;
		else
			mode = 0;
		
		CDasherView.DPoint retval = Input2Dasher(mousex, mousey, iType, mode);
		
		/* CSFS: As well as extensive replacement of functions which used 
		 * primitives by reference, I've removed code which saved co-ordinates
		 * to m_iDasherXCache as it appears it never gets referenced.
		 */
		
		return retval;
	}
	
	/**
	 * Draws a square highlighting the area between two given y co-ordinates.
	 * <p>
	 * Draws the square a different colour dependent on whether the
	 * 'active' flag is specified.
	 * <p>
	 * At present the colours are hard-coded: Colour 1 is used with
	 * a width of 3 for active, and colour 2 with a width of 1 for
	 * inactive.
	 * 
	 * @param iDasherMin Bottom y co-ordinate at which to align the square
	 * @param iDasherMax Top y co-ordinate at which to align the square
	 * @param bActive Draw this square in 'active' style?
	 */
	public void NewDrawGoTo(long iDasherMin, long iDasherMax, boolean bActive) {
		long iHeight = (iDasherMax - iDasherMin);
		
		int iColour;
		int iWidth;
		
		if(bActive) {
			iColour = 1;
			iWidth = 3;
		}
		else {
			iColour = 2;
			iWidth = 1;
		}
		
		CDasherView.Point[] p = new CDasherView.Point[4];
		
		p[0] = Dasher2Screen( 0, iDasherMin);
		p[1] = Dasher2Screen( iHeight, iDasherMin);
		p[2] = Dasher2Screen( iHeight, iDasherMax);
		p[3] = Dasher2Screen( 0, iDasherMax);
		
		Screen().Polyline(p, 4, iWidth, iColour);
	}
	
//	TODO: Autocalibration should be in the eyetracker filter class
	
	/* CSFS: There were two functions here called ResetSum and ResetSumCounter
	 * I've removed them, their calls, and their relevant variables, since the
	 * Eclipse asserted these variables were only ever set and so were useless.
	 */
	
	/**
	 * Sets m_yAutoOffset back to zero.
	 */
	public void ResetYAutoOffset() {
		m_yAutoOffset = 0;
	}
	
	/**
	 * Sets a new screen, invalidates our current visible region,
	 * and recalls SetScalingFactor, since the relationships between
	 * Dasher and Screen co-ordinates may well have changed at this
	 * point.
	 * <p>
	 * Also caches the Screen's current height in CanvasY.
	 *
	 * @param NewScreen New screen
	 */
	public void ChangeScreen(CDasherScreen NewScreen) {
		m_Screen = NewScreen;
		m_bVisibleRegionValid = false;
		//int Width = Screen().GetWidth();
		int Height = Screen().GetHeight();
		//CanvasX = 9 * Width / 10;
		// CanvasBorder = Width - CanvasX; REMOVED: redundant according to Eclipse.
		CanvasY = Height;
		m_iScalingFactor = 100000000;
		SetScaleFactor();
	}
	
	/**
	 * Gets m_yAutoOffset, the y-coordinate screen offset to be applied
	 * for eyetracker devices.
	 */
	public int GetAutoOffset() {
		return m_yAutoOffset;
	}
	
	/* CSFS: The following method had a large body but returned on its first
	 * line. Presumably this is dead code; deleted and modified its only
	 * calling routine so that it doesn't call it anymore.
	 * 
	public CDasherScreen.Point AutoCalibrate(int mousex, int mousey) {
		return;
	}

	 */
	
	
		
	/* CSFS: Another method which was apparently dead: DrawGameModePointer. Again
	 * I've edited out references to it.
	 */

	/**
	 * Defers to m_ymap.map
	 * 
	 * @see Cymap
	 */
	public double ymap(double x) {
		return m_ymap.map((long)x);
	}
	
	
	// INLINE FUNCTIONS (CDasherViewSquare.inl)
	
	// Redundant function; taken over by Dasher2Screen.
	
	/*public D2Yret dashery2screen(long y1, long y2, int s1, int s2) {

		D2Yret retval = new D2Yret();

		retval.s1 = s1;
		retval.s2 = s2;

		y1 = m_ymap.map(y1);
		y2 = m_ymap.map(y2);

		if(y1 > lpMaxY) {
			retval.size = 0; return retval;
		} if(y2 < 0) {
			retval.size = 0; return retval;
		}

		if(y1 < 0)                  // "highest" legal coordinate to draw is 0.
		{
			y1 = 0;
		}

		// Is this square actually on the screen? Check bottom
		if(y2 > lpMaxY)
			y2 = lpMaxY;

		retval.size = (int)(y2 - y1);

		retval.s1 = (int)(y1 * CanvasY / lpMaxY);
		retval.s2 = (int)(y2 * CanvasY / lpMaxY);

		return retval;

	}*/

	/* CSFS: There used to be another version of dashery2screen accepting only one
	 * long as an argument; however it seemed to be dead code and so I have removed
	 * it.
	 */

	/// Draw the crosshair

	/**
	 * Draws the Crosshair, with the vertical bar at a specified
	 * x co-ordinate and the horizontal bar halfway up the screen.
	 * <p>
	 * The horizontal bar is hard coded to run from 12/14(sx)
	 * to 17/14(sx). This will cause trouble if sx is zero, and
	 * there will be issues if sx is small since we're working
	 * with integers, not floating point.
	 * <p>
	 * This method could do with being rewritten.
	 * 
	 * @param sx X co-ordinate for the vertical bar. Not zero, or
	 * the horizontal bar will not correctly display at present.
	 */
	public void Crosshair(long sx) {
		long[] x = new long[2];
		long[] y = new long[2];

		// Vertical bar of crosshair

		/* CSFS: These used to use the 'old' get-visible-extent functions. Since I
		 * had deleted these, I have converted them to use the new version.
		 */

		CDasherView.DRect visreg = VisibleRegion();

		x[0] = sx;
		y[0] = visreg.minY;

		x[1] = sx;
		y[1] = visreg.maxY;

		if(GetBoolParameter(Ebp_parameters.BP_COLOUR_MODE) == true) {
			DasherPolyline(x, y, 2, 1, 5);
		}
		else {
			DasherPolyline(x, y, 2, 1, -1);
		}

		// Horizontal bar of crosshair

		x[0] = 12 * sx / 14;
		y[0] = lpMaxY / 2;

		x[1] = 17 * sx / 14;
		y[1] = lpMaxY / 2;

		if(GetBoolParameter(Ebp_parameters.BP_COLOUR_MODE) == true) {
			DasherPolyline(x, y, 2, 1, 5);
		}
		else {
			DasherPolyline(x, y, 2, 1, -1);
		}
	}

	/**
	 * Reverse the x co-ordinate nonlinearity.
	 * <p>
	 * For details of this non-linearity, see the constructor.
	 * 
	 * @param x Value to which the mapping should be unapplied
	 * @return Raw value
	 */
	public double unapplyXMapping(double x) // invert x non-linearity
	{
		if(x < m_dXMappingLogLinearBoundary * m_dXMappingLinearScaleFactor)
			return x / m_dXMappingLinearScaleFactor;
		else
			return m_dXMappingLogLinearBoundary - m_dXMappingLogarithmicScaleFactor + m_dXMappingLogarithmicScaleFactor * Math.exp((x / m_dXMappingLinearScaleFactor - m_dXMappingLogLinearBoundary) / m_dXMappingLogarithmicScaleFactor);

	}

	/**
	 * Apply the x co-ordinate nonlinearity.
	 * <p>
	 * For details of this non-linearity, see the constructor.
	 * 
	 * @param x Value to which the mapping should be applied
	 * @return Mapped value
	 */
	public double applyXMapping(double x) 
	// x non-linearity
	{
		if(x < m_dXMappingLogLinearBoundary)
			return m_dXMappingLinearScaleFactor * x;
		else
			return m_dXMappingLinearScaleFactor * (m_dXMappingLogarithmicScaleFactor * Math.log((x + m_dXMappingLogarithmicScaleFactor - m_dXMappingLogLinearBoundary) / m_dXMappingLogarithmicScaleFactor) + m_dXMappingLogLinearBoundary);
	}

}


/**
 * Exception to be raised to signal that a given Node was not
 * drawable.
 */
class NodeCannotBeDrawnException extends Exception {
	/**
	 * The Node which could not be drawn
	 */
	protected CDasherNode WhichNode;

	/**
	 * Default constructor; creates the exception but does not
	 * store a Node.
	 *
	 */
	public NodeCannotBeDrawnException() {
		WhichNode = null;
	}

	/**
	 * Creates a new exception with a reference to the Node
	 * which could not be drawn.
	 * 
	 * @param thenode Problem Node.
	 */
	public NodeCannotBeDrawnException(CDasherNode thenode) {
		WhichNode = thenode;
	}

	/**
	 * Retrieves the Node which couldn't be drawn.
	 * 
	 * @return Reference to undrawable Node.
	 */
	public CDasherNode GetNode() {
		return WhichNode;
	}
}