package com.dataSonification.v2.sound;

import java.util.Arrays;

import com.dataSonification.v2.data.DataInfo;
import com.dataSonification.v2.util.Key;

class AdaptiveMovementTrainer extends Trainer {

	/**
	 * The keys that this trainer reads.
	 */
	private static final Key[] local_reads = { Key.FIELD2_VALUE, Key.STRONG_THRESH, Key.OUT_THRESH };
	

	public AdaptiveMovementTrainer() {
	    super();
		reads.addAll(Arrays.asList(local_reads));
	}
    
    /*
     * Javadoc in superclass
     */
    DataInfo evaluate() { 
        double current = (Double) cache.get(Key.FIELD2_VALUE);
        double strong_thresh = (Double) cache.get(Key.STRONG_THRESH);
        double out_thresh = (Double) cache.get(Key.OUT_THRESH);

        DataInfo di = DataInfo.SONIFIED;
        if (current <= -strong_thresh) {
            di = DataInfo.UNDERPERFORM;
        } else if (current <= -out_thresh) {
            di = DataInfo.WEAK_UNDERPERFORM;
        } else if (current >= strong_thresh) {
            di = DataInfo.STRONG_OUTPERFORM;
        } else if (current >= out_thresh) {
            di = DataInfo.OUTPERFORM;
        }
       
        return di;
    }
    
}