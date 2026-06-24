/*
 * Sonifiable.java
 *
 * Created on May 25, 2004
 */
package com.dataSonification.v2.sound;

import com.dataSonification.v2.sound.ConfigurableInstrument;

/**
 * The Sonifiable interface hides the details of how an object
 * makes sound.  The Gatekeeper accepts and plays Sonifiables.
 * @author Kimo Johnson
 */
// package-private access
interface Sonifiable {
	/**
	 * Called by Gatekeeper to play the Sonifiable.
	 * @param startTime when to sonify
	 * @throws InterruptedException if Sonifiable is interrupted
	 */
	void sonify(double startTime) throws InterruptedException;
	
	/**
	 * Called by Gatekeeper to set the instrument of the sonifiable
	 * @param ins
	 */
	void setInstrument(ConfigurableInstrument ins);
	
	/**
	 * Causes the Sonifiable to finish now.
	 */
	void finishAll();
    
    boolean isBlank();
}
