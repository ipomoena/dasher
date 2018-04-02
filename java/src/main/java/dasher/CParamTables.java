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
 * Enumeration of possible Dasher settings, including their internal
 * names, friendly names, default values, and a flag indicating whether
 * they are persistent and should be stored across sessions.
 * <p>
 * The set of available parameters is currently hard-coded into
 * this class.
 * <p>
 * If a new parameter must be created, an external identifier should
 * be added to Ebp_parameters, Elp_parameters or Esp_parameters for
 * a boolean, integer or string parameter respectively, and then an
 * entry added to the appropriate table in this class.
 */
public class CParamTables {
	
	/**
	 * Value indicating a persistent setting
	 */
	public final boolean PERS = true;
	
	/**
	 * Table of all available boolean parameters.
	 */	
	public bp_table[] BoolParamTable = {
			  new bp_table(Ebp_parameters.BP_DRAW_MOUSE_LINE, "DrawMouseLine", PERS, true, "Draw Mouse Line"),
			  new bp_table(Ebp_parameters.BP_DRAW_MOUSE, "DrawMouse", PERS, true, "Draw Mouse Position"),
			  new bp_table(Ebp_parameters.BP_SHOW_SLIDER, "ShowSpeedSlider", PERS, true, "ShowSpeedSlider"),
			  new bp_table(Ebp_parameters.BP_START_MOUSE, "StartOnLeft", PERS, true, "StartOnLeft"),
			  new bp_table(Ebp_parameters.BP_START_SPACE, "StartOnSpace", PERS, false, "StartOnSpace"),
			  new bp_table(Ebp_parameters.BP_START_STYLUS, "StartOnStylus", PERS, false, "StartOnStylus"),
			  new bp_table(Ebp_parameters.BP_STOP_IDLE, "StopOnIdle", PERS, false, "StopOnIdle"),
			  new bp_table(Ebp_parameters.BP_KEY_CONTROL, "KeyControl", PERS, false, "KeyControl"),
			  new bp_table(Ebp_parameters.BP_CONTROL_MODE, "ControlMode", PERS, false, "ControlMode"),
			  new bp_table(Ebp_parameters.BP_COLOUR_MODE, "ColourMode", PERS, true, "ColourMode"),
			  new bp_table(Ebp_parameters.BP_MOUSEPOS_MODE, "StartOnMousePosition", PERS, false, "StartOnMousePosition"),
			  new bp_table(Ebp_parameters.BP_OUTLINE_MODE, "OutlineBoxes", PERS, true, "OutlineBoxes"),
			  new bp_table(Ebp_parameters.BP_PALETTE_CHANGE, "PaletteChange", PERS, true, "PaletteChange"),
			  new bp_table(Ebp_parameters.BP_NUMBER_DIMENSIONS, "NumberDimensions", PERS, false, "NumberDimensions"),
			  new bp_table(Ebp_parameters.BP_EYETRACKER_MODE, "EyetrackerMode", PERS, false, "EyetrackerMode"),
			  new bp_table(Ebp_parameters.BP_AUTOCALIBRATE, "Autocalibrate", PERS, true, "Autocalibrate"),
			  new bp_table(Ebp_parameters.BP_DASHER_PAUSED, "DasherPaused", !PERS, true, "Dasher Paused"),
			  new bp_table(Ebp_parameters.BP_GAME_MODE, "GameMode", PERS, false, "Dasher Game Mode"),
			  new bp_table(Ebp_parameters.BP_TRAINING, "Training", !PERS, false, "Provides locking during training"),
			  new bp_table(Ebp_parameters.BP_REDRAW, "Redraw", !PERS, false, "Force a full redraw at the next timer event"),
			  new bp_table(Ebp_parameters.BP_LM_DICTIONARY, "Dictionary", PERS, true, "Whether the word-based language model uses a dictionary"),
			  new bp_table(Ebp_parameters.BP_LM_LETTER_EXCLUSION, "LetterExclusion", PERS, true, "Whether to do letter exclusion in the word-based model"),
			  new bp_table(Ebp_parameters.BP_AUTO_SPEEDCONTROL, "AutoSpeedControl", PERS, true, "AutoSpeedControl"),
			  new bp_table(Ebp_parameters.BP_CLICK_MODE, "ClickMode", PERS, false, "Dasher Click Mode"),
			  new bp_table(Ebp_parameters.BP_LM_ADAPTIVE, "LMAdaptive", PERS, true, "Whether language model should learn as you enter text"),
			  new bp_table(Ebp_parameters.BP_BUTTONONESTATIC, "ButtonOneStaticMode", PERS, false, "One-button static mode"),
			  new bp_table(Ebp_parameters.BP_BUTTONONEDYNAMIC, "ButtonOneDynamicMode", PERS, false, "One-button dynamic mode"),
			  new bp_table(Ebp_parameters.BP_BUTTONMENU, "ButtonMenuMode", PERS, false, "Button menu mode"),
			  new bp_table(Ebp_parameters.BP_BUTTONPULSING, "ButtonPulsingMode", PERS, false, "One-button dynamic pulsing mode"),
			  new bp_table(Ebp_parameters.BP_BUTTONSTEADY, "ButtonSteadyMode", PERS, true, "One-button dynamic steady mode"),
			  new bp_table(Ebp_parameters.BP_BUTTONDIRECT, "ButtonDirectMode", PERS, false, "Three-button direct mode"),
			  new bp_table(Ebp_parameters.BP_BUTTONFOURDIRECT, "ButtonFourDirectMode", PERS, false, "Four-button direct mode"),
			  new bp_table(Ebp_parameters.BP_BUTTONALTERNATINGDIRECT, "ButtonAlternatingDirectMode", PERS, true, "Alternating direct mode"),
			  new bp_table(Ebp_parameters.BP_COMPASSMODE, "ButtonCompassMode", PERS, false, "Compass mode"),
			  new bp_table(Ebp_parameters.BP_SOCKET_INPUT_ENABLE, "SocketInputEnable", PERS, false, "Read pointer coordinates from network socket instead of mouse"),
			  new bp_table(Ebp_parameters.BP_SOCKET_DEBUG, "SocketInputDebug", PERS, false, "Print information about socket input processing to console"),
			  new bp_table(Ebp_parameters.BP_OLD_STYLE_PUSH, "OldStylePush", PERS, false, "Old style node pushing algorithm"),
			  new bp_table(Ebp_parameters.BP_CIRCLE_START, "CircleStart", PERS, false, "Start on circle mode"),
			  new bp_table(Ebp_parameters.BP_GLOBAL_KEYBOARD, "GlobalKeyboard", PERS, false, "Whether to assume global control of the keyboard"),
			  new bp_table(Ebp_parameters.BP_DELAY_VIEW, "DelayView", !PERS, false, "Delayed dynamics (for two button mode)"),
			  new bp_table(Ebp_parameters.BP_LM_REMOTE, "RemoteLM", PERS, false, "Language model is remote and responds asynchronously."),
			  new bp_table(Ebp_parameters.BP_CONNECT_LOCK, "Connecting", !PERS, false, "Currently waiting for a connection to a remote LM; lock Dasher.")};
	
