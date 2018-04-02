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

import java.awt.datatransfer.FlavorEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

/** 
 * The menu bar is entirely dumb. Its purpose is to do the donkey work of setting
 * up the menus; it then simply listens for events and calls the appropriate methods
 * belonging to its host. It is the responsibility of the host to ensure it is
 * properly set up to begin with, including setting which options are selected
 * at application startup.
 * <p>
 * Members and functions not documented; largely everything means
 * what its name suggests. If new menu items are desired, one should
 * create a new local variable referencing the new menu items,
 * and add lines to the Constructor which create the new menus.
 */
public class JDasherMenuBar extends JMenuBar implements ActionListener, ItemListener, java.awt.datatransfer.FlavorListener {
	
	private JMenu file, edit, options, control, prediction, help;
	
	private JMenuItem file_new, file_exit;
	
	private JMenuItem edit_cut, edit_copy, edit_paste;
	
	private JMenu control_speed; 
	private JRadioButtonMenuItem control_speed_slow, control_speed_medium, control_speed_fast, control_speed_fastest;
	private JCheckBoxMenuItem control_speed_auto;
	private JMenu control_style;
	private JRadioButtonMenuItem control_style_normal, control_style_click;
	private JCheckBoxMenuItem control_mousestart, control_spacestart;
		
	private JMenuItem options_editfont;
	private JMenu options_fontsize;
	private JRadioButtonMenuItem options_fontsize_small, options_fontsize_medium, options_fontsize_large;
	private JCheckBoxMenuItem options_mouseline;
	private JMenu options_alphabet, options_colours;
	
	private JMenu prediction_langmodel;
	private JRadioButtonMenuItem prediction_langmodel_ppm, prediction_langmodel_net; 
	private JCheckBoxMenuItem prediction_langmodel_learn;
	
	private JMenuItem help_about;
	
	private ButtonGroup options_colours_group, options_alphabet_group;
	
	private JDasherMenuBarListener m_Host;
	
