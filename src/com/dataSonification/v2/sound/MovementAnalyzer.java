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
class MovementAnalyzer extends Analyzer {
	
	/**
	 * The data keys that this analyzer reads.
	 */
	private static final Key[] local_reads = { Key.SIGNIFICANT_MOVE, Key.REF1_FIELD, Key.CURRENT_FIELD, Key.LAST_INCREMENT };

	/**
	 * The keys that this analyzer writes.
	 */
	private static final Key[] local_writes = { Key.INCREMENT, Key.FIELD2_VALUE };
	
	/**
	 * Instantiates the MovementAnalyzer and writes default values to 
	 * the defaults cache.
	 */
	MovementAnalyzer() {
		super();
		reads.addAll(Arrays.asList(local_reads));
		writes.addAll(Arrays.asList(local_writes));
		
		// Setup any defaults, min value so no change on start is up
		defaults.put(Key.LAST_INCREMENT, Integer.MIN_VALUE);
	}

	/* 
	 * Javadoc in superclass
	 */
	boolean analyze(DataComponent data) {
		
	    double base = (Double) data.getField(Key.REF1_FIELD);
	    double current = (Double) data.getField(Key.CURRENT_FIELD);
	    
		double significant_move = (Double) cache.get(Key.SIGNIFICANT_MOVE);
		double dist = (current - base) / significant_move;
		int last_increment = (Integer) cache.get(Key.LAST_INCREMENT);

		int increment = computeIncrement(dist, last_increment);

		cache.put(Key.INCREMENT, increment);
		cache.put(Key.FIELD2_VALUE, current);

		Log.println(Subsystem.SOUND, null, "MovementAnalyzer: increment: " + increment + " last: " + 
		       (last_increment == Integer.MIN_VALUE ? "init" : Integer.toString(last_increment)));
		return increment != last_increment;
	}
	
	static int computeIncrement(double dist, int last_increment) {
		int increment = (int)Math.floor(dist);
		
		// adjust increment if it is numerically close to the next increment
		if ((increment+1 - dist) < Const.SIG_MOVE_TOL) {
		    increment += 1;
		}

		// Adjust increment if current is down
		if (increment < last_increment && (dist - increment) > Const.SIG_MOVE_TOL) {
			increment++;
		}
		return increment;
	}
}
