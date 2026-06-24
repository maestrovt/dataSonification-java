package com.dataSonification.v2.sound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.Subsystem;

public class DefaultRemapper implements Remapper {
	
	private int start_pitch;
	private int step;
	private int end_pitch;
	private String file_base;
	private String file_type;
	private List<SampleInfo> samples;
	
	public void setState(int st_p, int s, int ed_p, String base, String type) {
		start_pitch = st_p;
		step = s;
		end_pitch = ed_p;
		file_base = new String(base);
		file_type = new String(type);
		
		samples = new ArrayList<SampleInfo>((int)Math.ceil((end_pitch-start_pitch)/step));
	
		//for (int pitch = start_pitch; pitch <= end_pitch; pitch += step) {
		//	samples.add(new SampleInfo(file_base+Integer.toString(pitch)+file_type, pitch));
		//}
	}
	
	public Iterator<SampleInfo> iterator() {
		return samples.iterator();
	}
	
	public int getAlternativeSampleIndex(int pitchIndex, double timeStretch, double[] data) {
		
        
        return pitchIndex;
	}
    
    public SampleInfo[] getSamplesFor(int pitchIndex, double timeStretch, double[] data)
    {
        SampleInfo[] ret = null;
        if(pitchIndex > start_pitch && pitchIndex < end_pitch)
        {
            if((pitchIndex-start_pitch) % step == 0)
            {
                ret = new SampleInfo[1];
                
                ret[0] =  new SampleInfo(file_base+pitchIndex+file_type,pitchIndex);
            }
            else
            {
                int lowPitch = pitchIndex - ((pitchIndex-start_pitch)%step);
                int highPitch = lowPitch+step;
                ret = new SampleInfo[2];
                 
                ret[0] =  new SampleInfo(file_base+lowPitch+file_type,lowPitch); 
                ret[1] = new SampleInfo(file_base+highPitch+file_type,highPitch);
            }
                
        }
        return ret;
    }
}