	/**
	 * Creates a JDasherMenuBar which signals a given listener
	 * when the user selects menu items.
	 * 
	 * @param listener Listener whose methods are to be
	 * invoked upon user commands
	 */
	public JDasherMenuBar(JDasherMenuBarListener listener) {
		
		m_Host = listener;
			
		file = new JMenu("File"); this.add(file);
		edit = new JMenu("Edit"); this.add(edit);
		options = new JMenu("Options"); this.add(options);
		control = new JMenu("Control"); this.add(control);
		prediction = new JMenu("Prediction"); this.add(prediction);
		help = new JMenu("Help"); this.add(help);
		
		file_new = new JMenuItem("New"); file.add(file_new); file_new.addActionListener(this);
		file_exit = new JMenuItem("Exit"); file.add(file_exit); file_exit.addActionListener(this);
		
		edit_cut = new JMenuItem("Cut"); edit.add(edit_cut); edit_cut.addActionListener(this);
		edit_copy = new JMenuItem("Copy"); edit.add(edit_copy); edit_copy.addActionListener(this);
		edit_paste = new JMenuItem("Paste"); edit.add(edit_paste); edit_paste.addActionListener(this);
		
		edit_paste.setEnabled(false);
				
		options_editfont = new JMenuItem("Select Font..."); options.add(options_editfont); options_editfont.addActionListener(this);
		
		options_fontsize = new JMenu("Dasher Font Size"); options.add(options_fontsize);
			
		ButtonGroup options_fontsize_group = new ButtonGroup();
		options_fontsize_large = new JRadioButtonMenuItem("Large"); options_fontsize_group.add(options_fontsize_large); options_fontsize.add(options_fontsize_large); options_fontsize_large.addActionListener(this);
		options_fontsize_medium = new JRadioButtonMenuItem("Medium"); options_fontsize_group.add(options_fontsize_medium); options_fontsize.add(options_fontsize_medium); options_fontsize_medium.addActionListener(this);
		options_fontsize_small = new JRadioButtonMenuItem("Small"); options_fontsize_group.add(options_fontsize_small); options_fontsize.add(options_fontsize_small); options_fontsize_small.addActionListener(this);
		
		options_mouseline = new JCheckBoxMenuItem("Display Mouse Line"); options.add(options_mouseline); options_mouseline.addItemListener(this);
		
		options_alphabet = new JMenu("Alphabet"); options.add(options_alphabet); 

		options_alphabet_group = new ButtonGroup();
				
		options_colours = new JMenu("Colour Scheme"); options.add(options_colours);
		
		options_colours_group = new ButtonGroup();
		
		control_style = new JMenu("Control Style"); control.add(control_style);
		ButtonGroup control_style_group = new ButtonGroup();
		control_style_normal = new JRadioButtonMenuItem("Normal Control (hover)"); control_style_group.add(control_style_normal); control_style.add(control_style_normal); control_style_normal.addActionListener(this);
		control_style_click = new JRadioButtonMenuItem("Click Mode"); control_style_group.add(control_style_click); control_style.add(control_style_click); control_style_click.addActionListener(this);
		
		control_mousestart = new JCheckBoxMenuItem("Start on Mouse"); control.add(control_mousestart); control_mousestart.addItemListener(this);
						
		control_spacestart = new JCheckBoxMenuItem("Start on Space"); control.add(control_spacestart); control_spacestart.addItemListener(this);
				
		control_speed = new JMenu("Dasher Speed"); control.add(control_speed);
		ButtonGroup control_speed_group = new ButtonGroup();
		control_speed_slow = new JRadioButtonMenuItem("Slow"); control_speed_group.add(control_speed_slow); control_speed.add(control_speed_slow); control_speed_slow.addActionListener(this);
		control_speed_medium = new JRadioButtonMenuItem("Normal"); control_speed_group.add(control_speed_medium); control_speed.add(control_speed_medium); control_speed_medium.addActionListener(this);
		control_speed_fast = new JRadioButtonMenuItem("Fast"); control_speed_group.add(control_speed_fast); control_speed.add(control_speed_fast); control_speed_fast.addActionListener(this);
		control_speed_fastest = new JRadioButtonMenuItem("Fastest"); control_speed_group.add(control_speed_fastest); control_speed.add(control_speed_fastest); control_speed_fastest.addActionListener(this);
		control_speed.addSeparator();
		control_speed_auto = new JCheckBoxMenuItem("Auto-adjust"); control_speed.add(control_speed_auto); control_speed_auto.addItemListener(this);
		
		prediction_langmodel = new JMenu("Language Model"); prediction.add(prediction_langmodel);
		ButtonGroup prediction_langmodel_group = new ButtonGroup();
		prediction_langmodel_ppm = new JRadioButtonMenuItem("Prediction by Partial Match"); prediction_langmodel.add(prediction_langmodel_ppm); prediction_langmodel_group.add(prediction_langmodel_ppm); prediction_langmodel_ppm.addActionListener(this);
		prediction_langmodel_net = new JRadioButtonMenuItem("Networked PPM"); prediction_langmodel.add(prediction_langmodel_net); prediction_langmodel_group.add(prediction_langmodel_net); prediction_langmodel_net.addActionListener(this);
		prediction_langmodel.addSeparator();
		prediction_langmodel_learn = new JCheckBoxMenuItem("Language Model Learns"); prediction_langmodel.add(prediction_langmodel_learn); prediction_langmodel_learn.addItemListener(this);
	
		help_about = new JMenuItem("About..."); help.add(help_about); help_about.addActionListener(this);

	}
	
	public void actionPerformed(ActionEvent e) {
		// Handles all ordinary and radio-button menus.
		
		if(e.getActionCommand().equals("New")) {
			m_Host.menuNew();
		}
		else if(e.getActionCommand().equals("Exit")) {
			m_Host.menuExit();
		}
		else if(e.getActionCommand().equals("Cut")) {
			m_Host.menuCut();
		}
		else if(e.getActionCommand().equals("Copy")) {
			m_Host.menuCopy();
		}
		else if(e.getActionCommand().equals("Paste")) {
			m_Host.menuPaste();
		}
		else if(e.getActionCommand().equals("Select Font...")) {
			m_Host.menuSelFont();
		}
		else if(e.getActionCommand().equals("Large")) {
			m_Host.menuSetFontSize(4);
		}
		else if(e.getActionCommand().equals("Medium")) {
			m_Host.menuSetFontSize(2);
		}
		else if(e.getActionCommand().equals("Small")) {
			m_Host.menuSetFontSize(1);
		}
		else if(e.getActionCommand().equals("Normal Control (hover)")) {
			m_Host.menuSetInputFilter("Normal Control");
		}
		else if(e.getActionCommand().equals("Click Mode")) {
			m_Host.menuSetInputFilter("Click Mode");
		}
		else if(e.getActionCommand().equals("Slow")) {
			m_Host.menuSetDasherSpeed(100);
			setSpeedAbs();
		}
		else if(e.getActionCommand().equals("Normal")) {
			m_Host.menuSetDasherSpeed(200);
			setSpeedAbs();
		}
		else if(e.getActionCommand().equals("Fast")) {
			m_Host.menuSetDasherSpeed(400);
			setSpeedAbs();
		}
		else if(e.getActionCommand().equals("Fastest")) {
			m_Host.menuSetDasherSpeed(800);
			setSpeedAbs();
		}
		else if(e.getActionCommand().equals("Prediction by Partial Match")) {
			m_Host.menuSetLMID(0);
		}
		else if(e.getActionCommand().equals("Networked PPM")) {
			m_Host.menuSetLMID(5);					
		}
		else if(e.getActionCommand().equals("About...")) {
			m_Host.menuHelpAbout();			
		}
	}
	
