package com.dataSonification.v2.sound;

import java.util.Arrays;

import com.dataSonification.v2.data.DataInfo;
import com.dataSonification.v2.data.InfoType;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.Subsystem;

class AdaptiveUnboundedTrainer extends Trainer {

    /**
	 * The keys that this trainer reads.
	 */
	private static final Key[] local_reads = { Key.INCREMENT, Key.SPAN, Key.LAST_INCREMENT, 
	        Key.AVERAGE1, Key.AVERAGE2, Key.FIELD1_VALUE, Key.FIELD2_VALUE };
    

	AdaptiveUnboundedTrainer() {
	    super();
		reads.addAll(Arrays.asList(local_reads));
	}
	
    /*
     * Javadoc in superclass.
     */
    DataInfo evaluate() {
        String average1 = (String) cache.get(Key.AVERAGE1);
        String average2 = (String) cache.get(Key.AVERAGE2);
        
        double avg1 = (Double) cache.get(Key.FIELD1_VALUE);
        double avg2 = (Double) cache.get(Key.FIELD2_VALUE);
        
        int increment = (Integer) cache.get(Key.INCREMENT);
        int last_increment = (Integer) cache.get(Key.LAST_INCREMENT);
        
        int span = (Integer) cache.get(Key.SPAN);
        
        InfoType type = InfoType.NO_INFO;
        String info = null;        
        
        boolean init = (last_increment == Integer.MIN_VALUE);
        
        if (crossed(increment, last_increment, init)) {
            // We crossed the lower of the two
            Log.println(Subsystem.SOUND, null, "Crossed lower: " + average1 + " = " + avg1 + ", " + average2 + " = " + avg2);
            info = avg1 < avg2 ? average1 : average2;
            type = InfoType.CROSS;
        } else if (crossed(increment - span, last_increment - span, init)) {
            // We crossed the greater of the two
            info = avg1 > avg2 ? average1 : average2;
            type = InfoType.CROSS;
        }
        
        return new DataInfo(type, info);
    }
    
    private boolean crossed(int i1, int i2, boolean init) {
        if (i1 == 0)
            return true;
        
        if (init)
            return false;
        
        return (i1 > 0 && i2 < 0) || (i1 < 0 && i2 > 0);
    }
}