package com.dataSonification.v2.sound;

import java.util.Arrays;
import java.util.Map;

import com.dataSonification.v2.sound.ConfigurableInstrument;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.softsynth.jmsl.JMSL;
import com.dataSonification.v2.util.Subsystem;
import com.dataSonification.v2.util.ReturnCode;

/**
 * Creates arrangements that set the pitch and velocity of a continuously
 * playing midi instrument.
 * 
 * @author Kimo Johnson
 */
class ContinuousArranger extends Arranger {
	
	/**
	 * The keys that this analyzer reads.
	 */
	private static final Key[] local_reads = { Key.INCREMENT, Key.BASE_PITCH, Key.LOUDNESS };
	
	/**
	 * The keys that this analyzer writes.
	 */
	private static final Key[] local_writes = { Key.LAST_INCREMENT };

	/**
	 * Reference to a continuous Instrument.
	 */
	private SonifiableMidiInstrument sonifiable_ins;
	
	ContinuousArranger() {
	    super();
		reads.addAll(Arrays.asList(local_reads));
		writes.addAll(Arrays.asList(local_writes));
	}
	
	/* 
	 * Javadoc in superclass
	 */
	void setInstrument(ConfigurableInstrument ins) {
		if (!(ins instanceof SonifiableMidiInstrument))
		    throw new IllegalArgumentException("ContinuousArranger requires a SonifiableMidiInstrument.");
		
		sonifiable_ins = (SonifiableMidiInstrument)ins;
	
		try {
			sonifiable_ins.open(JMSL.now());
		} catch (InterruptedException e) {
			Log.println(Subsystem.SOUND, ReturnCode.GENERAL_ERROR, "ContinuousArranger: caught InterruptedException in setInstrument", Log.P_ERROR);
		}
	}
	
	/* 
	 * Javadoc in superclass
	 */
	Sonifiable arrange(Map<Key,Object> table) {
		
		int base_pitch = (Integer) table.get(Key.BASE_PITCH);
		double loudness = (Double) table.get(Key.LOUDNESS);
		int increment = (Integer) table.get(Key.INCREMENT);
		
		int current_pitch = base_pitch + increment;

		sonifiable_ins.addMidiEvent(current_pitch, (int)80);

		// Write back to table
		table.put(Key.LAST_INCREMENT, increment);

		return sonifiable_ins;
	}
	
}
