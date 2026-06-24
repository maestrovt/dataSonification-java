package com.dataSonification.v2.sound;

import java.util.Arrays;
import java.util.Map;

import com.dataSonification.v2.Const;
import com.dataSonification.v2.util.Key;
import com.softsynth.jmsl.DefaultDimensionNameSpace;


/**
 * 
 * @author Kimo Johnson
 *
 * @see SizeAnalyzer
 */
class SizeArranger extends Arranger {
	
	/**
	 * The keys that this arranger reads.
	 */
	private static final Key[] local_reads = { Key.INCREMENT, Key.TRILL_LENGTH, 
		Key.BASE_PITCH, Key.LOUDNESS, Key.TEMPO, Key.HOLD };
	
	/**
	 * The keys that this arranger writes.
	 */
	private static final Key[] local_writes = { Key.LAST_INCREMENT };
	
//	 package-private access
	SizeArranger() {
	    super();
		reads.addAll(Arrays.asList(local_reads));
		writes.addAll(Arrays.asList(local_writes));
	}


//	 package-private access
	Sonifiable arrange(Map<Key,Object> table) {
	    
		// Get values from hashtable
		int base_pitch = (Integer) table.get(Key.BASE_PITCH);
		double loudness = (Double) table.get(Key.LOUDNESS);
		double tempo = (Double) table.get(Key.TEMPO);
		double hold = (Double) table.get(Key.HOLD);
		
		int increment = (Integer) table.get(Key.INCREMENT);
		int trill_length = (Integer) table.get(Key.TRILL_LENGTH);
		int last_increment = (Integer) table.get(Key.LAST_INCREMENT);
		
		// Handle the initialization case
		last_increment = (last_increment == Integer.MIN_VALUE) ? 0 : last_increment;
		
		// duration, pitch, amplitude, hold
		SonifiableMusicShape music = new SonifiableMusicShape(DefaultDimensionNameSpace.instance());

		int second_pitch = base_pitch + Const.TRILL_INTERVAL;
		
		for (int i = 0; i < trill_length; i++) {
            music.add(0.125, base_pitch, loudness, hold*0.125);
            music.add(0.125, second_pitch, loudness, hold*0.125);
        }
		if (Math.abs(increment) > 0) {
			// Second and third notes of ThreeNoteArranger
			music.add(1.0, base_pitch, loudness, hold*1.0);
			music.add(1.0, base_pitch + last_increment, loudness, hold*1.0);
			music.add(1.0, base_pitch + increment, loudness, hold*1.0);
		} else {
			music.add(1.0, base_pitch, loudness, hold*1.0);
		}

		music.setTimeStretch(60.0 / tempo);

		// write to map
		table.put(Key.LAST_INCREMENT, increment);
		return music;
	}
	
	
//	 package-private access
	void stop() {}
}