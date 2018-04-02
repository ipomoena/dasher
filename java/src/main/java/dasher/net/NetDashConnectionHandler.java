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

import java.io.OutputStream;
import java.util.ArrayList;

import java.util.concurrent.locks.*;

import dasher.CEditEvent;

/**
 * Wraps an instance of NetDasher and passes commands to it
 * when asked to by a DasherCommandInterpreterThread.
 */
public class NetDashConnectionHandler implements DasherEditListener {

	/**
	 * Dasher we are wrapping
	 */
	private NetDasher Dasher;
	
	/**
	 * Screen attached to this Dasher
	 */
	private NetScreen Screen;
	
	/**
	 * System time when this Dasher was last accessed; used to
	 * determine if this session is idle by NetDashGC
	 */
	private long lastUsed;
	
	/**
	 * Session identifier associated with this instance
	 */
	private int sessID;
	
	/**
	 * Flag indicating if this Dasher has been destroyed by the
	 * GC thread.
	 */
	private boolean destroyed;
	
	/**
	 * Lock which is set when a thread is operating on this
	 * Dasher in some way.
	 */
	private Lock myLock;
	
	/**
	 * Creates and wraps a new instance of NetDasher and
	 * creates and registers our helper classes including a NetScreen
	 * for output and a NetInput for input.
	 * 
	 * @param sessID Session identifer associated with this handler
	 * @param width Screen width to draw for
	 * @param height Screen height to draw for
	 * @param dataLocation Where to look for alphabet and colour XML files
	 * @param alphIO AlphIO object holding alphabet data 
	 * @param colIO ColourIO object holding colour data
	 */	
	public NetDashConnectionHandler(int sessID, int width, int height, String dataLocation, dasher.CAlphIO alphIO, dasher.CColourIO colIO) {
				
		myLock = new ReentrantLock();
		
		Dasher = new NetDasher(this, alphIO, colIO);
		
		Screen = new NetScreen(sessID, width, height);
		
		this.sessID = sessID;
		
		if(dataLocation != null) Dasher.setUserLoc(dataLocation);
		
		Dasher.Realize();
		
		Dasher.SetStringParameter(dasher.Esp_parameters.SP_INPUT_FILTER, "Click Mode");
		Dasher.SetStringParameter(dasher.Esp_parameters.SP_INPUT_DEVICE, "Network Input");
		Dasher.SetBoolParameter(dasher.Ebp_parameters.BP_DRAW_MOUSE_LINE, false);
		Dasher.SetBoolParameter(dasher.Ebp_parameters.BP_DRAW_MOUSE, false);
		Dasher.SetLongParameter(dasher.Elp_parameters.LP_ZOOMSTEPS, 1);
		
		Screen.setInitialAlph(Dasher.GetStringParameter(dasher.Esp_parameters.SP_ALPHABET_ID));
		Screen.setInitialCS(Dasher.GetStringParameter(dasher.Esp_parameters.SP_COLOUR_ID));
		
		ArrayList<String> alphs = new ArrayList<String>();
		
		Dasher.GetAlphabets(alphs);
		Screen.setAlphabets(alphs);
		
		ArrayList<String> cols = new ArrayList<String>();
		
		Dasher.GetColours(cols);
		Screen.setColours(cols);
		
		Dasher.ChangeScreen(Screen);
		
	}
	
	/**
	 * Reports a mouse click to our hosted Dasher; this should
	 * be called by the command interpreter when the client sends
	 * a document reporting a click.
	 * <p>
	 * Internally we pass the co-ordinates on using NetDasher's
	 * setCoordinates method, call KeyDown to report a click, set
	 * the Screen's OutputStream, and finally calls NewFrame so that
	 * a frame is drawn to NetScreen and passes out through the
	 * given OutputStream.
	 * <p>
	 * Finally, our internal lastUsed field is updated to note
	 * the time when the user last interacted with this session.
	 * 
	 * @param x Mouse x co-ordinate
	 * @param y Mouse y co-ordinate
	 * @param output Stream to which drawn XML will be output
	 */
	public void Clicked(int x, int y, OutputStream output) {		
		
		Dasher.setCoordinates(x, y);		
		Dasher.KeyDown(0, 100);

		Screen.setOutput(output);
				
		Dasher.NewFrame(0);
		
		lastUsed = System.currentTimeMillis();

	}
	
