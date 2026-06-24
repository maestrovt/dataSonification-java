package com.dataSonification.v2;

import java.util.HashMap;
import java.util.Map;

import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.ReturnCode;
import com.dataSonification.v2.util.Subsystem;

/**
 * Main entry point for the application.
 * 
 * @author Kimo Johnson
 *
 */
public class Main {
	public static void main(String[] args) {
	    //	  Print version
	    Log.println(Subsystem.CORE, null, Const.getVersionString());
	    
		/*
		 * Initialize JMSL (these should be called based on the configuration)	
		 * These calls need to be outside the core because MainApplet
		 * sets the soundbank URL and this needs to be done after JMSL.midi
		 * is set.
		 */ 
	    
		try {
//		    JMSL.midi = MidiIO_JavaSound.instance();		
//		    JMSL.midi.setEditEnabled(false);
//		    JMSL.setIsApplet(false);
			
			/*
			 * Process the command line arguments
			 */
		    Map<Key,String> settings = processArgs(args);
		    Core mainCore = Core.instance();
		    mainCore.init(settings);
			//mainCore.setUI(Integer.parseInt(settings.get(Key.CORE_ID)));
		} catch (Exception e) {
		    System.exit(1);
		}
	}
	
	static Map<Key,String> processArgs(String[] args) {
	    Map<Key,String> m = new HashMap<Key,String>();
	    for (int i = 0; i < args.length; i++) {
	        String arg = args[i].trim();
	        
	        if (arg.charAt(0) != '-' )
	            continue;
	        
	        if (i == args.length-1)
	            break;
	        
	        try {
	            Key k = Key.valueOf(arg.substring(1).toUpperCase());
	            String val = args[i+1].trim();
		        m.put(k, val);
	        } catch (IllegalArgumentException e) {
	            Log.println(Subsystem.CORE, ReturnCode.GENERAL_ERROR, "Main: No key for " + arg.substring(1), Log.P_ERROR);
	        }
	        
	    }
	    return m;
	}
}