package com.dataSonification.v2.sound;

import com.dataSonification.v2.data.DataComponent;


/**
 * Abstract class outlining the Analyzer component of a Sonification. 
 * @author Kimo Johnson
 */
//package-private access
abstract class Analyzer extends SonificationComponent {
    
	/**
	 * Analyzes a DataEvent.
	 * @param e the DataEvent to analyze
	 * @return true to sonify, false to ignore
	 */
	abstract boolean analyze(DataComponent e);
}