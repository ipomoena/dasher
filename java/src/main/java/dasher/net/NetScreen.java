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

import java.util.ArrayList;

import dasher.CCustomColours;
import dasher.CDasherView;
import dasher.EColorSchemes;

import java.awt.Color;

import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.Queue;
import java.util.LinkedList;

/**
 * Specialised implementation of CDasherScreen which, instead of
 * drawing its output to the screen, produces an XML document
 * describing drawing primitives.
 * <p>
 * The XML is then serialized and output to a specified
 * OutputStream (in the context of NetDasher typically a Socket's
 * OutputStream) with the intent that some client application
 * should render the drawing primitives described by the document
 * and so reproduce the display.
 * <p>
 * The class also currently plays the role of injecting XML tags
 * informing the client of EditEvents and of delivering some
 * extra data in the first frame sent to a given client. 
 * In the future, these roles should ideally be moved into a 
 * seperate class such that the different classes which want 
 * to deliver XML to the client could each create an Element, 
 * and combination and serialization could be co-ordinated by 
 * some master NetDasherOutput class.
 * <p>
 * <b>XML format</b>
 * <p>The entire document is always surrounded by <pre><message>...</message></pre>
 * tags.
 * <p>
 * <b>init</b>: Sent only with the first produced frame, to notify
 * the client of initial conditions. Contains the following children:
 * <p><i>sessid</i>: Contains the session ID associated with
 * this newly created Dasher.
 * <p><i>alphabet</i>: An &lt;alphabet/> tag is included
 * for each alphabet Dasher knows about. The title of each is
 * described in the tag's name attribute, and the default attribute
 * will be 'true' for one alphabet, indicating that which Dasher
 * is using by default. Example: %lt;alphabet name="French" default="true"/>
 * <p><i>colour</i>: A &lt;colour/> tag is included
 * for each colour scheme Dasher knows about. The title of each is
 * described in the tag's name attribute, and the default attribute
 * will be 'true' for one alphabet, indicating that which Dasher
 * is using by default. Example: &lt;colour name="Rainbow" default="true"/>
 * <p>
 * <b>edit</b>: Records every EditEvent which has been received
 * by this Screen since the last frame.
 * <p>
 * The children of edit are of the form:
 * <p>
 * <i>add</i>: Add tags surround text added since the last frame;
 * the tags are recorded in the XML document in the same order
 * as the events occured. Example: &lt;add>H&lt;/add>
 * <p>
 * <i>remove</i>: Have a single attribute named 'num' which indicates
 * how many characters have been deleted.
 * Example: &lt;remove num="3"/>
 * <p>
 * <b>frame</b>: Frame tags contain a sequence of tags describing
 * drawing primitives. The tags which may occur here are each
 * described in the documentation for the function which may
 * produce them, below.
 */
public class NetScreen implements dasher.CDasherScreen {

	/**
	 * Flag indicating whether to output an init section with
	 * our next frame.
	 */
	protected boolean firstMessage = true;
	
	/**
	 * Document object which should be populated with data to
	 * be sent and which will in due time be serialized to our
	 * OutputStream.
	 */
	protected Document drawInstructions;
	
	/**
	 * Element from which all other elements descend; named
	 * 'message' in the document schema.
	 */
	protected Element root;
		
	/**
	 * Element to which all drawing primitives should be attached.
	 */
	protected Element frame;
	
	/**
	 * Colour scheme currently in use
	 */
	protected dasher.CCustomColours m_ColourScheme;
	
	/**
	 * OutputStream to which XML should be serialized at the end
	 * of the drawing cycle.
	 */
	protected java.io.OutputStream m_Output;
	
	/**
	 * Session ID associated with the Dasher to which this Screen
	 * is linked.
	 */
	protected int sessID;
	
	/**
	 * Queue of EditEvents registered since the last frame was drawn.
	 */
	protected Queue<dasher.CEditEvent> editEventQueue;
	
	/**
	 * List of available alphabets
	 */
	protected ArrayList<String> Alphabets;
	
	/**
	 * List of available colour schemes
	 */
	protected ArrayList<String> Colours;
	
	/**
	 * Default colour scheme
	 */	
	protected String initialCS;
	
