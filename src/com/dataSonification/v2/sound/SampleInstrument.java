package com.dataSonification.v2.sound;

import java.util.Iterator;

import com.dataSonification.v2.Config;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.softsynth.jmsl.DefaultDimensionNameSpace;
import com.softsynth.jmsl.DimensionNameSpace;
import com.softsynth.jmsl.jsyn.TransposingSampleSustainingInstrument;

import com.dataSonification.v2.util.Subsystem;
import java.util.Vector;

public class SampleInstrument extends TransposingSampleSustainingInstrument implements ConfigurableInstrument
{
    
    /**
     * The config object for this instrument.
     */
    private Config config = null;

    private Remapper remapper = null;
    
	public SampleInstrument() {
		this(com.softsynth.jmsl.view.SampleFinderDialog.getDirectory());
	}
	
	public SampleInstrument(String directory) {
	    super(directory);
	}

	public void buildFromAttributes() {
	    if (config == null)
	        throw new IllegalStateException("config is not set.");

	   // Log.println(Subsystem.SOUND, null, "SampleInstrument: sample directory: " + com.softsynth.jmsl.view.SampleFinderDialog.getDirectory(), Log.P_VERBOSE);
	    int base_pitch = (Integer) config.getField(Key.START_PITCH);

        int step = (Integer) config.getField(Key.STEP);
        int end_pitch = (Integer) config.getField(Key.END_PITCH);
        
        String file_base = (String) config.getField(Key.FILE_BASE);
        String file_type = (String) config.getField(Key.FILE_TYPE);

        try {
        		Class<?> remapper_class = (Class) config.getField(Key.REMAPPER);
        		remapper = (Remapper) remapper_class.newInstance();
        } catch (Exception e) {
        		remapper = new DefaultRemapper();
        }
        
        remapper.setState(base_pitch, step, end_pitch, file_base, file_type);
        
        for (Iterator<SampleInfo> it = remapper.iterator(); it.hasNext(); ) {
        		SampleInfo si = it.next();
        		addSamplePitch(si.filename, si.index);
        }
        
        super.buildFromAttributes();
    }
    
    public DimensionNameSpace getDimensionNameSpace()
    {
        return DefaultDimensionNameSpace.instance();
    }

    /*
     * Javadoc in interface.
     */
    public void setConfig(Config c)
    {
        this.config = c;
        buildFromAttributes();
    }
    
    public void preload(SonifiableMusicShape s)
    {
         Log.println(Subsystem.SOUND,null, "Preloading...", Log.P_VERBOSE);

         Vector data = s.getData();
         for (Iterator it = data.iterator(); it.hasNext(); ) {
        		double[] dataar = (double[])it.next();
                preload((int)dataar[1],s.getTimeStretch(),dataar);
        }
    }
    
    public void preload(int pitchIndex, double timeStretch, double[] data)
    {
        SampleInfo[] newSamples = remapper.getSamplesFor(pitchIndex,timeStretch, data);
        if(newSamples != null)
        {
            boolean bNewSamples = false;
            
            int ilength = newSamples.length;
            
            for(int i = 0; i < ilength; i++)
            {
            	int ind1 = findClosestSampleIndex(newSamples[i].index);
            	int ind2 = newSamples[i].index;
            	if( ind1  != ind2 )
                {
                    bNewSamples = true;
                    addSamplePitch(newSamples[i].filename,newSamples[i].index);
                    try
                    {
                        Log.println(Subsystem.SOUND,null, "Loading: " + newSamples[i].filename, Log.P_VERBOSE);
                        loadSample(newSamples[i].filename, newSamples[i].index);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                
                Log.println(Subsystem.SOUND,null, "Closest Pitch: " + this.findClosestSampleIndex(pitchIndex), Log.P_VERBOSE);
            }
            
        }
        
    }
    
    public int getAlternativeSampleIndex(int pitchIndex, double timeStretch, double[] data) {
    		if (remapper != null)
        {
            return remapper.getAlternativeSampleIndex(pitchIndex, timeStretch, data);
        }
    		return pitchIndex;
    }
    

}
