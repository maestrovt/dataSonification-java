/*
 * Analyzer.java
 *
 * Created on April 28, 2004, 12:48 PM
 */

package com.dataSonification.v2.sound;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dataSonification.v2.Config;
import com.dataSonification.v2.Core;
import com.dataSonification.v2.DBConfig;
import com.dataSonification.v2.DBManager;
import com.dataSonification.v2.ID;
import com.dataSonification.v2.data.DataComponent;
import com.dataSonification.v2.data.DataInfo;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.ReturnCode;
import com.dataSonification.v2.util.Subsystem;
import com.dataSonification.v2.util.SyncBuffer;
import com.softsynth.jmsl.JMSL;

/**
 * The dataSonification Sonification class. Analyzers and Arrangers plug into
 * this object as components.
 *
 * @author Kimo Johnson
 */
public class Sonification implements Config.ChangeListener
{
    
    /**
     * The work thread for the sonification.
     * @see runSonification
     */
    private Thread sonification_thread = null;
    
    /**
     * Indicates that the sonification thread should stop.
     */
    private volatile boolean shouldStop = false;
    
    /**
     * The analyzer component for the sonification.
     */
    private Analyzer analyzer;
    
    /**
     * The arranger component for the sonification.
     */
    private Arranger arranger;
    
    /**
     * The trainer component for the sonification.
     */
    private Trainer trainer;
    
    /**
     * Shared cache between Analyzer, Trainer, and Arranger.
     */
    private Map<Key,Object> cache;
    
    /**
     * The configuration for this sonification, linked at construction.
     */
    private Config config;
    
    /**
     * Queue of data to be analyzed.
     */
    private final SyncBuffer<DataComponent> data;
    
    /**
     * Set of keys that each DataComponent must have
     */
    private Set<Key> dataKeys;
    
    /**
     * The identifier for this sonification.
     */
    private ID id;
    
    /**
     * The last DataAction.
     */
    private DataInfo last_info;
    
    /**
     * The last Sonifiable.
     */
    private Sonifiable s;
    
    private volatile boolean initialized;
    
    public Sonification()
    {
        data = new SyncBuffer<DataComponent>();
        cache = Collections.synchronizedMap(new EnumMap<Key,Object>(Key.class));
        dataKeys = EnumSet.noneOf(Key.class);
        
        analyzer = null;
        arranger = null;
        trainer = null;
        initialized = false;
    }
    
    
    /**
     * Sets the config object for this sonification class.
     * @param config the config object
     * @throws IllegalStateException if there are problems with the config
     */
    public void setConfig(ID id, Config config)
    {
        
        /*
         * Core ensures that the config object has the S_ID key and that
         * the key is unique.
         */
        this.config = config;
        this.id = id;
        Log.println(Subsystem.SOUND, ReturnCode.NO_CODE, "Sonification: setting config. " + this + "(" + id + ")", Log.P_VERBOSE);
        
        // This should only be done once, or some sort of shutdown
        // procedure will be necessary
        
        try
        {
            Class<?> analyzer_class = (Class) config.getField(Key.ANALYZER);
            Class<?> arranger_class = (Class) config.getField(Key.ARRANGER);
            analyzer = (Analyzer) analyzer_class.newInstance();
            arranger = (Arranger) arranger_class.newInstance();
        }
        catch (Exception e)
        {
            Log.println(Subsystem.SOUND, ReturnCode.GENERAL_WARNING, "Sonification: using default analyzer and arranger " + this + "(" + id + ")", Log.P_WARNING);
            analyzer = new DefaultAnalyzer();
            arranger = new DefaultArranger();
        }
        
        try
        {
            Class<?> trainer_class = (Class) config.getField(Key.TRAINER);
            trainer = (Trainer) trainer_class.newInstance();
        }
        catch (Exception e)
        {
            Log.println(Subsystem.SOUND, ReturnCode.GENERAL_WARNING, "Sonification: using default trainer " + this + "(" + id + ")", Log.P_WARNING);
            trainer = new DefaultTrainer();
        }
        
        analyzer.setCache(cache);
        arranger.setCache(cache);
        trainer.setCache(cache);
        
        try
        {
            validateConfig();
        }
        catch (NoSuchFieldException e)
        {
            Log.println(Subsystem.SOUND, ReturnCode.SOUND_COMPONENT_MISMATCH, "Sonification: analyzer, arranger, and/or trainer mismatch: " + e.toString() + " " + this + "(" + id + ")", Log.P_ERROR);
            initialized = false;
            analyzer = null;
            arranger = null;
            trainer = null;
            return;
        }
        
        if((Boolean)config.getField(Key.ENABLED))
        {
            initInstrument();
          
        }
        initialized = true;
        
    }
    
