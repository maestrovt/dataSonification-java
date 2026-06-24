package com.dataSonification.v2.sound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.dataSonification.v2.Const;
import com.dataSonification.v2.Core;
import com.dataSonification.v2.ID;
import com.dataSonification.v2.data.DataComponent;
import com.dataSonification.v2.data.DataInfo;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.ReturnCode;
import com.dataSonification.v2.util.Subsystem;
import com.dataSonification.v2.util.SyncBuffer;
import com.softsynth.jmsl.JMSL;
import com.softsynth.jmsl.JMSLMixerContainer;

/**
 * Acts as a dispatcher for all sonification jobs.
 * 
 * @author Kimo Johnson
 */
public class Gatekeeper {

	/**
	 * A queue of Sonifiables to process.
	 */
	private SyncBuffer<SonifiableGroup> sonifiables;

	/**
	 * Mixer for controlling pan and amplitude for the instruments.
	 */
	private JMSLMixerContainer mixer;

	/**
	 * Work thread for the Gatekeeper.
	 */
	private Thread gatekeeper_thread;
	
	/**
	 * Indicates that the Gatekeeper should stop.
	 */
	private volatile boolean shouldStop = false;
	
	/**
	 * Reference to the singleton Gatekeeper instance.
	 */
	private static Gatekeeper keeper = null;
	
	/**
	 * Lock to protect the singleton Gatekeeper instance.
	 */
	private static final Object classLock = Gatekeeper.class;
	
	/**
	 * A reference to the last sonifiable.
	 */
	private SonifiableGroup sg;
	
	/**
	 * A map from sonification id to instrument
	 */
	private Map<ID,ConfigurableInstrument> id_instrument;
	
	/**
	 * Instantiates and returns an instance of the singleton Gatekeeper.
	 * @return the instance of the Gatekeeper
	 */
	public static Gatekeeper instance() {
		synchronized (classLock) {
			if (keeper == null) {
                Log.println(Subsystem.SOUND, null, "Gatekeeper Reinitialized", Log.P_VERBOSE);
                
                keeper = new Gatekeeper();
			}
			return keeper;
		}
	}
		
	
	/**
	 * Private constructor to enforce singleton Gatekeeper.
	 */
	private Gatekeeper() {
		sonifiables = new SyncBuffer<SonifiableGroup>(Const.GATEKEEPER_MAX_SONIFIABLES);
		id_instrument = Collections.synchronizedMap(new HashMap<ID,ConfigurableInstrument>());
		mixer = new JMSLMixerContainer();
		mixer.start();
	}

	/**
	 * Stops the gatekeeper thread.
	 */
	public synchronized void stop() {
		if (gatekeeper_thread != null && gatekeeper_thread.isAlive()) {
		    shouldStop = true;
			gatekeeper_thread.interrupt();
			try {
				gatekeeper_thread.join();
			} catch (InterruptedException e) {}
			gatekeeper_thread = null;
		}
	}
	
	/**
	 * Starts the gatekeeper thread.
	 */
	public synchronized void start() {
		
		if (gatekeeper_thread != null && gatekeeper_thread.isAlive())
			return;
	
		shouldStop = false;
		gatekeeper_thread = new Thread(new Runnable() {
			public void run() {
				runGatekeeper();
			}
		});
		gatekeeper_thread.start();		
	}

	/**
	 * Main loop for the gatekeeper.
	 */
	private void runGatekeeper() {
		sonifiables.clear();
        Log.println(Subsystem.SOUND, null, "Gatekeeper running ", Log.P_VERBOSE);
        
		sg = null;
		try {
			while (!shouldStop) {
				try {
					sg = sonifiables.get();
					Log.println(Subsystem.SOUND, null, "Gatekeeper: got " + sg.id, Log.P_VERBOSE);
					ConfigurableInstrument ins = id_instrument.get(sg.id);
					if (ins != null ) {
					    sg.s.setInstrument(ins);
					    Core.instance().uiJob(sg.id, sg.action);
						sg.s.sonify(JMSL.now());
                        sg.dc.stamp(Subsystem.SOUND, ReturnCode.SONIFIED, null);
                        sg.dc.finished();
						Thread.sleep(Const.GATEKEEPER_PAUSE_TIME);
					}
					
				} catch (Exception e) {
                    e.printStackTrace();
                    shouldStop = true;
				}
			}
		}
		catch (Exception e) {}
		finally {
		    if (sg != null) {
				sg.s.finishAll();
			}
			Log.println(Subsystem.SOUND, null, "Gatekeeper: stopping.", Log.P_INFO);
		}
	}
	
