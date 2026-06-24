package com.dataSonification.v2.ui;

import com.dataSonification.v2.Const;
import com.dataSonification.v2.Core;
import com.dataSonification.v2.ID;
import com.dataSonification.v2.data.DataInfo;
import com.dataSonification.v2.data.MessageComponent;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.Subsystem;

/**
 * @author John Stephens
 */

public class FacelessUI implements UI {

    private final Core core;
    //private VoiceInstrument voice;
    private UIHelper helper;
    
    public FacelessUI() {

        super();

        core = Core.instance();
        core.start();
        
        //voice = new VoiceInstrument("/Users/kimo/Projects/dataSonification/Samples/");
        Log.println(Subsystem.UI, null, "ExcelUI: core started");
    }

    public void displayError(String errorMessage) {
    }

    /**
     * When core's mode changes this method is called to update the UI
     * 
     * @param mode the new mode of the core
     */
    public void setMode(int mode) {
        if (Const.STOPPED == mode) {
            Log.println(Subsystem.UI, null, "FacelessUI: setMode calling exit()");
            System.exit(0);
        }
    }

    public void reset() {
    }

    public void uiJob(ID id, DataInfo info) {
        
		switch(info.type) {
		case EXTERNAL_STOP_RECEIVED:
			Log.println(Subsystem.UI, null, "FacelessUI: EXTERNAL_STOP_RECEIVED - shutting down");
            Core.instance().exit();
            System.exit(0);
			break;
		case ERROR:
			Log.println(Subsystem.UI, null, "FacelessUI: Error: " + id);
			break;
		case NO_INFO:
			Log.println(Subsystem.UI, null, "FacelessUI: No info for: " + id);
			break;
		case IGNORED:
			Log.println(Subsystem.UI, null, "FacelessUI: Ignoring: " + id);
			break;
		default:
			helper.help(id, info);	
		}
        
    }

    /*
     * Javadoc in interface
     */
    public void setHelper(UIHelper helper) {
        this.helper = helper;
    }

    /*
     * Javadoc in interface
     */
    public void completedMessage(MessageComponent message) {}
 
}