    public void initInstrument()
    {
        try     
        {
            Class<?> ins_class = (Class) config.getField(Key.INSTRUMENT);
            loadInstrument(ins_class);
        }
        catch (IllegalStateException e)
        {
            Gatekeeper.instance().setInstrument(id, new DefaultInstrument(), 0.5, 0.9);
        }   
    }
    
    
    /**
     * Validates the specified Analyzer, Arranger, and config for compatibility.
     * @throws NoSuchFieldException if some necessary key is not found
     */
    private void validateConfig() throws NoSuchFieldException
    {
        goodReadList(analyzer.readList(), true);
        goodReadList(trainer.readList(), false);
        goodReadList(arranger.readList(), false);
    }
    
    /**
     * Determine if the keys in a SonificationComponent's read list can be
     * found.
     * @param keyList
     * @param case_analyzer special case for processing an analyzer
     * @throws NoSuchFieldException
     */
    private void goodReadList(List<Key> keyList, boolean case_analyzer) throws NoSuchFieldException
    {
        for (Key key : keyList)
        {
            boolean goodKey = false;
            switch(key.getSource())
            {
                case CONFIG:
                    goodKey = config.containsKey(key);
                    break;
                case COMPONENT:
                    if (case_analyzer)
                        goodKey = analyzer.keyInDefaults(key);
                    else
                        goodKey = analyzer.keyInWrites(key);
                    break;
                case DATA:
                    dataKeys.add(key);
                    goodKey = true;
            }
            if (!goodKey)
            {
                throw new NoSuchFieldException(key.toString());
            }
        }
    }
    
    /**
     * Initializes the shared cache from the config.
     */
    private void initCache()
    {
        
        cache.clear();
        
        for (Key key : analyzer.readList())
        {
            if (key.getSource() == Key.Source.CONFIG)
                cache.put(key, config.getField(key));
        }
        
        for (Key key : arranger.readList())
        {
            if (key.getSource() == Key.Source.CONFIG)
                cache.put(key, config.getField(key));
        }
        
        for (Key key : trainer.readList())
        {
            if (key.getSource() == Key.Source.CONFIG)
                cache.put(key, config.getField(key));
        }
        
        analyzer.initCache();
        arranger.initCache();
        trainer.initCache();
    }
    
    
    /**
     * Loads the instrument and adds it to the Gatekeeper's mixer.
     */
    private void loadInstrument(Class<?> ins_class)
    {
        
        ConfigurableInstrument instrument;
        try
        {
            instrument = (ConfigurableInstrument) ins_class.newInstance();
            Log.println(Subsystem.SOUND,null,"Setting Instrument to: " + instrument.getClass().getName(), Log.P_VERBOSE);
            
            DBManager dbm = Core.instance().getInstrumentDBM();
            Integer inst_id = (Integer) config.getField(Key.INST_ID);
            DBConfig inst_config = new DBConfig(dbm, "AllInstruments", Key.INST_ID, inst_id);
            inst_config.merge(config);
            
            instrument.setConfig(inst_config);
            instrument.open(JMSL.now());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new IllegalStateException(e.toString());
        }
        
        double pan = (Double) config.getField(Key.PAN);
        double amp = (Double) config.getField(Key.AMPLITUDE);
        
        Gatekeeper.instance().setInstrument(id, instrument, pan, amp);
        
        if(arranger instanceof ContinuousArranger)
            ((ContinuousArranger)arranger).setInstrument(instrument);
    }
    
    
    /**
     * Starts the data processing thread for this sonification.
     */
    public void start()
    {
        
        if (!initialized)
        {
            return;
        }
        
        initCache();
        data.clear();
        s = null;
        Log.println(Subsystem.SOUND, ReturnCode.NO_CODE, "Start called for: " + this + "(" + id + ")");
        
        shouldStop = false;
        sonification_thread = new Thread(new Runnable()
        {
            public void run()
            {
                runSonification();
            }
        });
        sonification_thread.start();
    }
    
