package com.dataSonification.v2.sound;

import java.util.Map;
import java.util.Arrays;

import com.dataSonification.v2.Const;
import com.dataSonification.v2.util.Key;
import com.softsynth.jmsl.DefaultDimensionNameSpace;

/**
 * Arranges DataEvents from a MovementAnalyzer in terms of single motive. The motive consists of a single sample. The sample
 * is referenced as though by a pitch, as a way to a quick and simple implementation.
 * In this class, if the START_PITCH is used, this indicates upward movement. If END_PITCH is used, downward.
 * @author Edward Childs
 *
 * @see MovementAnalyzer
 */


public class OneNoteArranger extends Arranger {

    /**
     * The keys that this arranger reads.
     */
    private static final Key[] local_reads = { Key.INCREMENT, Key.BASE_PITCH, Key.DURATION,
            Key.LOUDNESS, Key.HOLD, Key.TEMPO };
            
            /**
             * The keys that this arranger writes.
             */
            private static final Key[] local_writes = { Key.LAST_INCREMENT };
            
//	 package-private access
            OneNoteArranger()
            {
                super();
                reads.addAll(Arrays.asList(local_reads));
                writes.addAll(Arrays.asList(local_writes));
            }
            
            
//	 package-private access
	Sonifiable arrange(Map<Key, Object> table) {
        // Get values from hash table
        int base_pitch = (Integer) table.get(Key.BASE_PITCH);
        int end_pitch = base_pitch + Const.TWO_NOTE_INTERVAL;
        
        double duration = (Double) table.get(Key.DURATION);
        double loudness = (Double) table.get(Key.LOUDNESS);
        double hold = (Double) table.get(Key.HOLD);
        double tempo = (Double) table.get(Key.TEMPO);
        
        int increment = (Integer) table.get(Key.INCREMENT);
        int last_increment = (Integer) table.get(Key.LAST_INCREMENT);
        
        // duration, pitch, amplitude, hold
        SonifiableMusicShape music = new SonifiableMusicShape(DefaultDimensionNameSpace.instance());
        
        last_increment = (last_increment == Integer.MIN_VALUE) ? 0:last_increment;
        int pitch = (((increment - last_increment) > 0) ? base_pitch: end_pitch);
        music.add(duration, pitch, loudness, hold);
        music.setTimeStretch(60.0 / tempo);
        
        // write to hash table
        table.put(Key.LAST_INCREMENT, increment);

		return music;
	}
//	 package-private access
    void stop()
    {}

}
