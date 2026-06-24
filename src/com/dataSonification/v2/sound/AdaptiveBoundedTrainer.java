package com.dataSonification.v2.sound;

import java.util.Arrays;

import com.dataSonification.v2.data.DataInfo;
import com.dataSonification.v2.util.Key;

class AdaptiveBoundedTrainer extends Trainer {

	/**
	 * The keys that this trainer reads
	 */
	private static final Key[] local_reads = { Key.INCREMENT, Key.SPAN, Key.LAST_SPAN };

	public AdaptiveBoundedTrainer() {
		super();

		reads.addAll(Arrays.asList(local_reads));
	}

	/*
	 * Javadoc in superclass
	 */
	DataInfo evaluate() {
		int increment = (Integer) cache.get(Key.INCREMENT);

		int span = (Integer) cache.get(Key.SPAN);
		int last_span = (Integer) cache.get(Key.LAST_SPAN);

		DataInfo di = DataInfo.SONIFIED;
		if (span != last_span) {
			if (increment == 0) {
				di = DataInfo.BREAK_LOW;
			} else if (increment == span) {
				di = DataInfo.BREAK_HIGH;
			}
		}
		return di;
	}

}