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

package dasher.applet.font;

import javax.swing.BoxLayout;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JPanel;

public class JFontDialog extends JDialog implements ActionListener {

	private JList m_FontList;
	private JList m_SizeList;
	private JList m_StyleList;
	private FontListener m_Owner;
	private Font originalFont;
	
	public JFontDialog(FontListener callback) {
		super();
		
		m_Owner = callback;
		originalFont = null;
		
		createDialog();
		
	}
	
	public JFontDialog(FontListener callback, Font currentFont) {
		super();
		
		m_Owner = callback;
		originalFont = currentFont;
		
		createDialog();
		
	}
	
	public void createDialog() {
		
		this.setSize(330, 375);
		this.setResizable(false);
							
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
		this.add(listPanel);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
		this.add(buttonPanel);
		
		m_FontList = new JList(java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		JScrollPane FontPane = new JScrollPane(m_FontList);
		Dimension FontPaneSize = new Dimension(150, 300);
		FontPane.setSize(FontPaneSize); FontPane.setPreferredSize(FontPaneSize);
		listPanel.add(FontPane);
		
		if(originalFont != null) m_FontList.setSelectedValue(originalFont.getFamily(), true);
		
		Integer[] sizes = {8,9,10,11,12,14,16,18,20,22,24,26,28,36,48,72};
		m_SizeList = new JList(sizes);
		JScrollPane SizePane = new JScrollPane(m_SizeList);
		Dimension SizePaneSize = new Dimension(50, 300);
		SizePane.setSize(SizePaneSize); SizePane.setPreferredSize(SizePaneSize);
		listPanel.add(SizePane);
		
		if(originalFont != null) m_SizeList.setSelectedValue(originalFont.getSize(), true);
		
		String[] styles = {"Normal","Bold","Italic","Bold Italic"};
		m_StyleList = new JList(styles);
		JScrollPane StylePane = new JScrollPane(m_StyleList);
		Dimension StylePaneSize = new Dimension(100, 300);
		StylePane.setSize(StylePaneSize); StylePane.setPreferredSize(StylePaneSize);
		listPanel.add(StylePane);
				
		if(originalFont != null ) { 
			int currentStyle = originalFont.getStyle();
			if(currentStyle == Font.PLAIN) {
				m_StyleList.setSelectedValue("Normal", true);
			}
			else if(currentStyle == Font.BOLD) {
				m_StyleList.setSelectedValue("Bold", true);
			}
			else if(currentStyle == Font.ITALIC) {
				m_StyleList.setSelectedValue("Italic", true);
			}
			else {
				m_StyleList.setSelectedValue("Bold Italic", true);
			}
		}
		
		
		Dimension ButtonSize = new Dimension(70, 25);
		
		JButton OKButton = new JButton("OK");
		OKButton.setSize(ButtonSize); OKButton.setPreferredSize(ButtonSize);

		OKButton.addActionListener(this);
		
		buttonPanel.add(OKButton);
		
		JButton CancelButton = new JButton("Cancel");
		CancelButton.setSize(ButtonSize); CancelButton.setPreferredSize(ButtonSize);
		
		CancelButton.addActionListener(this);
		
		buttonPanel.add(CancelButton);
		
		this.setVisible(true);
		
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand() == "OK") {
			if(m_FontList.getSelectedValue() == null) {
				this.setVisible(false);
				return;
			}
			if(m_SizeList.getSelectedValue() == null) {
				m_SizeList.setSelectedValue(Integer.toString(originalFont.getSize()), false);
				if(m_SizeList.getSelectedValue() == null) {
					m_SizeList.setSelectedValue(10, false);
				}
			}
			if(m_StyleList.getSelectedValue() == null) {
				m_StyleList.setSelectedValue("Normal", false);
			}
			
			m_Owner.setNewFont(new Font(m_FontList.getSelectedValue().toString(), translateStyle(m_StyleList.getSelectedValue().toString()), (Integer)m_SizeList.getSelectedValue()));
			this.setVisible(false);
		}
		else if(e.getActionCommand() == "Cancel") {
			this.setVisible(false);
		}
	}
	
	public int translateStyle(String in) {
		if(in == "Normal") return Font.PLAIN;
		if(in == "Bold") return Font.BOLD;
		if(in == "Italic") return Font.ITALIC;
		return Font.BOLD | Font.ITALIC;
	}
	
}
