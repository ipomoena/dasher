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

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.JTextArea;

/**
 * JDasherEdit is essentially an ordinary JTextArea with an added
 * event handler to respond to Dasher's Edit Events. It responds
 * to EditEvents to update the contents of the text box, and to
 * EditContextEvents to supply a context if it knows of one.
 * 
 */
public class JDasherEdit extends JTextArea implements CaretListener {
	
	/**
	 * Interface which this EditBox will control when text is
	 * entered, typically by calling InvalidateContext if the
	 * user manually edits the text.
	 */
	private dasher.CDasherInterfaceBase m_Interface;
	
	/**
	 * Where the TextArea's Dot was last event; useful in determining
	 * what kind of edit the user has made. 
	 */
	private int iPrevDot;
	
	/**
	 * Where the TextArea's Mark was last event; useful in determining
	 * what kind of edit the user has made. 
	 */
	private int iPrevMark;
	
	/**
	 * Flag to supress events which we generate by acting on the
	 * Dasher interface. Duplicate events may be generated because
	 * we listen to both Dasher's internal event system and also
	 * Java's EditEvents to spot when the user makes changes
	 * to the TextArea's contents.
	 */
	private boolean supressNextEvent;
	
	/**
	 * Creates a new EditBox with a given width and height in characters
	 * and with the ability to control a given interface.
	 * <p>
	 * The interface can be null; in this case user changes to the
	 * edit box will not be reflected in Dasher's behaviour.
	 * 
	 * @param rows EditBox width in characters
	 * @param cols EditBox height in characters
	 * @param Interface Interface to control
	 */
	public JDasherEdit(int rows, int cols, dasher.CDasherInterfaceBase Interface) {
		super(rows, cols);
		m_Interface = Interface;
		
		this.addCaretListener(this);
	}
	
	/**
	 * Handles a given event; typically called by Dasher to signal
	 * text edit events.
	 * <p>
	 * If text is added whilst there is a selection, we delete it
	 * just as if the user had typed over the selection;
	 * likewise removing a character whilst something is selected
	 * removes both the selection and its preceding character.
	 * <p>
	 * When asked for context with an EditContextEvent, the EditBox
	 * responds with the x characters before the caret.
	 * 
	 * @param event Event to be handled
	 */	
	public void handleEvent(dasher.CEvent event) {
		if(event.m_iEventType == 2) {  // EV_EDIT
			
			dasher.CEditEvent evt = (dasher.CEditEvent)event;
			if(evt.m_iEditType == 1) { // New text!
				
				if(this.getSelectedText() != "") {
					supressNextEvent = true;
					this.replaceSelection("");
				}
				supressNextEvent = true;
				int oldCaret = this.getCaretPosition();
				this.insert(evt.m_sText, oldCaret);
				this.setCaretPosition(oldCaret + 1);
				
			}
			
			else if(evt.m_iEditType == 2) { // Delete text
				if(this.getText() != "") {

					supressNextEvent = true;

					if(this.getSelectedText() != "") {
						this.replaceSelection("");
					}

					supressNextEvent = true;
					this.replaceRange("", this.getCaretPosition() - (evt.m_sText.length()), this.getCaretPosition());
				}
			}
			
			/* The supressNextEvent attribute is to permit us to differentiate
			 * between events originated in Dasher and those originated by the
			 * edit box. Dasher-originated DocumentEvents will be immediately preceded
			 * by an EDIT_EVENT, so we should ignore them.
			 */
			
		}
		
		if(event.m_iEventType == 3) { // Request for context
			
			dasher.CEditContextEvent evt = (dasher.CEditContextEvent)event;
			
			String NewContext;
			
			int StartPosition = java.lang.Math.max(0, this.getSelectionStart() - evt.m_iMaxLength);
			
			try {
				if(StartPosition == 0) {
					NewContext = this.getText(0, this.getSelectionStart());
				}
				else {
					NewContext = this.getText(StartPosition, evt.m_iMaxLength);
				}
			}
			catch(Exception e) {
				return;
			}
			
			evt.newContext = NewContext;
						
		}
	}

	/**
	 * Called by Java's event handling subsystem when the user moves
	 * the caret, either by typing or clicking.
	 * <p>
	 * If the supressEvent flag is false, we respond by invalidating
	 * our current context so that the interface will request a new one,
	 * which we will respond to with that which is appropriate
	 * to the new position.
	 * <p>
	 * If the supressEvent flag is true we set it to false and
	 * do nothing else.
	 * <p>
	 * If m_Interface is null, nothing is ever done. 
	 */
	public void caretUpdate(CaretEvent e) {
		
		if(e.getDot() == iPrevDot && e.getMark() == iPrevMark) return;
		
		if(!supressNextEvent && m_Interface != null) {
			
			m_Interface.InvalidateContext(true);
			// Used to be false. Changed so we can type when at zero-length.
			
		}
		else {
			// Event caused by Dasher performing modifications; ignore.
			supressNextEvent = false;
		}
		
		iPrevDot = e.getDot();
		iPrevMark = e.getMark();
		
	}	
}