	/**
	 * Default alphabet name
	 */
	protected String initialAlph;
	
	/**
	 * DocumentBuilder to be used in producing XML
	 */
	protected DocumentBuilder parser;
	
	/**
	 * Transformer which will render the XML document as text
	 * prior to output.
	 */	
	protected Transformer XMLSerializer;
	
	/**
	 * Width of this screen in pixels
	 */
	protected int m_Width;
	
	/**
	 * Height of this screen in pixels
	 */
	protected int m_Height;
	
	/**
	 * Creates a new NetScreen.
	 * <p>
	 * Sets up a DocumentBuilder and Transformer for the production
	 * of XML and saves the height, width and session ID associated
	 * with this screen.
	 * 
	 * @param sessID Session ID
	 * @param width Screen width in pixels
	 * @param height Screen height in pixels
	 */
	public NetScreen(int sessID, int width, int height) {
		
		this.sessID = sessID;
		editEventQueue = new LinkedList<dasher.CEditEvent>();
		m_Width = width; m_Height = height;
		
		// Create instance of DocumentBuilderFactory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// Get the DocumentBuilder
		try {
			parser = factory.newDocumentBuilder();
		}
		catch(Exception e) {
			System.out.printf("Couldn't create XML parser: %s%n", e);
		}
		
		TransformerFactory tranFactory = TransformerFactory.newInstance();
		try {
			XMLSerializer = tranFactory.newTransformer();
		}
		catch(TransformerConfigurationException e) {
			System.out.printf("Failed to create XML transformer: %s%n", e);
			return;
		}
		
		
	}
	
