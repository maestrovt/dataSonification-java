package com.dataSonification.v2.sound;

import java.util.Arrays;

import com.dataSonification.v2.data.DataInfo;
import com.dataSonification.v2.util.Key;

class ColorTickerTrainer extends Trainer {

	/**
	 * The keys that this analyzer reads.
	 */
	private static final Key[] local_reads = { Key.INCREMENT };
	

	ColorTickerTrainer() {
		super();
		reads.addAll(Arrays.asList(local_reads));
	}
    
    /*
     * Javadoc in
     */
    DataInfo evaluate() {
        int increment = (Integer) cache.get(Key.INCREMENT);
		DataInfo action = DataInfo.EVEN_BASE;
		if (increment > 0)
			action = DataInfo.UP_BASE;
		else if(increment < 0)
			action = DataInfo.DOWN_BASE;
		return action;
    }
    
}