package com.dataSonification.v2.sound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * A very simple remapper to go with the OneNoteArranger, no sample interpolation and no offset for special lengths.
 * @author Edward Childs
 */
public class DiscreteRemapper implements Remapper {
	private int start_pitch;
	private int step;
	private int end_pitch;
	private String file_base;
	private String file_type;
	private List<SampleInfo> samples;

	public int getAlternativeSampleIndex(int pitchIndex, double timeStretch,
			double[] data) {
		return pitchIndex;
	}

	public SampleInfo[] getSamplesFor(int pitchIndex, double timeStretch,
			double[] data) {
        SampleInfo[] ret = null;
        if(pitchIndex >= start_pitch && pitchIndex <= end_pitch)
        {
            ret = new SampleInfo[1];
            ret[0] =  new SampleInfo(file_base+pitchIndex+file_type,pitchIndex);
        }
                
		return ret;
	}

	public Iterator<SampleInfo> iterator() {
		return samples.iterator();
	}

	public void setState(int st_p, int s, int ed_p, String base, String type) {
		start_pitch = st_p;
		step = s;
		end_pitch = ed_p;
		file_base = new String(base);
		file_type = new String(type);
		
		// Allocate room for samples
		samples = new ArrayList<SampleInfo>(2*(int)Math.ceil((end_pitch-start_pitch)/step));

	}

}