	/**
	 * Table of all available integer parameters
	 */
	public lp_table[] LongParamTable = {
			  new lp_table(Elp_parameters.LP_ORIENTATION, "ScreenOrientation", PERS, -2, "Screen Orientation"),
			  new lp_table(Elp_parameters.LP_REAL_ORIENTATION, "RealOrientation", !PERS, 0, "Actual screen orientation (allowing for alphabet default)"),
			  new lp_table(Elp_parameters.LP_MAX_BITRATE, "MaxBitRateTimes100", PERS, 80, "Max Bit Rate Times 100"),
			  new lp_table(Elp_parameters.LP_VIEW_ID, "ViewID", PERS, 1, "ViewID"),
			  new lp_table(Elp_parameters.LP_LANGUAGE_MODEL_ID, "LanguageModelID", PERS, 0, "LanguageModelID"),
			  new lp_table(Elp_parameters.LP_DASHER_FONTSIZE, "DasherFontSize", PERS, 1, "DasherFontSize"),
			  new lp_table(Elp_parameters.LP_UNIFORM, "UniformTimes1000", PERS, 50, "UniformTimes1000"),
			  new lp_table(Elp_parameters.LP_YSCALE, "YScaling", PERS, 0, "YScaling"),
			  new lp_table(Elp_parameters.LP_MOUSEPOSDIST, "MousePositionBoxDistance", PERS, 50, "MousePositionBoxDistance"),
			  new lp_table(Elp_parameters.LP_STOP_IDLETIME, "StopIdleTime", PERS, 1000, "StopIdleTime" ),
			  new lp_table(Elp_parameters.LP_TRUNCATION, "Truncation", PERS, 0, "Truncation"),
			  new lp_table(Elp_parameters.LP_TRUNCATIONTYPE, "TruncationType", PERS, 0, "TruncationType"),
			  new lp_table(Elp_parameters.LP_LM_MAX_ORDER, "LMMaxOrder", PERS, 5, "LMMaxOrder"),
			  new lp_table(Elp_parameters.LP_LM_EXCLUSION, "LMExclusion", PERS, 0, "LMExclusion"),
			  new lp_table(Elp_parameters.LP_LM_UPDATE_EXCLUSION, "LMUpdateExclusion", PERS, 1, "LMUpdateExclusion"),
			  new lp_table(Elp_parameters.LP_LM_ALPHA, "LMAlpha", PERS, 49, "LMAlpha"),
			  new lp_table(Elp_parameters.LP_LM_BETA, "LMBeta", PERS, 77, "LMBeta"),
			  new lp_table(Elp_parameters.LP_LM_MIXTURE, "LMMixture", PERS, 50, "LMMixture"),
			  new lp_table(Elp_parameters.LP_MOUSE_POS_BOX, "MousePosBox", !PERS, -1, "Mouse Position Box Indicator"),
			  new lp_table(Elp_parameters.LP_NORMALIZATION, "Normalization", !PERS, 1 << 16, "Interval for child nodes"),
			  new lp_table(Elp_parameters.LP_LINE_WIDTH, "LineWidth", PERS, 1, "Width to draw crosshair and mouse line"),
			  new lp_table(Elp_parameters.LP_LM_WORD_ALPHA, "WordAlpha", PERS, 50, "Alpha value for word-based model"),
			  new lp_table(Elp_parameters.LP_USER_LOG_LEVEL_MASK, "UserLogLevelMask", PERS, 0, "Controls level of user logging, 0 = none, 1 = short, 2 = detailed, 3 = both"),
			  new lp_table(Elp_parameters.LP_SPEED_DIVISOR, "SpeedDivisor", !PERS, 100, "Factor by which to slow down (multiplied by 100)"),
			  new lp_table(Elp_parameters.LP_ZOOMSTEPS, "Zoomsteps", PERS, 32, "Integerised ratio of zoom size for click/button mode, denom 64."),
			  new lp_table(Elp_parameters.LP_B, "ButtonMenuBoxes", PERS, 4, "Number of boxes for button menu mode"),
			  new lp_table(Elp_parameters.LP_S, "ButtonMenuSafety", PERS, 25, "Safety parameter for button mode, in percent."),
			  new lp_table(Elp_parameters.LP_Z, "ButtonMenuBackwardsBox", PERS, 1, "Number of back-up boxes for button menu mode"),
			  new lp_table(Elp_parameters.LP_R, "ButtonModeNonuniformity", PERS, 0, "Button mode box non-uniformity"),
			  new lp_table(Elp_parameters.LP_RIGHTZOOM, "ButtonCompassModeRightZoom", PERS, 5120, "Zoomfactor (*1024) for compass mode"),
			  new lp_table(Elp_parameters.LP_BOOSTFACTOR, "BoostFactor", !PERS, 100, "Boost/brake factor (multiplied by 100)"),
			  new lp_table(Elp_parameters.LP_AUTOSPEED_SENSITIVITY, "AutospeedSensitivity", PERS, 100, "Sensitivity of automatic speed control (percent)"),
			  new lp_table(Elp_parameters.LP_SOCKET_PORT, "SocketPort", PERS, 20320, "UDP/TCP socket to use for network socket input"),
			  new lp_table(Elp_parameters.LP_SOCKET_INPUT_X_MIN, "SocketInputXMinTimes1000", PERS, 0, "Bottom of range of X values expected from network input"),
			  new lp_table(Elp_parameters.LP_SOCKET_INPUT_X_MAX, "SocketInputXMaxTimes1000", PERS, 1000, "Top of range of X values expected from network input"),
			  new lp_table(Elp_parameters.LP_SOCKET_INPUT_Y_MIN, "SocketInputYMinTimes1000", PERS, 0, "Bottom of range of Y values expected from network input"),
			  new lp_table(Elp_parameters.LP_SOCKET_INPUT_Y_MAX, "SocketInputYMaxTimes1000", PERS, 1000, "Top of range of Y values expected from network input"),
			  new lp_table(Elp_parameters.LP_OX, "OX", PERS, 2048, "X coordinate of crosshair"),
			  new lp_table(Elp_parameters.LP_OY, "OY", PERS, 2048, "Y coordinate of crosshair"),
			  new lp_table(Elp_parameters.LP_MAX_Y, "MaxY", PERS, 4096, "Maximum Y coordinate"),
			  new lp_table(Elp_parameters.LP_INPUT_FILTER, "InputFilterID", PERS, 3, "Module ID of input filter"),
			  new lp_table(Elp_parameters.LP_CIRCLE_PERCENT, "CirclePercent", PERS, 10, "Percentage of nominal vertical range to use for radius of start circle"),
			  new lp_table(Elp_parameters.LP_TWO_BUTTON_OFFSET, "TwoButtonOffset", PERS, 1024, "Offset for two button dynamic mode")	};
	
