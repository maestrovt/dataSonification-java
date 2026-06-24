/*
 * FourNoteArranger.java
 *
 * Created on February 10, 2005, 3:38 PM
 */

package com.dataSonification.v2.sound;

import java.util.Arrays;
import java.util.Map;

import com.dataSonification.v2.Const;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.softsynth.math.AudioMath;
import com.dataSonification.v2.util.Subsystem;

/**
 *
 * @author  epc
 */
public class FourNoteArranger extends ThreeNoteArranger {
	
	/**
	 * The data keys that this arranger reads.
	 */
	private static final Key[] local_reads = { Key.TARGET_DISTANCE, Key.TARGET_INCREMENT, Key.INCREMENT, Key.BASE_PITCH };
	
    /** Creates a new instance of FourNoteArranger */
    public FourNoteArranger() {
        super();
        reads.addAll(Arrays.asList(local_reads));

    }
    
    public Sonifiable arrange(Map<Key,Object> table) {
        Log.println(Subsystem.SOUND, null, "FourNoteArranger: arrange");
        SonifiableMusicShape music = (SonifiableMusicShape) super.arrange(table);
        
        int increment = (Integer) table.get(Key.INCREMENT);
        int target_increment = (Integer) table.get(Key.TARGET_INCREMENT);
        int target_distance = (Integer) table.get(Key.TARGET_DISTANCE);
        
        int target_pitch = (Integer) table.get(Key.BASE_PITCH) + increment + target_increment;
        Log.println(Subsystem.SOUND, null, "FourNoteArranger: target_pitch " + target_pitch);
        double duration = (Double) table.get(Key.DURATION);
       
        
        if ( Math.abs(target_increment) <= target_distance)
        {
            double target_loudness = AudioMath.decibelsToAmplitude(-(double)Math.abs(target_increment));
            double target_hold, target_duration;
            if (target_increment == 0)
            {
                target_hold = Const.MAX_DURATION * Const.CROSSOVER_MULT;
                target_duration = target_hold;
            }
            else
            {
                target_hold = (Const.MAX_DURATION/(double)target_distance) * (1+(target_distance - Math.abs(target_increment)));
                Log.println(Subsystem.SOUND, null, "Target Hold: " + target_hold, Log.P_VERBOSE);
                target_duration = target_hold * Const.FOUR_NOTE_HOLD_RATIO;
            }
            music.add(target_duration, target_pitch, target_loudness, target_hold);
        
        }
 
        return music;
    }    
}
