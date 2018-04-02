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
 * Enumeration of possible integer parameter references. See
 * CParamTables for a list and definitions.
 */
public enum Elp_parameters implements EParameters {
		  LP_ORIENTATION, LP_REAL_ORIENTATION, LP_MAX_BITRATE,
		  LP_VIEW_ID, LP_LANGUAGE_MODEL_ID, LP_DASHER_FONTSIZE,
		  LP_UNIFORM, LP_YSCALE, LP_MOUSEPOSDIST, LP_STOP_IDLETIME, LP_TRUNCATION, 
		  LP_TRUNCATIONTYPE, LP_LM_MAX_ORDER, LP_LM_EXCLUSION,
		  LP_LM_UPDATE_EXCLUSION, LP_LM_ALPHA, LP_LM_BETA,
		  LP_LM_MIXTURE, LP_MOUSE_POS_BOX, LP_NORMALIZATION, LP_LINE_WIDTH, 
		  LP_LM_WORD_ALPHA, LP_USER_LOG_LEVEL_MASK, LP_SPEED_DIVISOR, 
		  LP_ZOOMSTEPS, LP_B, LP_S, LP_Z, LP_R, LP_RIGHTZOOM,
		  LP_BOOSTFACTOR, LP_AUTOSPEED_SENSITIVITY, LP_SOCKET_PORT, LP_SOCKET_INPUT_X_MIN, LP_SOCKET_INPUT_X_MAX,
		  LP_SOCKET_INPUT_Y_MIN, LP_SOCKET_INPUT_Y_MAX, LP_OX, LP_OY, LP_MAX_Y, LP_INPUT_FILTER, 
		  LP_CIRCLE_PERCENT, LP_TWO_BUTTON_OFFSET;
		  
}