    /**
     * The work loop for this sonification.  DataEvents are analyzed by
     * the Analyzer creating DataActions.  If the event is to be sonified,
     * the Arranger creates a Sonifiable for the event.
     */
    private void runSonification()
    {
        Log.println(Subsystem.SOUND, ReturnCode.NO_CODE, "Sonification Analyzer now running " + this + "(" + id + ")", Log.P_VERBOSE);
        
        try
        {
//			Main Loop
            while (!shouldStop)
            {
                DataComponent currentData = data.get();
                
                if (currentData == null)
                    break;
                
                //We shouldn't completely crash if the analyzer throws an exception...
                boolean to_sonify = false;
                try
                {
                    to_sonify = analyzer.analyze(currentData);
                }
                catch(Exception e)
                {
                    Log.println(Subsystem.SOUND, ReturnCode.GENERAL_WARNING, "Got Exception when Analyzing: " + e.getMessage(), Log.P_WARNING);
                    to_sonify = false;
                }
                
                DataInfo info = DataInfo.IGNORED;
                if (to_sonify)
                {
                    info = trainer.evaluate();
                    s = arranger.arrange();
                    if(!s.isBlank())
                    {
                        Gatekeeper.instance().addSonifiable(s, id, info, currentData);
                        last_info = info;
                    }
                    else
                    {
                        currentData.stamp(Subsystem.SOUND, ReturnCode.NOT_SONIFIED, "NOT_SONIFIED");
                        currentData.finished();
                    }
                    
                }
                else
                {
                    Core.instance().uiJob(id, info);
                    currentData.stamp(Subsystem.SOUND, ReturnCode.NOT_SONIFIED, "NOT_SONIFIED");
                    currentData.finished();
                }
                
            }
            
        }
        catch (InterruptedException e)
        {
            Log.println(Subsystem.SOUND, ReturnCode.NO_CODE, "Sonification: runSonification interrupted " + this + "(" + id + ")", Log.P_INFO);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.println(Subsystem.SOUND, ReturnCode.NO_CODE, "Sonification: runSonification caught: " + e.toString() + " " + this + "(" + id + ")", Log.P_INFO);
        }
        finally
        {
            Log.println(Subsystem.SOUND, ReturnCode.NO_CODE, "Sonification: reached finally clause", Log.P_INFO);
        }
        
        Log.println(Subsystem.SOUND, ReturnCode.NO_CODE, "Sonification Analyzer stopped " + this + "(" + id + ")", Log.P_VERBOSE);
    }
    
    
    /**
     * Adds a DataEvent to the queue of events to be processed.
     * @param event an event to be processed
     */
    public void update(DataComponent event)
    {
        // Verify that the DataComponent has all the proper keys
        Set<Key> eventKeys = event.getFields().keySet();
        for (Key key : dataKeys)
        {
            if (!eventKeys.contains(key))
            {
                event.stamp(Subsystem.SOUND, ReturnCode.DATA_FIELD_MISSING, "sonification cannot find required field: " + key.toString());
                event.finished();
                Log.println(Subsystem.SOUND, ReturnCode.DATA_FIELD_MISSING, "Stamping Component: Sonification: cannot find " + key.toString() + " in DataComponent. " + this + "(" + id + ")", Log.P_INFO);
                return;
            }
        }
        data.put(event);
    }
    