	/**
	 * Table of all available string parameters
	 */
	public sp_table[] StringParamTable = {
			  new sp_table(Esp_parameters.SP_ALPHABET_ID, "AlphabetID", PERS, "", "AlphabetID"),
			  new sp_table(Esp_parameters.SP_ALPHABET_1, "Alphabet1", PERS, "", "Alphabet History 1"),
			  new sp_table(Esp_parameters.SP_ALPHABET_2, "Alphabet2", PERS, "", "Alphabet History 2"),
			  new sp_table(Esp_parameters.SP_ALPHABET_3, "Alphabet3", PERS, "", "Alphabet History 3"),
			  new sp_table(Esp_parameters.SP_ALPHABET_4, "Alphabet4", PERS, "", "Alphabet History 4"),
			  new sp_table(Esp_parameters.SP_COLOUR_ID, "ColourID", PERS, "", "ColourID"), 
			  new sp_table(Esp_parameters.SP_DEFAULT_COLOUR_ID, "DefaultColourID", !PERS, "", "Default Colour ID (Used for auto-colour mode)"),
			  new sp_table(Esp_parameters.SP_DASHER_FONT, "DasherFont", PERS, "", "DasherFont"),
			  new sp_table(Esp_parameters.SP_SYSTEM_LOC, "SystemLocation", !PERS, "sys_", "System Directory"),
			  new sp_table(Esp_parameters.SP_USER_LOC, "UserLocation", !PERS, "usr_", "User Directory"),
			  new sp_table(Esp_parameters.SP_GAME_TEXT_FILE, "GameTextFile", !PERS, "gamemode_english_GB.txt", "File with strings to practice writing"),
			  new sp_table(Esp_parameters.SP_TRAIN_FILE, "TrainingFile", !PERS, "", "Training text for alphabet"),
			  new sp_table(Esp_parameters.SP_SOCKET_INPUT_X_LABEL, "SocketInputXLabel", PERS, "x", "Label preceding X values for network input"),
			  new sp_table(Esp_parameters.SP_SOCKET_INPUT_Y_LABEL, "SocketInputYLabel", PERS, "y", "Label preceding Y values for network input"),
			  new sp_table(Esp_parameters.SP_INPUT_FILTER, "InputFilter", PERS, "Normal Control", "Input filter used to provide the current control mode"),
			  new sp_table(Esp_parameters.SP_INPUT_DEVICE, "InputDevice", PERS, "Mouse Input", "Driver for the input device"),
			  new sp_table(Esp_parameters.SP_LM_HOST, "LMHost", PERS, "", "Language Model Host")};
}

