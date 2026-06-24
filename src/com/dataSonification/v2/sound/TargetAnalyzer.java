package com.dataSonification.v2.sound;

import java.util.Arrays;

import com.dataSonification.v2.data.DataComponent;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.Subsystem;
/**
 * Analyzes DataEvents in terms of differences between a REF1_FIELD and
 * CURRENT_FIELD.
 * 
 * @author Kimo Johnson
 */
//package-private access
class TargetAnalyzer extends MovementAnalyzer {

    /**
     * The keys that this analyzer reads.
     */
    private static final Key[] local_reads = { Key.TARGET_FIELD, Key.CURRENT_FIELD, Key.REF1_FIELD, Key.SIGNIFICANT_MOVE };

    /**
     * The keys that this analyzer writes.
     */
    private static final Key[] local_writes = { Key.TARGET_INCREMENT };

    /**
     * Instantiates the MovementAnalyzer and writes default values to the
     * defaults cache.
     */
    TargetAnalyzer() {
        super();
        reads.addAll(Arrays.asList(local_reads));
        writes.addAll(Arrays.asList(local_writes));

        defaults.put(Key.TARGET_INCREMENT, 12);
    }


    /*
     * Javadoc in superclass
     */
    boolean analyze(DataComponent data) {

        boolean to_sonify = super.analyze(data);
        if (!to_sonify) {
            return false;
        }

        double target = (Double) data.getField(Key.TARGET_FIELD);
        double current = (Double) data.getField(Key.CURRENT_FIELD);
        double base = (Double) data.getField(Key.REF1_FIELD);
        
        double significant_move = (Double) cache.get(Key.SIGNIFICANT_MOVE);
        double target_dist = (target - base) / significant_move;
        int target_steps = (int) Math.floor(target_dist);

        int increment = (Integer) cache.get(Key.INCREMENT);
        
        int target_increment = target_steps - increment;

        cache.put(Key.TARGET_INCREMENT, target_increment);

        Log.println(Subsystem.SOUND, null, "TargetAnalyzer: target increment: " + target_increment);
        return true;
    }
}