    public boolean isReady()
    {
        return initialized;
    }
    
    /**
     * Stops the data processing thread for this sonification.
     */
    public void stop()
    {
        Log.println(Subsystem.SOUND, ReturnCode.NO_CODE, "Sonification: stop called for s_id: " + this + "(" + id + ")", Log.P_INFO);
        if (sonification_thread != null && sonification_thread.isAlive())
        {
            // Flush the data buffer
            data.clear();
            s = null;
            
            // Remove any scheduled sonifiables from the gatekeeper
            Gatekeeper.instance().removeSonifiables(id);
            
            shouldStop = true;
            sonification_thread.interrupt();
            try
            {
                sonification_thread.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            sonification_thread = null;
        }
    }
    
    /**
     * Causes sonification to play last event sonified.
     */
    public void replay()
    {
        if (s != null)
        {
            Log.println(Subsystem.SOUND, ReturnCode.NO_CODE, "Sonification: replay called for " + this + "(" + id + ")", Log.P_VERBOSE);
            Gatekeeper.instance().playNow(s, id, last_info);
        }
        else
        {
            Log.println(Subsystem.SOUND, ReturnCode.NO_CODE, "Sonification: replay called for " + this + "(" + id + ")" + ", but no last event", Log.P_VERBOSE);
        }
    }
    
    
    /*
     * Javadoc in interface.
     */
    public void configChanged(Key key)
    {
        // This isn't correct.  Action may be different depending on what field changed.
        Object value = config.getField(key);
        
        Map<Key,Object> m = new EnumMap<Key,Object>(Key.class);
        m.put(key,value);
        configChanged(m);
    }
    
    
    /*
     * Javadoc in interface.  Error checking should be done in SocketUI.
     */
    public void configChanged(Map<Key,Object> m)
    {      
        // redesign...special cases should be handled in a different way
        if (m.containsKey(Key.SIGNIFICANT_MOVE)) {
            double old_sig_move = (Double) cache.get(Key.SIGNIFICANT_MOVE);
            int last_increment = (Integer) cache.get(Key.LAST_INCREMENT);
            double old_position = last_increment * old_sig_move;
            double new_sig_move = (Double) m.get(Key.SIGNIFICANT_MOVE);
            int new_increment = (int)Math.round(old_position/new_sig_move);
            
            cache.put(Key.LAST_INCREMENT, new_increment);
        }
        
        for (Key key : m.keySet())
        {
            Object value = m.get(key);
            updateConfig(key, value);
            Log.println(Subsystem.SOUND, ReturnCode.NO_CODE, this + "(" + id + ")" + " configChanged " + key + " = " + value);
        }
        
        // Handle special cases
        if (m.containsKey(Key.INST_ID))
        {
            Class<?> ins_class = (Class) config.getField(Key.INSTRUMENT);
            try
            {
                loadInstrument((Class<?>) ins_class);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        

    }
    
    /*
     * Called by configChanged.
     */
    private void updateConfig(Key key, Object value)
    {
        if (key.getSource() != Key.Source.CONFIG)
        {
            Log.println(Subsystem.SOUND, ReturnCode.NO_CODE, this + "(" + id + ")" + "cannot update non-config key: " + key, Log.P_WARNING);
            return;
        }
        
        // If the key is a config key in the cache, update it directly
        if (cache.containsKey(key))
        {
            cache.put(key, value);
        }
        
        config.setField(key, value);
    }
    
    public String toString()
    {
        return (String) config.getField(Key.TICKER);
    }
    
    
}