	/**
	 * Forces Dasher to draw a frame to a given output stream
	 * immediately.
	 * <p>
	 * There is no need to call this when reporting a mouse-click
	 * as a frame will automatically be drawn as part of the process.
	 * <p>
	 * Finally, our internal lastUsed field is updated to note
	 * the time when the user last interacted with this session.
	 * 
	 * @param output OutputStream to which we should output XML
	 */
	public void Draw(OutputStream output) {
		
		Screen.setOutput(output);
		
		Dasher.Draw(true);
		
		lastUsed = System.currentTimeMillis();
		
	}
	
	/**
	 * Called by the wrapped NetDasher instance to report an EditEvent
	 * because the user has entered or left a node.
	 * <p>
	 * This method just passes it on to the Screen to incorporate
	 * into the next frame -- given the timing of EditEvents during
	 * the drawing cycle, this will almost certainly be the same as the
	 * frame emitted in response to the user interaction which spawned
	 * the edit event in the first place, producing the illusion
	 * of almost-instantaneous response.
	 * 
	 * @param event Event being reported
	 */
	public void HandleEvent(CEditEvent event) {
	
		Screen.addEditEvent(event);
		
	}

	/**
	 * Uses the lastUsed timestamp held internally to determine
	 * whether this session has been idle for 5 minutes or more.
	 * 
	 * @return True if idle
	 */
	public boolean isIdle() {
		
		return (System.currentTimeMillis() - lastUsed > 30000); // 5 minute timeout

	}
	
	/**
	 * Reports whether this session has in fact been destroyed
	 * by the garbage collector thread; this is to guard against
	 * situations where a reference to a collected
	 * object has somehow been held.
	 * <p>
	 * Any class which finds itself holding a destroyed reference
	 * should behave as if it failed to get the reference in the
	 * first place; typically it indicates a session expired whilst
	 * a thread was waiting on a lock. 
	 * 
	 * @return True if destroyed
	 */
	public boolean isDestroyed() {
		return destroyed;
	}
	
	/**
	 * Invoked by the garbage collector thread to destory the
	 * Interface of our wrapped NetDasher, freeing the vast majority
	 * of memory used.
	 * <p>
	 * After destroying an instance, all references to it should
	 * be nulled to permit the Java garbage collector to reclaim
	 * this wrapper.
	 *
	 */
	public void destroy() {
		destroyed = true;
		Dasher.DestroyInterface();
	}
	
	/**
	 * Gets the session ID associated with this instance
	 * 
	 * @return SessID
	 */
	public int getSessID() {
		return this.sessID;
	}
	
	/**
	 * Retrieves a reference to the Lock used to prevent concurrent
	 * modification of this instance of NetDasher.
	 * 
	 * @return myLock
	 */
	public Lock getLock() {
		return myLock;
	}
	
	/**
	 * Changes the alphabet used by our wrapped Dasher.
	 * <p>
	 * This does not cause drawing automatically; Draw should
	 * be called afterwards if this is desired.
	 * 
	 * @param alph Name of new alphabet
	 */
	public void changeAlphabet(String alph) {
		
		System.out.printf("Session #%d: retraining for alphabet '%s'%n", sessID, alph);
		
		Dasher.SetStringParameter(dasher.Esp_parameters.SP_ALPHABET_ID, alph);
		
		lastUsed = System.currentTimeMillis();
	}
	
	/**
	 * Changes the colour scheme used by our wrapped Dasher.
	 * <p>
	 * This does not cause drawing automatically; Draw should
	 * be called afterwards if this is desired.
	 * 
	 * @param col Name of new colour scheme
	 */
	public void changeColours(String col) {
				
		Dasher.SetStringParameter(dasher.Esp_parameters.SP_COLOUR_ID, col);
		
		lastUsed = System.currentTimeMillis();
	}

}
