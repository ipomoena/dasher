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

/**
 * Enumeration of possible boolean parameter references. See
 * CParamTables for a list and definitions.
 */
public enum Ebp_parameters implements EParameters {
	
		  BP_DRAW_MOUSE_LINE, BP_DRAW_MOUSE,
		  BP_SHOW_SLIDER, BP_START_MOUSE,
		  BP_START_SPACE, BP_START_STYLUS, BP_STOP_IDLE, BP_KEY_CONTROL,  BP_CONTROL_MODE, 
		  BP_COLOUR_MODE, BP_MOUSEPOS_MODE,
		  BP_OUTLINE_MODE, BP_PALETTE_CHANGE, BP_NUMBER_DIMENSIONS,
		  BP_EYETRACKER_MODE, BP_AUTOCALIBRATE, BP_DASHER_PAUSED,
		  BP_GAME_MODE, BP_TRAINING, BP_REDRAW, BP_LM_DICTIONARY, 
		  BP_LM_LETTER_EXCLUSION, BP_AUTO_SPEEDCONTROL, BP_CLICK_MODE, 
		  BP_LM_ADAPTIVE, BP_BUTTONONESTATIC, BP_BUTTONONEDYNAMIC,
		  BP_BUTTONMENU, BP_BUTTONPULSING, BP_BUTTONSTEADY, 
		  BP_BUTTONDIRECT, BP_BUTTONFOURDIRECT, BP_BUTTONALTERNATINGDIRECT,
		  BP_COMPASSMODE, BP_SOCKET_INPUT_ENABLE, BP_SOCKET_DEBUG, 
		  BP_OLD_STYLE_PUSH, BP_CIRCLE_START, BP_GLOBAL_KEYBOARD, 
		  BP_DELAY_VIEW, BP_LM_REMOTE, BP_CONNECT_LOCK
	
}