/**
 * Struct detailing a boolean parameter 
 */
class bp_table {
	
	/**
	 * Creates a new Boolean parameter
	 * 
	 * @param k Parameter (member of Ebp_parameters)
	 * @param rName Internal name
	 * @param pers Persistent
	 * @param def Default value
	 * @param hr Human-readable name
	 */
	public bp_table(Ebp_parameters k, String rName, boolean pers, boolean def, String hr) {
		key = k.ordinal();
		humanReadable = hr;
		persistent = pers;
		defaultVal = def;
		value = def;
		regName = rName;
	}
	
	int key;
	String regName;
	boolean persistent;
	boolean defaultVal;
	boolean value;
	String humanReadable;
}

/**
 * Struct defining an integer parameter
 */
class lp_table {
	
	static private final int lp_offset = Ebp_parameters.values().length;
	
	/**
	 * Creates a new integer parameter
	 * 
	 * @param k Parameter (member of Elp_parameters)
	 * @param rName Internal name
	 * @param pers Persistent
	 * @param def Default value
	 * @param hr Human-readable name
	 */
	public lp_table(Elp_parameters k, String rName, boolean pers, long def, String hr) {
		key = k.ordinal() + lp_offset;
		humanReadable = hr;
		persistent = pers;
		defaultVal = def;
		value = def;
		regName = rName;
	}
	
	int key;
	String regName;
	boolean persistent;
	long defaultVal;
	long value;
	String humanReadable;
}

/**
 * Creates a new String parameter
 * 
 * @param k Parameter (member of Esp_parameters)
 * @param rName Internal name
 * @param pers Persistent
 * @param def Default value
 * @param hr Human-readable name
 */
class sp_table {
	
	static private final int sp_offset = Ebp_parameters.values().length + Elp_parameters.values().length;
	
	public sp_table(Esp_parameters k, String rName, boolean pers, String def, String hr) {
		key = k.ordinal() + sp_offset;
		humanReadable = hr;
		persistent = pers;
		defaultVal = def;
		value = def;
		regName = rName;
	}
	
	int key;
	String regName;
	boolean persistent;
	String value;
	String defaultVal;
	String humanReadable;
}
