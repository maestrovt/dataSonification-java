package com.dataSonification.v2.sound;

import java.util.Arrays;
import java.util.Map;

import com.dataSonification.v2.util.Key;
import com.softsynth.jmsl.DefaultDimensionNameSpace;
import com.dataSonification.v2.Const;

/**
 * Arranges DataEvents from a MovementAnalyzer in terms of three notes: reference, last, and current.
 * @author Kimo Johnson
 *
 * @see MovementAnalyzer
 */
class ThreeNoteArranger extends Arranger {
	
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
	ThreeNoteArranger() {
	    super();
		reads.addAll(Arrays.asList(local_reads));
		writes.addAll(Arrays.asList(local_writes));
	}

//	 package-private access
	Sonifiable arrange(Map<Key,Object> table) {
	    
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

		int second_pitch = base_pitch
				+ (last_increment == Integer.MIN_VALUE ? 0 : last_increment);
		int third_pitch = base_pitch + increment;
        
        
        music.add(duration, base_pitch, loudness, hold);
        
        if (Math.abs(last_increment) > Const.MAX_INCREMENT)
        {
            second_pitch = base_pitch + (last_increment < 0 ? -1 : 1) * Const.MAX_INCREMENT;
                   
        }
            
        music.add(duration, second_pitch, loudness, hold);
        
        
        if (Math.abs(increment) > Const.MAX_INCREMENT)
        {
            third_pitch = base_pitch + (increment < 0 ? -1 : 1) * Const.MAX_INCREMENT;
              
            int num_notes = Math.abs(increment) - Const.MAX_INCREMENT+1;
            
            if(num_notes > Const.MAX_ALARM_NOTES)
                num_notes = Const.MAX_ALARM_NOTES;
            
            while(num_notes > 0)
            {
                music.add(duration, third_pitch, loudness, hold);
                num_notes--;
            }
            
        }
        else
        {   
            music.add(duration, third_pitch, loudness, hold);
        }
		music.setTimeStretch(60.0 / tempo);

		// write to hashtable
		table.put(Key.LAST_INCREMENT, increment);
		return music;
	}
	
}