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

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.BoxLayout;

/**
 * Small JFrame containing a label and progress bar which is shown
 * by Dasher when training or otherwise Locked.
 * <p>
 * At present this doesn't fully work because the Frame's screen
 * is not redrawn whilst another event is in progress -- and most
 * time consuming work in Dasher takes place in response to menu
 * events.
 * <p>
 * This could potentially be fixed by using marking events triggered
 * by menu clicks as to be done by the redraw thread.
 */
public class ScreenOverlay extends JFrame {

	/**
	 * Our progress bar
	 */
	private JProgressBar m_ProgressBar;
	
	/**
	 * Our label
	 */
	private JLabel m_Label;
	
	/**
	 * Creates the frame with a size of 200x100 and currently invisible.
	 *
	 */
	public ScreenOverlay() {
		
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		m_Label = new JLabel("JDasher");
		m_ProgressBar = new JProgressBar();
		
		m_ProgressBar.setVisible(false);
		
		this.setSize(200, 100);
		
		this.add(m_Label);
		this.add(m_ProgressBar);
		
	}
	
	/**
	 * Sets the Frame's label
	 * 
	 * @param newMessage Message to show the user
	 */
	public void setText(String newMessage) {
		m_Label.setText(newMessage);
	}
	
	/**
	 * Sets the current progress
	 * 
	 * @param done Amount done
	 * @param max Maximum amount
	 */	
	public void setProgress(int done, int max) {
		m_ProgressBar.setMaximum(max);
		m_ProgressBar.setValue(done);
	}
	
	/**
	 * Shows or hides the progress bar
	 * 
	 * @param visible Should the bar be visible?
	 */
	public void setProgressBarVisible(boolean visible) {
		m_ProgressBar.setVisible(visible);
	}
	
}
