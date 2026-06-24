package com.dataSonification.v2.sound;

import java.util.Arrays;
import java.util.Map;

import com.dataSonification.v2.Const;
import com.dataSonification.v2.util.Key;
import com.softsynth.jmsl.DefaultDimensionNameSpace;


/**
 * Arranges DataEvents from a MovementAnalyzer in terms of three notes: reference, last, and current.
 * @author Kimo Johnson
 *
 * @see MovementAnalyzer
 */
class TwoNoteArranger extends Arranger
{
    
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
            TwoNoteArranger()
            {
                super();
                reads.addAll(Arrays.asList(local_reads));
                writes.addAll(Arrays.asList(local_writes));
            }
            
            
//	 package-private access
            Sonifiable arrange(Map<Key,Object> table)
            {
                
                // Get values from hashtable
                int base_pitch = (Integer) table.get(Key.BASE_PITCH);
                double duration = (Double) table.get(Key.DURATION);
                double loudness = (Double) table.get(Key.LOUDNESS);
                double hold = (Double) table.get(Key.HOLD);
                double tempo = (Double) table.get(Key.TEMPO);
                
                int increment = (Integer) table.get(Key.INCREMENT);
                int last_increment = (Integer) table.get(Key.LAST_INCREMENT);
                
                // duration, pitch, amplitude, hold
                SonifiableMusicShape music = new SonifiableMusicShape(DefaultDimensionNameSpace.instance());
                
                last_increment = (last_increment == Integer.MIN_VALUE) ? 0:last_increment;
                
                int second_pitch = base_pitch
                        + (((increment - last_increment) > 0) ? Const.TWO_NOTE_INTERVAL: -1 * Const.TWO_NOTE_INTERVAL);
                int NREPEATS = Math.min(Const.MAX_TWO_NOTE_REPEATS,Math.abs(increment-last_increment));
                for (int i = 0; i < NREPEATS; i++)
                {
                    music.add(duration, base_pitch, loudness, hold);
                    music.add(duration, second_pitch, loudness, hold);
                }
                
                music.setTimeStretch(60.0 / tempo);
                
                // write to hashtable
                table.put(Key.LAST_INCREMENT, increment);
                return music;
            }
            
            
//	 package-private access
            void stop()
            {}
}