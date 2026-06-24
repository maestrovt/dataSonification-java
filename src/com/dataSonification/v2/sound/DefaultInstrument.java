package com.dataSonification.v2.sound;

import com.dataSonification.v2.Config;
import com.dataSonification.v2.sound.ConfigurableInstrument;
import com.softsynth.jmsl.InstrumentAdapter;
import com.softsynth.jmsl.JMSL;


public class DefaultInstrument extends InstrumentAdapter implements ConfigurableInstrument {
	public double play(double playTime, double timeStretch, double dar[]) {
		JMSL.out.println("DefaultInstrument.play() is handed the following array of doubles:");
		JMSL.printDoubleArray(dar);
		JMSL.out.println();
		return playTime + (dar[0] * timeStretch); // interpret dar[0] as duration
	}

    /*
     * Javadoc in interface
     */
    public void setConfig(Config c) {
    }
}