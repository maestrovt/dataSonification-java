package com.dataSonification.v2;

import java.awt.Color;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.Subsystem;

/**
 * Class storing constants for the application and applet.
 * @author Kimo Johnson
 */
public class Const {
	// Suppress default constructor for noninstantiability
	private Const() {}
	
	public static final String VERSION = "2.2.0";
	
	/**
	 * Core constants
	 */
	public static final String CORE_CONFIG_NAME = "settings/core.txt";
    public static final String SOUNDBANK_FILE = "soundbank-deluxe.gm";
	public static final String DS_PATH = "settings/dataSources/";
	public static final String DE_PATH = "settings/dataEntities/";
	public static final String CONFIG_EXTENSION = ".txt";

	/**
	 * Log level
	 */
	public static Log.LogCode LOG_LEVEL = Log.P_VERBOSE;
    public static Subsystem[] LOG_SUPPRESS_SYSTEMS = null; //{Subsystem.DATA};
       
     /*
      * DB Dead interval.
      *
      * Core.runCore() calls exitID() for any db_id whose dbg.last is older
      * than this. With the Stage 6 touchActivity fix, dbg.last is now
      * refreshed on every data message — so this acts as the maximum
      * tolerated gap between data messages before the server assumes the
      * client is gone.
      *
      * Bumped from 30 min to 4 hours: in real Excel usage, Stock Connector
      * freezes (sometimes 20+ min), lunch-hour trading lulls, and the user
      * stepping away can all produce 30-60 min data gaps that don't mean
      * the client died. 4 hours covers normal idle within a trading day
      * while still reaping a truly abandoned client before the next session.
      */
     public static long DB_DEAD_INTERVAL = 1000L*60*60*4;
     
     
     /*
	 * General constants
	 */
	public static final int SUCCESS = 1;
	public static final int FAILURE = 0;
	public static final int STOPPED = -1;

	
	/**
	 * Gatekeeper constants
	 */
	public final static int GATEKEEPER_PAUSE_TIME = 200;
	public final static int GATEKEEPER_MAX_SONIFIABLES = 22;
	
	/**
	 * Sonification constants
	 */
	public static final String INSTRUMENT_PATH = "settings/instruments/";
	
	public static final double SIG_MOVE_TOL = 1e-6;
	
	/**
	 * GUI constants
	 */
	public static final int MENU_FILE_QUIT = 1;
	public static final int MENU_EDIT_UNDO = 2;
	public static final int MENU_EDIT_REDO = 3;
	public static final int MENU_WINDOW_HIDE = 9;
	public static final int MENU_WINDOW_SHOW = 10;
	
	public static final int COMBO_CHANGED = 4;
	
	public static final int BUTTON_PLAY = 5;
	public static final int BUTTON_STOP = 6;
	public static final int BUTTON_MUTE = 7;
	
	public static final int LIST_CHANGED = 8;
	
	public static final String appName = "AppName";
	
	public static final boolean EDITABLE_TABLE = true;
	
	/**
	 * DataAction constants
	 * indices into the colors array
	 */
	public static final int EXTERNAL_STOP_RECEIVED = 20;
    public static final int DATA_UNRECOGNIZED = 4;
	public static final int DATA_UP_BASE = 3;
	public static final int DATA_EVEN_BASE = 2;
	public static final int DATA_DOWN_BASE = 1;
	public static final int DATA_IGNORED = 0;

	
	/**
	 * Applet UI constants
	 */
	public static final Color[] colors = {Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN, Color.BLACK};
	public static final long TICKER_FLASH_DELAY = 5000;
    
	
	/**
	 * ControlUI constants
	 */
	public static final int COL_STRING = 0;
	public static final int COL_CHECKBOX = 1;
	public static final int COL_SLIDER = 2;
	public static final int COL_BUTTON = 3;
	public static final int COL_MIDI = 4;
	public static final int COL_PAN = 5;
	public static final int COL_LABEL = 6;
	public static final int COL_MIDILABEL = 7;
	
	public static final int TABLE_NAME_SIZE = 32;
	
	public static boolean is_editable(int col) {
//		if (col == 0)
//			return true;
//		return false;
		return true;
	}

    /** Alarm constants **/
    public static final int MAX_INCREMENT = 12;
    
    public static final int MAX_ALARM_NOTES = 3;
    
    /**
     * Four Note Arranger Constants
     * Duration in Quarter Notes
     * Tempo indicated in Quarter Note = MM
     */
    public static final int MAX_DURATION = 2;
    /**
     * The duration multiplier used at the point that the crossover
     * of a technical target or moving average occurs.
     */        
    public static final int CROSSOVER_MULT = 2;
    /**
     * The ratio of the duration to the hold for the for the four note
     * arranget sonification scheme.
     */        
    public static final int FOUR_NOTE_HOLD_RATIO = 2;
    
    public static final double MAX_DB = 100.0;
    
    /**
     * Used in VoiceInstrument to separate ticker voice samples
     * from event voice samples.
     */
    public static final int MIN_EVENT_OFFSET = 50;
    
    /**
     * Used as a fake core id in VoiceInstrument
     */
    public static final int VOICE_EVENT_CORE_ID = -2;
    
    /**
     * Interval for TwoNoteArranger
     */
    public static final int TWO_NOTE_INTERVAL = 3;
    public static final int MAX_TWO_NOTE_REPEATS = 3;
  
    /**
     * Constants for Beet5 Arranger
     */
    public static final int BEET_5_INTERVAL = 1;
    public static final int BEET_5_REPEATS = 3;
    public static final int BEET_5_MULT = 2;
    
    /**
     * Constants for TrillAnalyzer
     */
    public static final double MIN_PRICE_DIFF = 0.0001;
    
	/**
	 * Constant for SizeArranger
	 */
	public static final int TRILL_INTERVAL = 1;
	
    /**
     * Compiler constants
     */
    public static final String REVISION = "@REVISION@";
    public static final String COMPILE_TIME = "@COMPILE_TIME@";
    public static final String COMPILE_USER = "@COMPILE_USER@";
    
    public static String getVersionString() {
        String version = "dataSonification Auditory Display, version " + VERSION;
        if (("@REV" + "ISION@").equals(REVISION)) {
            version += ", unknown revision.";
        } else {
            version += ", revision " + REVISION + ".";
        }
        
        // If time and user are set, add this info to the version string
        if (!("@COMPILE" + "_TIME@").equals(COMPILE_TIME) && 
                !("@COMPILE" + "_USER@").equals(COMPILE_USER)) {
            version += "\nCompiled by " + COMPILE_USER + ", " + COMPILE_TIME + ".";
        }
        return version;
    }
    
    public static void main(String[] args) {
        System.out.println("version = " + VERSION);
        System.out.println("revision = " + REVISION);
        System.out.println("compiled at = " + COMPILE_TIME);
        System.out.println("compiled by = " + COMPILE_USER);
    }
}
