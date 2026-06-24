package com.dataSonification.v2.sound;

import java.util.Arrays;
import java.util.Map;

import com.dataSonification.v2.util.Key;



class TrillArranger extends Arranger {
	/**
	 * The keys that this arranger reads.
	 */
	private static final Key[] local_reads = { Key.TOP_TRILL_LENGTH, Key.INCREMENT, Key.BOTTOM_TRILL_LENGTH,
			Key.BASE_PITCH, Key.DURATION, Key.HOLD, Key.LOUDNESS, Key.TEMPO };
	

//	 package-private access
	TrillArranger() {
		super();
		reads.addAll(Arrays.asList(local_reads));
	}

//	 package-private access
	Sonifiable arrange(Map<Key,Object> table) {
        
        int increment = (Integer) table.get(Key.INCREMENT);
        int topTrillLength = (Integer) table.get(Key.TOP_TRILL_LENGTH);
        int bottomTrillLength = (Integer) table.get(Key.BOTTOM_TRILL_LENGTH);
        
        
        int basePitch = (Integer) table.get(Key.BASE_PITCH);
        double hold = (Double) table.get(Key.HOLD);
        double loudness = (Double) table.get(Key.LOUDNESS);
        double tempo = (Double) table.get(Key.TEMPO);
        
        
        SonifiableMusicShape music = new SonifiableMusicShape(4);
       
        if (bottomTrillLength > 0) {
            int firstPitch = basePitch;
            int secondPitch = basePitch + increment;
	        for(int i = 0; i < bottomTrillLength; i++) {
	            music.add(0.125, firstPitch, loudness, hold*0.125);
	            music.add(0.125, secondPitch, loudness, hold*0.125);
	        }
	        music.add(1.0, firstPitch, loudness, hold*1.0);
	        
	        // The last note needs to be longer (+ half rest) if more notes follow
	        if (topTrillLength > 0) {
				
	            music.add(4.0, firstPitch, loudness, hold*2.0);
	        } else {
	            music.add(2.0, firstPitch, loudness, hold*2.0);
	        }
	        
        }
        
        if (topTrillLength > 0) {
            int firstPitch = basePitch + increment;
            int secondPitch = basePitch;
	        for(int i = 0; i < topTrillLength; i++) {
	              music.add(0.125, firstPitch, loudness, hold*0.125);
	              music.add(0.125, secondPitch, loudness, hold*0.125);
	        }
        
	        music.add(1.0, firstPitch, loudness, hold*1.0);
	        music.add(2.0, firstPitch, loudness, hold*2.0);
        }
        

        music.setTimeStretch( 60.0/tempo );

        return music;
	}
	
}