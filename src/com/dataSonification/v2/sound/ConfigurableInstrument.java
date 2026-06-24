package com.dataSonification.v2.sound;

import com.dataSonification.v2.Config;
import com.softsynth.jmsl.Instrument;

/**
 * The ConfigurableInstrument interface adds a setConfig method
 * to the com.softsynth.jmsl.Instrument interface.
 * 
 * @author Kimo Johnson
 *
 */
public interface ConfigurableInstrument extends Instrument {
	public void setConfig(Config c);
}