	/**
	 * Signals the start of a new frame; see CDasherScreen for
	 * the general contract of this method.
	 * <p>
	 * In this specific case, a new Document is created and stored
	 * in drawInstructions, and required Elements message and frame are
	 * created and stored in root and frame respectively for use
	 * by other means.
	 * <p>
	 * Finally, if the firstMessage flag is true, the &lt;init>
	 * section is created as described above.
	 */
	public void Blank() {
		
		try {

			// Create blank DOM Document
			drawInstructions = parser.newDocument();
			
			root = drawInstructions.createElement("message");
			drawInstructions.appendChild(root);
			
			if(firstMessage) {
				Element init = drawInstructions.createElement("init");
				
				init.setAttribute("sessid", Integer.toString(sessID));
				
				root.appendChild(init);
				
				for(String alph : Alphabets) {
					Element newAlph = drawInstructions.createElement("alphabet");
					
					newAlph.setAttribute("name", alph);
					
					if(alph.equals(initialAlph)) {
						newAlph.setAttribute("default", "true");
					}
					
					init.appendChild(newAlph);
				}
				
				for(String col : Colours) {
					Element newCol = drawInstructions.createElement("colour");
					
					newCol.setAttribute("name", col);
					
					if(col.equals(initialCS)) {
						newCol.setAttribute("default", "true");
					}
					
					init.appendChild(newCol);
				}
				
				firstMessage = false;
			}
			
			frame = drawInstructions.createElement("frame");
			root.appendChild(frame);
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
		
	}

	/**
	 * Calls addEditEvents before serializing the XML document
	 * produced during the drawing cycle and writing it to m_Output,
	 * (an OutputStream specified by the setOutput method).
	 * <p>
	 * For the general contract of this method, see CDasherScreen.
	 */
	public void Display() {

		addEditEvents();		
		
		
		Source src = new DOMSource(drawInstructions);
		Result dest = new StreamResult(m_Output);
		try {
			XMLSerializer.transform(src, dest);
		}
		catch(TransformerException e) {
			System.out.printf("Failed to output XML: %s%n", e);
			return;
		}
		
		try {
			m_Output.flush();
		}
		catch(java.io.IOException e) {
			System.out.printf("Failed to flush output: %s%n", e);
		}
		
		
	}
	
	/**
	 * Sets the list of available alphabets to be recorded
	 * during the production of the &lt;init> section
	 * produced for the first frame. 
	 * 
	 * @param Alphs List of available alphabets
	 */
	public void setAlphabets(ArrayList<String> Alphs) {
		Alphabets = Alphs;
	}
	
	/**
	 * Sets the list of available colour schemes to be recorded
	 * during the production of the &lt;init> section
	 * produced for the first frame. 
	 * 
	 * @param Cols List of available colour schemes
	 */
	public void setColours(ArrayList<String> Cols) {
		Colours = Cols;
	}
	
	/**
	 * Records an EditEvent to be added to the XML document
	 * during a call to Display().
	 * 
	 * @param event New EditEvent
	 */
	public void addEditEvent(dasher.CEditEvent event) {
		
		editEventQueue.add(event);
		
	}
	
	/**
	 * Adds all EditEvents recorded since the last frame to the
	 * XML document, in the manner described in the class summary.
	 */
	protected void addEditEvents() {
		dasher.CEditEvent event;
		
		Element edits = drawInstructions.createElement("edit");
		root.appendChild(edits);
		
		while((event = editEventQueue.poll()) != null) {
			
			if(event.m_iEditType == 1) { // add text
				Element add = drawInstructions.createElement("add");
				edits.appendChild(add);
				
				Text addtext = drawInstructions.createTextNode(event.m_sText);
				add.appendChild(addtext);
			}
			if(event.m_iEditType == 2) { // remove text
				Element remove = drawInstructions.createElement("remove");
				
				remove.setAttribute("num", Integer.toString(event.m_sText.length()));
				edits.appendChild(remove);
			}
			
		}
	}

	/**
	 * Adds a tag of the form &lt;circle centrex="..." centrey="..." radius="..." fill="..." colour="..."/>
	 * to the Frame tag of this document.
	 * <p>
	 * The colour attributes are recorded in standard #RRGGBB format.
	 */
	public void DrawCircle(int iCX, int iCY, int iR, int iColour, boolean bFill) {
		Element newCircle = drawInstructions.createElement("circle");
		
		newCircle.setAttribute("centrex", Integer.toString(iCX));
		newCircle.setAttribute("centrey", Integer.toString(iCY));
		newCircle.setAttribute("radius", Integer.toString(iR));
		newCircle.setAttribute("fill", Boolean.toString(bFill));
		newCircle.setAttribute("colour", getColour(iColour));
		
		frame.appendChild(newCircle);
		
	}

	/**
	 * Adds a tag of the form &lt;rect x="..." y="..." w="..." h="..." colour="..." olcolour="..." thickness="..." fill="..." drawoutline="..."/>
	 * to the Frame tag of this document.
	 * <p>
	 * The colour attributes are recorded in standard #RRGGBB format.
	 */	
	public void DrawRectangle(int x1, int y1, int x2, int y2, int Color, int iOutlineColour, EColorSchemes ColorScheme, boolean bDrawOutline, boolean bFill, int iThickness) {
		
		// Assuming our client wants co-ordinates for a top-left corner
		
		Element newRect = drawInstructions.createElement("rect");
		
		newRect.setAttribute("x", Integer.toString(x1));
		newRect.setAttribute("y", Integer.toString(y2));
		newRect.setAttribute("w", Integer.toString(Math.abs(x2 - x1)));
		newRect.setAttribute("h", Integer.toString(Math.abs(y2 - y1)));
		newRect.setAttribute("colour", getColour(Color));
		newRect.setAttribute("olcolour", getColour(iOutlineColour));
		newRect.setAttribute("thickness", Integer.toString(iThickness));
		
		newRect.setAttribute("fill", Boolean.toString(bFill));
		newRect.setAttribute("drawoutline", Boolean.toString(bDrawOutline));
		
		frame.appendChild(newRect);
	}

	/**
	 * Adds a tag of the form &lt;string size="..." x="..." y="...">The String&lt;/string>
	 * to the Frame tag of this document.
	 * <p>
	 * The co-ordinates describe the top-left corner of a bounding rectangle.
	 */
	public void DrawString(String string, int x1, int y1, long Size) {
		Element newString = drawInstructions.createElement("string");
		
		newString.setAttribute("size", Long.toString(Size));
		newString.setAttribute("x", Integer.toString(x1));
		newString.setAttribute("y", Integer.toString(y1));
				
		Text stringText = drawInstructions.createTextNode(string);
		
		newString.appendChild(stringText);
		
		frame.appendChild(newString);
		
	}

	public int GetHeight() {
		return m_Height;
	}

	public int GetWidth() {
		return m_Width;
	}

	/**
	 * See Polygon(CDasherView.Point[], int, int, int, boolean)
	 */
	public void Polygon(CDasherView.Point[] Points, int Number, int Color, int iWidth) {
		
		Polygon(Points, Number, Color, iWidth, true);
		
	}
	
	/**
	 * Adds a polygon to the XML document in the following format:
	 * <p>
	 * &lt;poly colour="..." fill="..." thickness="...">
	 * &lt;point x="..." y="..."/>
	 * &lt;point x="..." y="..."/>
	 * ...
	 * &lt;/poly>
	 * 
	 * @param Points Array of Points constituting the polygon
	 * @param Number Number of points in the polygon
	 * @param Color Outline colour
	 * @param iWidth Outline width
	 * @param fill Fill colour
	 */
	protected void Polygon(CDasherView.Point[] Points, int Number, int Color, int iWidth, boolean fill) {
		
		Element newPoly = drawInstructions.createElement("poly");
		newPoly.setAttribute("colour", getColour(Color));
		newPoly.setAttribute("thickness", Integer.toString(iWidth));
		newPoly.setAttribute("fill", Boolean.toString(fill));
		
		for(CDasherView.Point point : Points) {
			Element newPoint = drawInstructions.createElement("point");
			newPoint.setAttribute("x", Integer.toString(point.x));
			newPoint.setAttribute("y", Integer.toString(point.y));
			newPoly.appendChild(newPoint);
		}
		
		frame.appendChild(newPoly);
		
	}
	
	/**
	 * See Polygon(CDasherView.Point[], int, int, int, boolean)
	 */
	public void Polygon(CDasherView.Point[] Points, int Number, int Color) {

		Polyline(Points, Number, Color, 1);
		
	}

	/**
	 * See Polygon(CDasherView.Point[], int, int, int, boolean)
	 */
	public void Polyline(CDasherView.Point[] Points, int Number, int iWidth, int Colour) {
		
		Polygon(Points, Number, Colour, iWidth, false);
		
	}

	/**
	 * See Polygon(CDasherView.Point[], int, int, int, boolean)
	 */
	public void Polyline(CDasherView.Point[] Points, int Number, int iWidth) {
		
		Polyline(Points, Number, iWidth, 0);
		
	}

	/**
	 * Stub. For the general contract of this method, see
	 * CDasherScreen.
	 */
	public void SendMarker(int iMarker) {
				
	}

	public void SetColourScheme(CCustomColours ColourScheme) {
		
		m_ColourScheme = ColourScheme;
		
	}

	public CDasherView.Point TextSize(String string, int Size) {
		
		CDasherView.Point retval = new CDasherView.Point();
		retval.x = 10;
		retval.y = 10;
		
		return retval;
		
	}
	
	/**
	 * Transforms a Dasher colour identifier into an HTML-style
	 * #RRGGBB expression describing said colour.
	 * 
	 * @param iColour Colour index
	 * @return HTML colour
	 */
	protected String getColour(int iColour) {
		if(iColour == -1) iColour = 3;
		
		Color drawColour = new Color(m_ColourScheme.GetRed(iColour), m_ColourScheme.GetGreen(iColour), m_ColourScheme.GetBlue(iColour));
		
		int colournumber = drawColour.getRGB();
		
		return "#" + Integer.toHexString(colournumber).substring(2);
	}
	
	/**
	 * Sets the output stream to which XML should be serialized.
	 * <p>
	 * Typically this will be set prior to calling NewFrame, but it
	 * will take effect if set at any time before Display() is called.
	 * 
	 * @param out OutputStream to which the next frame's XML should be written
	 */
	public void setOutput(java.io.OutputStream out) {
		m_Output = out;
	}
	
	/**
	 * Sets the default alphabet to be reported when this
	 * Screen draws its first frame.
	 * 
	 * @param alph Default alphabet
	 */
	public void setInitialAlph(String alph) {
		initialAlph = alph;
	}
	
	/**
	 * Sets the default colour scheme to be reported when this
	 * Screen draws its first frame.
	 * 
	 * @param CS Default colour scheme
	 */
	public void setInitialCS(String CS) {
		initialCS = CS;
	}

}
