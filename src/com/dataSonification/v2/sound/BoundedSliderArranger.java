package com.dataSonification.v2.sound;

import java.util.Arrays;
import java.util.Map;

import com.dataSonification.v2.util.Key;
import com.softsynth.jmsl.DefaultDimensionNameSpace;


/**
 * 
 * @author Kimo Johnson
 *
 * @see SliderAnalyzer
 */
class BoundedSliderArranger extends Arranger {
	
	/**
	 * The keys that this arranger reads.
	 */
	private static final Key[] local_reads = { Key.INCREMENT, Key.SPAN, Key.BASE_PITCH,
                                                Key.LOUDNESS, Key.TEMPO };
	
	/**
	 * The keys that this arranger writes.
	 */
	private static final Key[] local_writes = { Key.LAST_INCREMENT, Key.LAST_SPAN };

	
	/**
	 * A reference to the last sonifiable returned by this arranger.
	 */
	private SonifiableMusicShape music = null;
	
	BoundedSliderArranger() {
	    super();
		reads.addAll(Arrays.asList(local_reads));
		writes.addAll(Arrays.asList(local_writes));
	}

//	 package-private access
	public Sonifiable arrange(java.util.Map table) {

		// Get values from hashtable
		int base_pitch = (Integer) table.get(Key.BASE_PITCH);
		double loudness = (Double) table.get(Key.LOUDNESS);
		double tempo = (Double) table.get(Key.TEMPO);

		int increment = (Integer) table.get(Key.INCREMENT);
		int last_increment = (Integer) table.get(Key.LAST_INCREMENT);
		int span = (Integer) table.get(Key.SPAN);
		int last_span = (Integer) table.get(Key.LAST_SPAN);
		
		// adjust for the initial condition
		if (last_span == Integer.MIN_VALUE) {
		    last_span = span;
		}
		
		// duration, pitch, amplitude, hold
		music = new SonifiableMusicShape(DefaultDimensionNameSpace.instance());

		// Base pitch may change if we are ticking down
		int last_base_pitch = base_pitch;
		if (span != last_span && increment == 0) {
		    base_pitch -= (span - last_span);
		    table.put(Key.BASE_PITCH, Integer.valueOf(base_pitch));
		}
		
		// if going up
		int first_pitch, second_pitch, old_second_pitch;
		if (increment > last_increment) {
		    first_pitch = base_pitch;
		    second_pitch = base_pitch + span;
		    old_second_pitch = last_base_pitch + last_span;
		} else {
		    first_pitch = base_pitch + span;
		    second_pitch = base_pitch;
		    old_second_pitch = last_base_pitch;
		}
		
		int third_pitch = base_pitch + increment;

		
		if (span != last_span) {
			music.add(0.5, first_pitch, loudness, 5.5);
			music.add(1.0, old_second_pitch, loudness, 0.8);
			music.add(2.0, second_pitch, loudness, 4.0);
			music.add(5.5, third_pitch, loudness+0.3, 0.3);
		} else {
		    music.add(0.5, first_pitch, loudness, 5.5);
			music.add(3.0, second_pitch, loudness, 5.0);
			music.add(5.5, third_pitch, loudness+0.3, 0.3);	
		}
		music.setTimeStretch(60.0 / tempo);

		// write to hashtable
		table.put(Key.LAST_INCREMENT, increment);
		table.put(Key.LAST_SPAN, span);
		return music;
	}

    
}