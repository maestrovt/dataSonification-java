package com.dataSonification.v2.sound;

import java.util.Arrays;

import com.dataSonification.v2.Const;
import com.dataSonification.v2.data.DataComponent;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.Subsystem;

/**
 * Analyzes DataEvents in terms of differences between a BASE_FIELD and CURRENT_FIELD.
 * @author Kimo Johnson
 */
//package-private access
class SliderAnalyzer extends Analyzer {
	
	/**
	 * The keys that this analyzer reads.
	 */
	private static final Key[] local_reads = { Key.SIGNIFICANT_MOVE, Key.REF1_FIELD, Key.REF2_FIELD, Key.CURRENT_FIELD, Key.LAST_INCREMENT, Key.LAST_SPAN };
	
	/**
	 * The keys that this analyzer writes.
	 */
	private static final Key[] local_writes = { Key.INCREMENT, Key.SPAN, Key.FIELD1_VALUE, Key.FIELD2_VALUE };
	
	/**
	 * Instantiates the MovementAnalyzer and writes default values to 
	 * the defaults cache.
	 */
	SliderAnalyzer() {
		super();
		reads.addAll(Arrays.asList(local_reads));
		writes.addAll(Arrays.asList(local_writes));
		
		// Setup defaults, min values indicate start condition
		defaults.put(Key.LAST_INCREMENT, Integer.MIN_VALUE);
		defaults.put(Key.LAST_SPAN, Integer.MIN_VALUE);
	}
	
	/* 
	 * Javadoc in superclass
	 */
	boolean analyze(DataComponent data) {
		
    		double avg1 = (Double) data.getField(Key.REF1_FIELD);
    		double avg2 = (Double) data.getField(Key.REF2_FIELD);
    		double price = (Double) data.getField(Key.CURRENT_FIELD);

		double low  = Math.min(avg1, avg2);
		double high = Math.max(avg1, avg2);

		double significant_move = (Double) cache.get(Key.SIGNIFICANT_MOVE);
		
		// Calculate the number of semitones for the spread
		int span = (int)Math.floor((high - low) / significant_move);
		
		int increment;
		int last_increment = (Integer) cache.get(Key.LAST_INCREMENT);
		int last_span = (Integer) cache.get(Key.LAST_SPAN);
		
		if (span == 0) {
		    increment = 0;
		} else {
			 
		    // Calculate the width of each step given that there are nSteps
		    // this should be close but not equal to significant_move
		    double width = (high - low)/span; 
	
		    double dist = (price - low) / width;
		    increment = (int)Math.floor(dist);

			// Adjust increment if current is down
			if (increment < last_increment && (dist - increment) > Const.SIG_MOVE_TOL) {
				increment += 1;
			}
		}
		
		cache.put(Key.INCREMENT, increment);
		cache.put(Key.SPAN, span);
		cache.put(Key.FIELD1_VALUE, avg1);
		cache.put(Key.FIELD2_VALUE, avg2);
		cache.put(Key.FIELD3_VALUE, price);


		Log.println(Subsystem.SOUND, null, "SliderAnalyzer: span: " + span + ", last_span: " + last_span + ", increment: " + increment + ", last_increment: " + last_increment, Log.P_VERBOSE);

		return (increment != last_increment) || (span != last_span);
	}
	
}
