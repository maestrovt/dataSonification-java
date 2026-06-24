/*
 * UI.java
 *
 * Created on April 28, 2004, 12:47 PM
 */

package com.dataSonification.v2.ui;

import com.dataSonification.v2.ID;
import com.dataSonification.v2.data.DataInfo;
import com.dataSonification.v2.data.MessageComponent;


/**
 * Superclass for all UI's
 * @author ben
 */
public interface UI {
    
   /**
    * When core's mode changes this method is called to update the UI
    * @param mode The new mode of the core
    */   
   public void setMode(int mode);
   
   public void reset();
   
   public void uiJob(ID id, DataInfo info);
   
   public void setHelper(UIHelper helper);
   
   public void completedMessage(MessageComponent message);
    
}
