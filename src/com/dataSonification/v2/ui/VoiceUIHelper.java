package com.dataSonification.v2.ui;

import java.util.Map;

import com.dataSonification.v2.Config;
import com.dataSonification.v2.ID;
import com.dataSonification.v2.data.DataInfo;
import com.dataSonification.v2.sound.Gatekeeper;
import com.dataSonification.v2.sound.VoiceInstrument;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.Subsystem;
import com.softsynth.jmsl.JMSL;
import com.softsynth.jmsl.MusicShape;


public class VoiceUIHelper implements UIHelper {

    private VoiceInstrument voice = null;
    private Config config;
    private Map<String,ID> event_map;
    private double voice_delay;
    private Integer id;
    
    public VoiceUIHelper() {
        id = Integer.valueOf(-1);
    }
    
    /*
     * Javadoc in interface.
     */
    public void help(ID id, DataInfo info) {
        
        if (null == voice) {
            return;
        }
        
        Log.println(Subsystem.UI, null, "VoiceUIHelper: " + "id: " + id + ", in map: " + event_map.containsValue(id) + ", type: " + info.type);
        
        ID sample = null;
		switch(info.type) {
		case CROSS:
			String crossed = info.getInfo().toUpperCase();
			sample = event_map.get(crossed);
			break;
		default:
			sample = event_map.get(info.type);
		}
        
      
//        if (sample != null && event_map.containsValue(id)) {
//            double duration = voice.getSampleDuration(id);
//            
//            MusicShape ms = new MusicShape(4);
//            ms.add(duration, id.intValue(), 0.3, duration);
//            // the sample is located at the position stored in s_id
//            ms.add(2.2, sample.getID(), 0.3, 2.2);
//            ms.setInstrument(voice);
//            ms.launch(JMSL.now()+voice_delay);
//        }
        
        
    }

    /*
     * Javadoc in interface.
     */
    public void setConfig(Config config) {
        this.config = config;
        loadVoice();
    }
    
    private void loadVoice() {
        try {
            voice = new VoiceInstrument();
            voice.setConfig(config);
            event_map = voice.getMap();
            
            for (String key : event_map.keySet()) {
                Log.println(Subsystem.UI, null, "VoiceUIHelper: key: " + key + ", value: " + event_map.get(key), Log.P_VERBOSE);
            }
            
            voice_delay = (Double)config.getField(Key.VOICE_DELAY);
            double pan = (Double)config.getField(Key.PAN);
            double loudness = (Double)config.getField(Key.LOUDNESS);
            
            voice.open(JMSL.now());
//            Gatekeeper.instance().setInstrument(id, voice, pan, loudness);
            
        } catch (InterruptedException e) {
            throw new IllegalStateException(e.toString());
        }

    }
    
}