	public void itemStateChanged(ItemEvent e) {
		JCheckBoxMenuItem tickbox = ((JCheckBoxMenuItem)e.getItem());
		
		if(tickbox.getText().equals("Display Mouse Line")) {
			m_Host.menuSetMouseLine(tickbox.isSelected());
		}
		else if(tickbox.getText().equals("Start on Mouse")) {
			m_Host.menuSetStartMouse(tickbox.isSelected());
		}
		else if(tickbox.getText().equals("Start on Space")) {
			m_Host.menuSetStartSpace(tickbox.isSelected());
		}
		else if(tickbox.getText().equals("Auto-adjust")) {
			m_Host.menuSetSpeedAuto(tickbox.isSelected());
			setSpeedAuto(tickbox.isSelected());
		}
		else if(tickbox.getText().equals("Language Model Learns")) {
			m_Host.menuSetLMLearn(tickbox.isSelected());
		}
	}
	
	public void setSelectedFontSize(int size) {
		switch(size) {
		case 1:
			options_fontsize_small.setSelected(true);
			break;
		case 2:
			options_fontsize_medium.setSelected(true);
			break;
		case 4:
			options_fontsize_large.setSelected(true);
			break;
		}
	}
	
	public void setAlphabets(ArrayList<String> alphs, String current) {
		
		ActionListener alphHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_Host.menuSetAlph(e.getActionCommand());
			}
		};
		
		for(String alph : alphs) {
			String AlphName = alph;
			JRadioButtonMenuItem newAlph = new JRadioButtonMenuItem(AlphName);
			newAlph.addActionListener(alphHandler);
			options_alphabet.add(newAlph);
			options_alphabet_group.add(newAlph);
			if(current.equals(AlphName)) {
				newAlph.setSelected(true);
			}
			
		}
		
	}
	
	public void setColours(ArrayList<String> colours, String current) {
		
		ActionListener colourHandler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_Host.menuSetColours(e.getActionCommand());
			}
		};
		
		
		for(String ColourName : colours) {
			JMenuItem newColour = new JRadioButtonMenuItem(ColourName);
			newColour.addActionListener(colourHandler);
			options_colours.add(newColour);
			options_colours_group.add(newColour);
			if(ColourName.equals(current)) {
				newColour.setSelected(true);
			}
		}
	}
	
	public void setInputFilter(String filter) {
		if(filter.equals("Normal Control")) control_style_normal.setSelected(true);
		if(filter.equals("Click Mode")) control_style_click.setSelected(true);
	}
	
	
	
	public void setMouseLine(boolean enabled) {
		options_mouseline.setSelected(enabled);
	}
	
	public void setStartMouse(boolean enabled) {
		control_mousestart.setSelected(enabled);
	}
	
	public void setStartSpace(boolean enabled) {
		control_spacestart.setSelected(enabled);
	}

	public void setSelectedLM(int LMID) {
		if(LMID == 0) {
			prediction_langmodel_ppm.setSelected(true);
		}
		else if(LMID == 5) {
			prediction_langmodel_net.setSelected(true);
		}
	}
	
	public void setPasteEnabled(boolean enabled) {
		edit_paste.setEnabled(enabled);
	}
	
	public void setSpeedAbs() {
		control_speed_auto.setSelected(false);
	}
	
	public void setSpeedAuto(boolean enabled) {
		control_speed_slow.setSelected(false);
		control_speed_medium.setSelected(false);
		control_speed_fast.setSelected(false);
		control_speed_fastest.setSelected(false);
		control_speed_auto.setSelected(enabled);
	}
	
	public void setLangModelLearns(boolean enabled) {
		prediction_langmodel_learn.setSelected(enabled);
	}
	
	public void setSelectedColour(String colour) {
		
		JMenuItem item;
		for(int i = 0; i < options_colours.getItemCount(); i++) {
			item = options_colours.getItem(i);
			if(item.getText().equals(colour)) {
				item.setSelected(true);
			}
		}
	}

	public void flavorsChanged(FlavorEvent arg0) {
		
		if(m_Host.isDataFlavorAvailable(java.awt.datatransfer.DataFlavor.stringFlavor)) {
			setPasteEnabled(true);
		}
		else {
			setPasteEnabled(false);
		}
		
	}
	
	
	
}
