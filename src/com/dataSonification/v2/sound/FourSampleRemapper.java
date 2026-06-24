package com.dataSonification.v2.sound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.Subsystem;

public class FourSampleRemapper implements Remapper {
	
	private int start_pitch;
	private int step;
	private int end_pitch;
	private String file_base;
	private String file_type;
	private List<SampleInfo> samples;
	private static final int offset1s = 100;
    private static final int offset2s = 200;
    private static final int offset4s = 300;
	
	public void setState(int st_p, int s, int ed_p, String base, String type) {
		start_pitch = st_p;
		step = s;
		end_pitch = ed_p;
		file_base = new String(base);
		file_type = new String(type);
		
		// Allocate room for samples
		samples = new ArrayList<SampleInfo>(2*(int)Math.ceil((end_pitch-start_pitch)/step));
	
		/*for (int pitch = start_pitch; pitch <= end_pitch; pitch += step) {
			samples.add(new SampleInfo(file_base + Integer.toString(pitch)+file_type, pitch));
			samples.add(new SampleInfo(file_base+"1s_" + Integer.toString(pitch)+ file_type, pitch + offset1s));
            samples.add(new SampleInfo(file_base+"2s_" + Integer.toString(pitch)+ file_type, pitch + offset2s));
            samples.add(new SampleInfo(file_base+"4s_" + Integer.toString(pitch)+ file_type, pitch + offset4s));
		}*/
	}
	
	public Iterator<SampleInfo> iterator() {
		return samples.iterator();
	}
	
	public int getAlternativeSampleIndex(int pitchIndex, double timeStretch, double[] data) {
		// data[3] is the hold amount
		double hold = data[3]*timeStretch;
        Log.println(Subsystem.SOUND,null,"FourSampleRemapper got hold: " + hold, Log.P_VERBOSE);
		if (hold <= 0.5)
			return pitchIndex;
        else if(hold > 0.5 && hold <= 1.5)
            return pitchIndex + offset1s;
		else if(hold > 1.5 && hold < 4.0)
            return pitchIndex + offset2s;
        else
			return pitchIndex + offset4s;
	}
    
    private int getOffset(double hold)
    {
        if (hold <= 0.5)
			return 0;
        else if(hold > 0.5 && hold <= 1.5)
            return offset1s;
		else if(hold > 1.5 && hold < 4.0)
            return offset2s;
        else
			return offset4s;
    }
    
    private String getOffsetString(double hold)
    {
        return getOffsetString(getOffset(hold));      
    }
    
    private String getOffsetString(int offset)
    {
        switch(offset)
        {
            case 0:
                return "";
            case offset1s:
                return "1s_";
            case offset2s:
                return "2s_";
            case offset4s:
                return "4s_";
            default:
                return "";
        }
    }
    
    public SampleInfo[] getSamplesFor(int pitchIndex, double timeStretch, double[] data)
    {
        double hold = data[3]*timeStretch;
        int offset = getOffset(hold);
        String suffix = getOffsetString(offset);

        SampleInfo[] ret = null;
        if(pitchIndex > start_pitch && pitchIndex < end_pitch)
        {
            if((pitchIndex-start_pitch) % step == 0)
            {
                ret = new SampleInfo[1];
                
                ret[0] =  new SampleInfo(file_base+suffix+pitchIndex+file_type,pitchIndex+offset);
            }
            else
            {
                int lowPitch = pitchIndex - ((pitchIndex-start_pitch)%step);
                int highPitch = lowPitch+step;
                ret = new SampleInfo[2];
                 
                ret[0] =  new SampleInfo(file_base+suffix+lowPitch+file_type,lowPitch+offset); 
                ret[1] = new SampleInfo(file_base+suffix+highPitch+file_type,highPitch+offset);
            }
                
        }
        return ret;  
    }
	
}