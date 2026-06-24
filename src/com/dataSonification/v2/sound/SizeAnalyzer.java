package com.dataSonification.v2.sound;

import java.util.Arrays;

import com.dataSonification.v2.data.DataComponent;
import com.dataSonification.v2.util.Key;

/**
 * Parameters
 *
 *
 * Significant move, threshold, threshold change.
 *
 * Data
 *
 * Current field, reference field, size field.
 *
 * Description
 *
 * The analyzer will examine the Data Event, consisting of three Data Items, and
 * test whether the size field exceeds the user defined threshold. If so, then a
 * sonification should occur. Current field, reference field, significant move
 * and threshold change are passed on to the Size Arranger in the event that a
 * sonification should occur, in order to determine how many trill are played,
 * and whether the second and third note are played.
 *
 * This sonification strategy, in this application, applies to executed trades.
 * Since each trade is by definition a unique event, the sonification should be
 * true even if a Data Event occurs in which all three Data Items are exactly
 * the same as the previous event.
 *
 */
class SizeAnalyzer extends Analyzer
{
    
    /**
     * The data keys that this analyzer reads.
     */
    private static final Key[] local_reads = { Key.CURRENT_FIELD, Key.REF1_FIELD, Key.SIZE_FIELD,
            Key.SIGNIFICANT_MOVE, Key.THRESHOLD, Key.THRESHOLD_CHANGE };
            
            /**
             * The keys that this analyzer writes.
             */
            private static final Key[] local_writes = { Key.INCREMENT, Key.LAST_INCREMENT, Key.TRILL_LENGTH };
            
            
//	 package-private access
            SizeAnalyzer()
            {
                super();
                reads.addAll(Arrays.asList(local_reads));
                writes.addAll(Arrays.asList(local_writes));
                
                defaults.put(Key.LAST_INCREMENT, Integer.MIN_VALUE);
            }
            
            
//	 package-private access
            boolean analyze(DataComponent data)
            {
                
                double base = (Double) data.getField(Key.REF1_FIELD);
                double current = (Double) data.getField(Key.CURRENT_FIELD);
                int size = (Integer) data.getField(Key.SIZE_FIELD);
                
                double sig_move = (Double) cache.get(Key.SIGNIFICANT_MOVE);
                int threshold = (Integer) cache.get(Key.THRESHOLD);
                int thresholdChange = (Integer) cache.get(Key.THRESHOLD_CHANGE);
                int last_increment = (Integer) cache.get(Key.LAST_INCREMENT);
                
                double dist = (current - base)/sig_move;
                int increment = MovementAnalyzer.computeIncrement(dist, last_increment);
                
                int trill_length = (size - threshold)/thresholdChange + 1;
                
                cache.put(Key.INCREMENT, increment);
                cache.put(Key.TRILL_LENGTH, trill_length);
                
                //To make it consistant w/ trill analyzer changed from
                // > to >= -Ben Childs
                return size >= threshold;
            }
}