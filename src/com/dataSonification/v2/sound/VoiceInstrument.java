package com.dataSonification.v2.sound;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.dataSonification.v2.Config;
import com.dataSonification.v2.Const;
import com.dataSonification.v2.Core;
import com.dataSonification.v2.ID;
import com.dataSonification.v2.data.InfoType;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.softsynth.jmsl.DefaultDimensionNameSpace;
import com.softsynth.jmsl.DimensionNameSpace;
import com.softsynth.jmsl.jsyn.SimpleSamplePlayingInstrument;
import com.dataSonification.v2.util.Subsystem;
import com.dataSonification.v2.data.SystemObject;
public class VoiceInstrument extends SimpleSamplePlayingInstrument implements ConfigurableInstrument {

    /**
     * The config object for this instrument.
     */
    private Config config = null;
    private Map<String,ID> ticker_id;
    private Map<ID,Double> sample_duration;
    
	public VoiceInstrument() {
		this("");
	}
	
	public VoiceInstrument(String directory) {
	    super(directory);
	    ticker_id = new HashMap<String,ID>();
	    sample_duration = new HashMap<ID,Double>();
	}

	public void buildFromAttributes() {
	    if (config == null)
	        throw new IllegalStateException("config is not set.");

        String sample_dir = com.softsynth.jmsl.view.SampleFinderDialog.getDirectory();
        
        // Build a temporary map of ticker->id
        Map<String,ID> temp = new HashMap<String,ID>();
        Map<ID,Config> configs = Core.instance().getSonificationConfigs();
        
        for (ID id : configs.keySet() ) {
            Config c = configs.get(id);
            String ticker = (String) c.getField(Key.TICKER);
            temp.put(ticker.toUpperCase(), id);
            Log.println(Subsystem.SOUND, null, "VoiceInstrument: ticker: " + ticker + ", id: " + id.toString(), Log.P_VERBOSE);
        }

        int max_id = 0;
        // Add ticker samples
        String file_type = (String) config.getField(Key.FILE_TYPE);
        String ticker_dir = (String) config.getField(Key.TICKER_DIR);
        File f = new File(sample_dir + ticker_dir);
        if (f.exists() && f.isDirectory()) {
            String[] files = f.list();
            for (int i = 0; i < files.length; i++) {
                if (files[i].endsWith(file_type)) {
                    String ticker = files[i].substring(0, files[i].length() - file_type.length()).toUpperCase();
                    
                    // Look up ticker in the map
                    ID id = (ID)temp.get(ticker);
                    if (null == id) {
                        // redesign...should we do something more intelligent here?
                        continue;
                        // id = new Integer(++max_id);
                    }
                    File sample = new File(sample_dir + ticker_dir + files[i]);
                    if (sample.exists()) {
	                    long nBytes = sample.length();
	                    // Assume mono wav files, 44100 Hz 16-bit
	                    double nSeconds = (nBytes - 44)/(44100*2.0);
	                    Log.println(Subsystem.SOUND, null, "VoiceInstrument: adding: " + ticker_dir + files[i] + ", length: " + nSeconds + " seconds.", Log.P_ALL);
	                    
                        int position = id.getID();
                        if (position > max_id)
                            max_id = position;
                        
	                    addSamplePitch(ticker_dir + files[i], position);
	                    ticker_id.put(ticker.toUpperCase(), id);
	                    sample_duration.put(id, nSeconds);
                    }
                }
            }
            
        }
       
        // set the offset between events and tickers
        int event_offset = max_id + Const.MIN_EVENT_OFFSET;
        String event_dir = (String)config.getField(Key.EVENT_DIR);

        // Add event samples
        f = new File(sample_dir + event_dir);

        if (f.exists() && f.isDirectory()) {
            String[] files = f.list();
            for (int i = 0; i < files.length; i++) {
                if (files[i].endsWith(file_type)) {
                    String event = files[i].substring(0, files[i].length() - file_type.length());
                    
                    // Look up ticker in the InfoType
                    File sample = new File(sample_dir + event_dir + files[i]);
                    
                    if (InfoType.isType(event) && sample.exists()) {
                        InfoType type = InfoType.typeForString(event);
                        
                        long nBytes = sample.length();
                        // Assume mono wav files, 44100 Hz 16-bit
                        double nSeconds = (nBytes - 44)/(44100*2.0);
                        
                        Log.println(Subsystem.SOUND, null, "VoiceInstrument: adding: " + event_dir + files[i] + ", length: " + nSeconds + " seconds.", Log.P_ALL);
                        addSamplePitch(event_dir + files[i], event_offset);
                        // redesign...this won't work
                        ID temp_id = new ID(Const.VOICE_EVENT_CORE_ID,event_offset++,ID.type.Sonification);
                        ticker_id.put(type.toString(), temp_id);
                        sample_duration.put(temp_id, nSeconds);
                    }
                }
            }
            
        }

		super.buildFromAttributes();
	}
	
	public Map<String,ID> getMap() {
	    return ticker_id;
	}
	
	public DimensionNameSpace getDimensionNameSpace() {
	    return DefaultDimensionNameSpace.instance();
	}
	
	public double getSampleDuration(Integer id) {
	    return sample_duration.get(id);
	}
	
    /*
     * Javadoc in interface.
     */
    public void setConfig(Config c) {
        this.config = c;
        buildFromAttributes();
    }

}