	/**
	 * Plays a Sonifiable now.
	 * @param s the Sonifiable to play
	 * @param id the identifier of the sonification object
	 * @param action the action associated with this sonifiable
	 */
	public void playNow(Sonifiable s, ID id, DataInfo action) {
		try {
			ConfigurableInstrument ins = id_instrument.get(id);
            if(ins instanceof SampleInstrument && s instanceof SonifiableMusicShape)
            {
                ((SampleInstrument)ins).preload((SonifiableMusicShape)s);
            }
			if (ins != null) {
				s.setInstrument(ins);
				Core.instance().uiJob(id, action);
				s.sonify(JMSL.now());
				
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			s.finishAll();
		}
  
	}

	/**
	 * Stops JMSL mixer.
	 */
	public void shutdown() {
	    this.stop();
		mixer.stop();
		synchronized (classLock) {
		    keeper = null;
		}
	}
	
	
	/**
	 * Adds a sonifiable to the Gatekeeper's queue.
	 * @param s the Sonifiable
	 * @param id identifier for the sonification that created this Sonifiable
	 * @param action the DataAction associated with this Sonifiable
	 */
//	 package-private access
	void addSonifiable(Sonifiable s, ID id, DataInfo action, DataComponent dc) {
		ConfigurableInstrument ins = id_instrument.get(id);
        if(ins instanceof SampleInstrument && s instanceof SonifiableMusicShape)
        {
            ((SampleInstrument)ins).preload((SonifiableMusicShape)s);
        }
        if(ins instanceof SimpleSampleInstrument && s instanceof SonifiableMusicShape)
        {
        	((SimpleSampleInstrument)ins).preload((SonifiableMusicShape)s);
        }
        
        sonifiables.update(new SonifiableGroup(s, id, action, dc));
	}
	

	/**
	 * Removes a sonifiable from the Gatekeeper's queue.
	 * @param id identifier for the sonification that created the Sonifiable
	 */
//	 package-private access
	void removeSonifiables(ID id) {
		sonifiables.remove(new SonifiableGroup(null, id, null, null));
	}

	/**
	 * Sets the instrument for a Sonification.
	 * 
	 * @param id the id of the sonification or helper associated with this instrument
	 * @param ins the instrument to be added
	 * @param pan the amount of pan for the instrument
	 * @param amp the amplitude for the instrument
	 */
//	public access for the voice instrument in FacelessUI
	public void setInstrument(ID id, ConfigurableInstrument ins, double pan, double amp) {
		if (ins == null) {
	        throw new IllegalArgumentException("instrument is null " + id);
	    }

		if (pan < 0.0 || pan > 1.0) {
			pan = 0.5;
		}
	
		if (amp < 0.0 || amp > 1.0) {
		    amp = 0.9;
		}
		
		// Check to see if there is already an instrument for this id
		if (id_instrument.containsKey(id)) {
			Log.println(Subsystem.SOUND, null, "Gatekeeper: deleting existing instrument for id: " + id, Log.P_VERBOSE);
			removeInstrument(id);
		}

		id_instrument.put(id, ins);
		mixer.addInstrument(ins, pan, amp);
		Log.println(Subsystem.SOUND, null, "Gatekeeper: added instrument to mixer for id: " + id, Log.P_VERBOSE);
	}
	
	/**
	 * Removes an instrument from the Gatekeeper's mixer.
	 * @param id the id of the sonification associated with the instrument
	 */
	public void removeInstrument(ID id) {
	    sonifiables.remove(new SonifiableGroup(null, id, null, null));
	    ConfigurableInstrument ins = id_instrument.get(id);
        if (ins != null)
        {
            id_instrument.remove(id);
            try {
                ins.close(JMSL.now());
            } catch(InterruptedException e) {
                Log.println(Subsystem.SOUND,ReturnCode.GENERAL_WARNING,"Gatekeeper caught exception closing instrument for id: " + id);
            }
            mixer.removeInstrument(ins);
        }
	}
    
    public void removeAllInstrumentsForID(Integer db_id) {
        List<ID> to_remove = new ArrayList<ID>();
        for (Iterator<ID> it = id_instrument.keySet().iterator(); it.hasNext(); ) {
            ID id = it.next();
            
            if (db_id == id.getDB_ID()) {
                to_remove.add(id);
            }
        }
        
        for (ID id : to_remove) {
            removeInstrument(id);
        }
        
    }

	
	/**
	 * Container for a Sonifiable, the identifier, and the DataAction.
	 * @author Kimo Johnson
	 */
//	 This class is static because it does not depend on an instance of Gatekeeper
	private static class SonifiableGroup {
		public final Sonifiable s;
		public final ID id;
		public final DataInfo action;
		public final DataComponent dc;
        
		public SonifiableGroup(Sonifiable s, ID id, DataInfo action, DataComponent dc) {
			this.s = s;
			this.id = id;
			this.action = action;
			this.dc = dc;
		}
		
		public boolean equals(Object rhs) {
			if (!(rhs instanceof SonifiableGroup))
				return false;
			return id == ((SonifiableGroup)rhs).id;
		}
	}

}
