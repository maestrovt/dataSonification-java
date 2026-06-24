package com.dataSonification.v2.sound;

import java.util.Arrays;
import java.util.Map;

import com.dataSonification.v2.util.Key;
import com.softsynth.jmsl.DefaultDimensionNameSpace;

class DefaultArranger extends Arranger {
    
	/**
	 * The data keys that this arranger reads.
	 */
    private static final Key[] local_reads = { Key.INCREMENT };
    
    DefaultArranger() {
        super();
        reads.addAll(Arrays.asList(local_reads));
    }


    /*
     * Javadoc in superclass.
     */
    Sonifiable arrange(Map<Key,Object> m) {
        int increment = (Integer) m.get(Key.INCREMENT);
        
        SonifiableMusicShape music = new SonifiableMusicShape(DefaultDimensionNameSpace.instance());
        
        music.add(1.0, 60+increment, 0.8, 0.9);
        
        music.setTimeStretch(60.0);
        
        m.put(Key.LAST_INCREMENT,  increment);
        return music;
    }

}