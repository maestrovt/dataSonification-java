package com.dataSonification.v2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import com.dataSonification.v2.data.ActionType;
import com.dataSonification.v2.data.DataInfo;
import com.dataSonification.v2.data.DataSource;
import com.dataSonification.v2.data.MessageComponent;
import com.dataSonification.v2.data.StatusComponent;
import com.dataSonification.v2.data.SystemObject;
import com.dataSonification.v2.sound.Gatekeeper;
import com.dataSonification.v2.sound.Sonification;
import com.dataSonification.v2.ui.UI;
import com.dataSonification.v2.ui.UIHelper;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.ReturnCode;
import com.dataSonification.v2.util.Subsystem;
import com.dataSonification.v2.util.SyncBuffer;
import com.dataSonification.v2.util.TableToClass;
import com.softsynth.jmsl.DefaultMusicClock;
import com.softsynth.jmsl.EventScheduler;
import com.softsynth.jmsl.JMSL;
import com.softsynth.jmsl.jsyn.JSynMusicDevice;
import com.softsynth.jmsl.midi.MidiIO_JavaSound;

/**
 * Main engine for the dataSonification V2 software.
 *
 * @author Kimo Johnson
 */
public class Core {
    /**
     * Reference to the singleton instance of Core.
     */
    private static Core core = null;

    /**
     * The current UI.
     */
    // private UI ui = null;
    /**
     * The current UI Helper
     */
    // private UIHelper helper = null;
    /**
     * Synchronized storage for error reports.
     */
    private SyncBuffer<Object> errorReports;

    /**
     * Stores sonification objects by ID.
     */
    private Map<Integer, Map<ID, SonificationGroup>> sonifications;

    /**
     * Stores sonification config objects by ID.
     */
    private Map<ID, Config> sonificationConfigs;

    /**
     * Stores data source objects by ID.
     */
    private Map<ID, DataSource> dataSources;

    /**
     * Stores data source config objects by ID.
     */
    private Map<ID, Config> dataSourceConfigs;

    /**
     * The ui's and ui helpers by core_id
     */
    private Map<Integer, UI> uis;

    private Map<Integer, UIHelper> uihelpers;

    /**
     * The config object for the Core.
     */
    // private Config coreConfig = null;
    /**
     * The main thread for a running Core.
     */
    private Thread core_thread = null;

    /**
     * Indicates that the core should stop.
     */
    private volatile boolean shouldStop = false;

    private volatile boolean shouldExit = false;

    /**
     * Stores the running status of different dbs.
     */
    private Map<Integer, DBGroup> active_dbs;

    private Stack<Integer> unused_ids;

    /**
     * A lock to use for instantiating the Core.
     */
    private static final Object classLock = Core.class;

    private DBManager instrument_dbm;
    
    private boolean use_jsyn;
    private boolean use_midi;
    
    
    private long start_time;
    
    
    /**
     * Creates and returns the singleton instance of Core.
     * 
     * @return the singleton instance of Core
     */
    public static Core instance() {
        synchronized (classLock) {
            if (core == null) {
                core = new Core();
            }
            return core;
        }
    }

    /**
     * Initializes the Core by reading config files or config DB.
     */
    /* private because Core is a singleton */
    private Core() {
        // Instantiate tables
        sonifications = new HashMap<Integer, Map<ID, SonificationGroup>>();
        sonificationConfigs = new HashMap<ID, Config>();
        dataSources = new HashMap<ID, DataSource>();
        dataSourceConfigs = new HashMap<ID, Config>();

        uis = new HashMap<Integer,UI>();
        // uihelpers = new HashMap<Integer,UIHelper>();

        // errorReports = new SyncBuffer<Object>();

        active_dbs = new HashMap<Integer, DBGroup>();
        unused_ids = new Stack<Integer>();
        unused_ids.push(1);

        use_jsyn = false;
        use_midi = false;
        
        JMSL.midi = MidiIO_JavaSound.instance();
        JMSL.midi.setEditEnabled(false);
        JMSL.setIsApplet(false);
        JMSL.scheduler = new EventScheduler();
        JMSL.scheduler.start();
        
        start_time = System.currentTimeMillis();
    }

    public int init(Map<Key, String> args) {

        int db_id = 0;

        try {
            // Set the log level
            Const.LOG_LEVEL = Log.LogCode.forCode(getLogLevel(args));

            // Get config object
            Config config = getConfig(args);

            // Get ID of active DB or new DB
            db_id = getDBManagerID(config);

            // All this JMSL stuff here is strange.
            // Set the sample directory
            String sample_dir = (String) config.getField(Key.SAMPLE_DIR);

            if (!sample_dir.endsWith(File.separator))
                sample_dir = sample_dir + File.separator;

            com.softsynth.jmsl.view.SampleFinderDialog.setDirectory(sample_dir);
            Log.println(Subsystem.CORE, null, "Sample Dir set: " + sample_dir,
                    Log.P_VERBOSE);

            if (config.getField(Key.USE_MIDI).equals(Boolean.TRUE) && !use_midi) {
                JMSL.midi.open();
                use_midi = true;
                Log.println(Subsystem.CORE, null, "opened Midi", Log.P_VERBOSE);
            }

            if (config.getField(Key.USE_JSYN).equals(Boolean.TRUE) && !use_jsyn) {
                JSynMusicDevice.instance().open();
                Log.println(Subsystem.CORE, null, "Core: opened Jsyn", Log.P_VERBOSE);
                use_jsyn = true;
            } else {
                // The call above to open on JSynMusicDevice sets JMSL.clock
                // In the case when we restart and don't use JSyn, we need to
                // set the clock manually.
                JMSL.clock = new DefaultMusicClock();
            }
            
            initInstrumentDBM(config);
            initFromConfigDB(db_id);
            
            // Restart the core if we are reinitializing it and it was started
            DBGroup dbg = active_dbs.get(db_id);
            if (dbg.running) {
                dbg.running = false;
                startID(db_id);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }

        return db_id;
    }

    /**
     * Returns a file at the specified path as an InputStream. The path must be
     * relative to Core.class.
     * 
     * @param s
     *            the file path relative to Core.class
     * @return the file as an InputStream
     */
    public static InputStream getInputStream(String s) {
        return Core.class.getResourceAsStream(s);
    }

    /**
     * Get the log level specified in the input args
     * 
     * @param args
     *            input arguments
     * @return the log level
     */
    private int getLogLevel(Map<Key, String> args) {
        int log_level = Const.LOG_LEVEL.getCode();
        try {
            log_level = Integer.parseInt(args.get(Key.LOG_LEVEL));
        } catch (Exception e) {
        }
        return log_level;
    }

    /**
     * Returns a Config object for the config file specified in the path String.
     * 
     * @param s
     *            the path to the config file
     * @return a Config object containing the data in the file
     * @throws IOException
     *             if there is a problem reading the config file
     */
    private Config getConfig(Map<Key, String> args) throws IOException {
        // Initialize the coreConfig object by reading the settings file
        String core_name = args.get(Key.CORE_CONFIG_NAME);
        if (null == core_name) {
            core_name = Const.CORE_CONFIG_NAME;
        }

        InputStream is = getInputStream(core_name);
        Config config;

        if (is == null)
            config = new FileConfig();
        else
            config = new FileConfig(new BufferedReader(new InputStreamReader(is)));

        // Merge the command line args into config
        for (Key key : args.keySet()) {
            Object value = args.get(key);
            Log.println(Subsystem.CORE, null, "setting " + key + " to " + value, Log.P_VERBOSE);
            config.setField(key, value);
        }
        
        return config;
    }

    private int getDBManagerID(Config config) {

        int db_id = 0;
        boolean running_status = false;
        DBManager dbm = new DBManager(config);

        for (int id : active_dbs.keySet()) {
            DBGroup dbg = active_dbs.get(id);
            if (dbm.equals(dbg.manager)) {
                db_id = id;
                running_status = dbg.running;
                break;
            }
        }
        
        
        if (db_id > 0) {
            Log.println(Subsystem.CORE,ReturnCode.GENERAL_WARNING,"Core reinitializing db_id: " + db_id);            
            exitID(db_id);
            
        } else {
            // Get unused ID
            db_id = unused_ids.pop();
            if (unused_ids.isEmpty()) {
                unused_ids.push(db_id + 1);
            }
        }

        active_dbs.put(db_id, new DBGroup(dbm, running_status));
        return db_id;
    }

    /**
     * Initializes Core from a config database.
     * 
     * @throws IllegalStateException
     *             if Core cannot be initialized from the database
     */
    private void initFromConfigDB(Integer db_id) {
        try {
            String sql = "SELECT d_id,type from DataSource";
            DBManager dbm = active_dbs.get(db_id).manager;

            dbm.initialize();
            DBResult result = dbm.queryDB(sql);

            while (result.rs.next()) {
                String table = result.rs.getString(2);
                Integer d_id = Integer.valueOf(((Number) result.rs.getObject(1)).intValue());

                Config config = new DBConfig(dbm, table, Key.D_ID, d_id);

                DataSource ds = (DataSource) Class.forName(TableToClass.map(table)).newInstance();
                ds.setConfig(config);

                ID ds_id = new ID(db_id, d_id, ID.type.DataSource);
                dataSources.put(ds_id, ds);
                dataSourceConfigs.put(ds_id, config);
            }

            result.st.close();

            Map<ID,SonificationGroup> m = new HashMap<ID,SonificationGroup>();
            
            // Initialize Sonifications
            sql = "SELECT enabled,s_id,d_id FROM Sonification";
            result = dbm.queryDB(sql);
            while (result.rs.next()) {
                int s_id = ((Number) result.rs.getObject(2)).intValue();

                int d_id = result.rs.getInt(3);
                DataSource ds = dataSources.get(new ID(db_id, d_id, ID.type.DataSource));

                boolean enabled = result.rs.getBoolean(1);

                if (ds != null) {
                    Sonification s = new Sonification();
                    Config config = new DBConfig(dbm, "Sonification", Key.S_ID, s_id);
                    ID id = new ID(db_id, s_id, ID.type.Sonification);
                    
                    s.setConfig(id, config);
                    
                    m.put(id, new SonificationGroup(s, enabled));
                    sonificationConfigs.put(id, config);

                    ds.addDataEntity(config, id, s);
                }
            }

            sonifications.put(db_id, m);
            instrument_dbm.shutdown();
            dbm.shutdown();

        } catch (SQLException e) {
            throw new IllegalStateException(e.toString());
        } catch (InstantiationException e) {
            throw new IllegalStateException(e.toString());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e.toString());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.toString());
        }
    }
    
    private void initInstrumentDBM(Config config) {
        String driver = (String) config.getField(Key.INST_DB_DRIVER);
        String name = (String) config.getField(Key.INST_DB_NAME);
        String user = (String) config.getField(Key.INST_DB_USER);
        String password = (String) config.getField(Key.INST_DB_PASSWORD);
        
        if (user == null) {
            user = "";
        }
        
        if (password == null) {
            password = "";
        }
        
        if (driver != null && name != null && user != null && password != null) {
            Config c = new FileConfig();
            c.setField(Key.DB_DRIVER, driver);
            c.setField(Key.DB_NAME, name);
            c.setField(Key.DB_USER, user);
            c.setField(Key.DB_PASSWORD, password);
            
            if (instrument_dbm != null) {    
                instrument_dbm.shutdown();
            }
            
            instrument_dbm = new DBManager(c);
            instrument_dbm.initialize();
        }
    }
    
    public DBManager getInstrumentDBM() {
        return instrument_dbm;
    }

    /**
     * Starts the core thread.
     */
    public void start() {
        synchronized (classLock) {
            if (core_thread != null && core_thread.isAlive()) {
                Log.println(Subsystem.CORE, null, "start thread called, but Core already running.", Log.P_VERBOSE);
                return;
            }

            shouldStop = false;

            core_thread = new Thread(new Runnable() {
                public void run() {
                    runCore();
                }
            });
        }
        core_thread.start();
    }

    public synchronized boolean startID(int db_id) 
    {
        DBGroup dbg = active_dbs.get(db_id);
        Map<ID,SonificationGroup> m = sonifications.get(db_id);
        
        if (dbg == null || m == null) 
        {
            Log.println(Subsystem.CORE, null, "Start called for invalid db_id: " + db_id, Log.P_VERBOSE);
            return false;
        }
        
        Gatekeeper.instance().start();
        
        if (!dbg.running && dbg.manager.initialize()) 
        {
            for (SonificationGroup sg : m.values()) 
            {
                if (sg != null && sg.enabled)
                    sg.s.start();
            }

            for (ID id : dataSources.keySet()) 
            {
                DataSource ds = dataSources.get(id);
                if (ds != null && id.getDB_ID() == db_id)
                    ds.connect();
            }

            dbg.manager.shutdown();
            dbg.running = true;
            active_dbs.put(db_id, dbg);
        }
        return true;
    }

    /**
     * Stops the core core thread.
     */
    public void stop() {
        if (core_thread != null && core_thread.isAlive()) {
            synchronized (classLock) {
                shouldStop = true;
                core_thread.interrupt();
            }
            try {
                core_thread.join();
                Log.println(Subsystem.CORE, null, "Core Thread Finished", Log.P_VERBOSE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Log.println(Subsystem.CORE, null,
                    "stop called, but Core already stopped.", Log.P_VERBOSE);
        }

    }

    public synchronized boolean stopID(int db_id) 
    {
        DBGroup dbg = active_dbs.get(db_id);
        Map<ID,SonificationGroup> m = sonifications.get(db_id);
        
        if (dbg == null || m == null) 
        {
            Log.println(Subsystem.CORE, null, "Stop called for invalid db_id: " + db_id, Log.P_VERBOSE);
            return false;
        }

        if (dbg.running)
        {
            for (SonificationGroup sg : m.values()) 
            {
                if (sg != null)
                    sg.s.stop();
            }
    
            for (ID id : dataSources.keySet()) 
            {
                DataSource ds = dataSources.get(id);
                if (ds != null && id.getDB_ID() == db_id)
                    ds.disconnect();
            }
    
            dbg.running = false;
            active_dbs.put(db_id, dbg);
        }
        return true;
    }

    /**
     * Shutdown everything and quit.
     */
    public void exit() {
        Log.println(Subsystem.CORE, null, "exiting.", Log.P_INFO);
        stopEverything();

        Gatekeeper.instance().shutdown();
        
        if (use_jsyn)
            com.softsynth.jmsl.jsyn.SampleLoader.clearSamples();
        
        JMSL.scheduler.stop();
        JMSL.midi.closeDevices();
        JMSL.midi.close();
        // Null out static fields
        JMSL.scheduler = null;
        JMSL.midi = null;
        JMSL.clock = null;
        JMSL.closeMusicDevices();
    
        active_dbs.clear();
        
        synchronized(classLock) {
            core = null;
        }
    }

    public void exitID(int db_id) {
        
        DBGroup dbg = active_dbs.get(db_id);
        
        if (dbg == null) {
            Log.println(Subsystem.CORE, null, "Exit called for invalid db_id: " + db_id, Log.P_VERBOSE);
            return;
        }
        
        stopID(db_id);
      
        Gatekeeper.instance().removeAllInstrumentsForID(db_id);
        
        // Clean up tables
        sonifications.remove(db_id);
        
        for (Iterator<ID> it = sonificationConfigs.keySet().iterator(); it.hasNext() ; ) {
            ID id = it.next();
            if (id.getDB_ID() == db_id) {
                it.remove();
            }
        }
        
        for (Iterator<ID> it = dataSources.keySet().iterator(); it.hasNext() ; ) {
            ID id = it.next();
            
            if (id.getDB_ID() == db_id) {
                DataSource ds = dataSources.get(id);
                ds.shutdown();
                it.remove();
            }
        }
        
        for (Iterator<ID> it = dataSourceConfigs.keySet().iterator(); it.hasNext() ; ) {
            ID id = it.next();
            if (id.getDB_ID() == db_id) {
                it.remove();
            }
        }
        
        dbg.manager.shutdown();
        active_dbs.remove(db_id);
        System.gc();
    }
    
    /**
     * Used to report an Object with erroneous status.
     * 
     * @param sender
     *            the object that needs attention
     */
    public void error(Object sender) {
        errorReports.put(sender);
    }

    /**
     * Retrieves status of the core
     * 
     * @return true if core thread is running, else false.
     */
//    public boolean getStatus() {
//        return (core_thread != null) && core_thread.isAlive() && !shouldStop;
//    }

    /**
     * Returns data source objects. Called by SocketUI.
     * 
     * @return a map of data source objects
     */
    public synchronized Map<ID, DataSource> getDataSources() {
        return dataSources;
    }

    /**
     * Gets the configs for the sonification objects.
     * 
     * @return the sonification configs
     */
    public synchronized Map<ID, Config> getSonificationConfigs() {
        return sonificationConfigs;
    }

    public void update(Integer id, Map<Key, Object> m) {
        // /* Right now, ENABLED is handled by the Core,
        // * the other keys are handled by the sonifications.
        // */
        // if (m.containsKey(Key.ENABLED))
        // {
        // Boolean enabled = (Boolean) m.get(Key.ENABLED);
        // System.out.println("calling change enabled: " + enabled + " for id: "
        // + id);
        // changeEnabled(id, enabled);
        // m.remove(Key.ENABLED);
        // }
        // if (m.size() > 0)
        // {
        // SonificationGroup sg = sonifications.get(id);
        // sg.s.configChanged(m);
        // }
    }

    /**
     * Updates config information for a sonification. This method should be
     * deprecated since it is only used with the applet UI.
     * 
     * @param ID
     *            the identifier of the sonification to update
     * @param column
     *            info associated with the field to update
     * @param value
     *            the new value
     * @return true if successful, false if not
     */
    // public boolean updateConfig(Integer ID, ColumnInfo column, Object value)
    // {
    // if (column.recipient == Core.class)
    // {
    // if (column.field == Key.ENABLED)
    // changeEnabled(ID, value);
    // return true;
    // }
    // else if (column.recipient == Gatekeeper.class)
    // {
    // Log.println(Subsystem.CORE, null, "Gatekeeper update config",
    // Log.P_VERBOSE);
    // }
    // else if (column.recipient == Sonification.class)
    // {
    // Config c = sonificationConfigs.get(ID);
    // return c.setField(column.field, value);
    // }
    // return false;
    // }
    /**
     * Updates config information important to Core.
     * 
     * @param ID
     *            identifier for the object associated with this update
     * @param value
     *            the new value
     * @return true if successful, otherwise false
     */
    // private boolean changeEnabled(Integer ID, Object value)
    // {
    // System.out.println("changedEnabled: " + ID + " value: " + value);
    // SonificationGroup g = sonifications.get(ID);
    // try
    // {
    // if (!(value instanceof Boolean))
    // return false;
    //            
    // if (value.equals(Boolean.TRUE))
    // {
    // g.enabled = true;
    // if (core_thread != null && core_thread.isAlive())
    // {
    // g.s.initInstrument();
    // g.s.start();
    // }
    //                
    // }
    // else if (value.equals(Boolean.FALSE))
    // {
    // g.enabled = false;
    // if (core_thread != null && core_thread.isAlive())
    // g.s.stop();
    //                
    // }
    //            
    // // Update the config info for this sonification
    // Config c = sonificationConfigs.get(ID);
    // c.setField(Key.ENABLED, value);
    // }
    // catch (Exception e)
    // {
    // e.printStackTrace();
    // Log.println(Subsystem.CORE, ReturnCode.GENERAL_ERROR, "Exception: Core
    // cannot start " + ID, Log.P_ERROR);
    // return false;
    // }
    // return true;
    // }

    /**
     * Plays a the last event for a sonification.
     * 
     * @param ID
     *            the identifier of the sonification to play
     */
    public void replay(Integer ID) {
        /*
         * redesign merge this method and updateConfig code
         */
//        SonificationGroup g = sonifications.get(ID);
//        if (g != null) {
//            g.s.replay();
//        }
    }

    /**
     * Main loop for the core.
     */
    private void runCore() {
        try {
            Log.println(Subsystem.CORE, null, "started", Log.P_INFO);
            while (!shouldStop) {
                try {
                    Thread.sleep(Const.DB_DEAD_INTERVAL / 2);
                } catch (InterruptedException e) {
                    Log.println(Subsystem.CORE, null, "Core Interrupted",
                            Log.P_VERBOSE);
                }

                for (Integer id : active_dbs.keySet()) {
                    DBGroup dbg = active_dbs.get(id);
                    long current = System.currentTimeMillis();
                    long age = current - dbg.last;
                    if (age > Const.DB_DEAD_INTERVAL) {
                        Log.println(Subsystem.CORE, ReturnCode.GENERAL_WARNING,
                            "runCore: db_id " + id + " inactive for " + (age / 1000)
                            + "s (> " + (Const.DB_DEAD_INTERVAL / 1000)
                            + "s) — calling exitID");
                        exitID(id);
                    }
                }
            }
        } catch (Exception e) {
            Log.println(Subsystem.CORE, ReturnCode.GENERAL_ERROR,
                    "runCore caught an Exception " + e, Log.P_ERROR);
        } finally {
            stopEverything();
            for (UI ui : uis.values()) {
                ui.setMode(Const.STOPPED);
            }
        }
        Log.println(Subsystem.CORE, null, "stopped", Log.P_INFO);
    }

    /**
     * Handle errors from other objects. Currently unimplemented.
     * 
     * @param errorObject
     * @throws Exception
     */
    private void handleError(Object errorObject) throws Exception {
    }

    /**
     * Stops data sources, gatekeeper and sonifications.
     */
    private synchronized void stopEverything() {
        // Stop the gatekeeper
        Gatekeeper.instance().stop();

        for (Integer id : active_dbs.keySet()) {
            stopID(id);
        }
    }

    /**
     * Causes Core to instantiate the UI specified by its config.
     */
    public void setUI(Integer db_id) {
        // // Create UI
        // Log.println(Subsystem.CORE, null, "Attempting to create UI");
        //        
        // try
        // {
        // Class<?> ui_class = (Class) coreConfig.getField(Key.UI);
        // uis.put(db_id, (UI) ui_class.newInstance());
        // }
        // catch (Exception e)
        // {
        // e.printStackTrace();
        // throw new IllegalStateException(e.toString());
        // }
    }

    /**
     * Causes Core to use an already instantiated UI.
     * 
     * @param ui
     *            the UI to use
     */
    public void setUI(UI ui, Integer db_id) {
        // this.uis.put(db_id, ui);
        // UIHelper helper = null;
        // // Instantiate UI Helper
        // try
        // {
        // Class<?> helper_class = (Class) coreConfig.getField(Key.UI_HELPER);
        // helper = (UIHelper) helper_class.newInstance();
        //            
        // /*
        // * remove the package from the class name and see if this class has
        // * an associated table.
        // */
        // String helper_name = helper_class.getSimpleName();
        // if (DBManager.instance().isTable(helper_name))
        // {
        // 
        // Config config = new DBConfig(helper_class.getSimpleName(), Key.DB_ID,
        // db_id);
        // helper.setConfig(config);
        // }
        //            
        // }
        // catch (Exception e)
        // {
        // Log.println(Subsystem.CORE, null, "using DefaultUIHelper because of
        // exception: " + e.toString(), Log.P_INFO);
        // helper = new DefaultUIHelper();
        // }
        // finally
        // {
        // uihelpers.put(db_id, helper);
        // }
        //        
        // ui.setHelper(helper);
    }

    public void uiJob(ID id, DataInfo info) {
        // skip if there is no UI
        for (UI ui : uis.values()) {
            ui.uiJob(id, info);
        }
    }

    /**
     * All processed messages get sent back to the Core. This could become a
     * listener-style design where objects register with the Core. For now, only
     * the ui cares about completed messages.
     * 
     * @param message
     *            the message that has been processed
     */
    public void completedMessage(MessageComponent message) {
        MessageComponent.Stamp corestamp = message.getStamp(ReturnCode.DB_ID);

        if (uis == null) {
            Log.println(Subsystem.CORE, ReturnCode.NO_CODE,
                    "Not Sending Message as UI is NULL", Log.P_INFO);
            return;
        }

        if (corestamp != null) {
            uis.get(Integer.parseInt(corestamp.message)).completedMessage(
                    message);
        } else {
            for (UI ui : uis.values()) {
                if (ui != null)
                    ui.completedMessage(message);
            }
        }

        // Log.println(Subsystem.CORE, ReturnCode.NO_CODE, "Not Sending Message
        // as UI is NULL", Log.P_INFO);
    }

    /**
     * Mark a db_id as having received recent activity from its client. Resets
     * the inactivity timer that {@link #runCore()} uses to decide whether the
     * db is dead and should be torn down. Called by {@link com.dataSonification.v2.ui.SocketUI}
     * on every incoming data and control message that has a db_id.
     *
     * Prior to Stage 6 this was a side-effect of {@code getStatus(RUNNING, ...)},
     * which only fired from the disconnected-data-source recovery path in
     * SocketUI — meaning a healthy session never updated {@code dbg.last}
     * and the watchdog would kill it ~30-45 minutes after start.
     */
    public void touchActivity(int db_id) {
        DBGroup dbg = active_dbs.get(db_id);
        if (dbg != null) {
            dbg.last = System.currentTimeMillis();
        }
    }

    public String getStatus(Key key, SystemObject obj) {
    
        int db_id = obj.getDB_ID();
        int s_id  = obj.getS_ID();
        
        String retval = null;
        
        // Need to add status for core object
        
        if (s_id > 0) {
            Log.println(Subsystem.CORE,ReturnCode.GENERAL_WARNING,"Getting status for " + obj);
        } else { 
            switch(key) {
                case RUNNING:
                    DBGroup dbg = active_dbs.get(db_id);
                    if (dbg == null)
                        retval = "Invalid DB";
                    else {
                        if (dbg.running)
                            retval = "TRUE";
                        else
                            retval = "FALSE";
                        dbg.last = System.currentTimeMillis();
                        active_dbs.put(db_id, dbg);
                    }
                    // TODO: make active_dbs synchronized
                    
                    break;
                case VERSION:
                    retval = Const.VERSION + "r" + Const.REVISION;
                    break;
                    
                case VERSION_VERBOSE:
                    retval = Const.getVersionString();
                    break;
                    
                case UPTIME:
                    retval = Long.toString((System.currentTimeMillis() - start_time)/1000);
                    break;
                    
                case ACTIVE_DBS:
                    retval = "<db_list>";
                    for (Integer id : active_dbs.keySet()) {
                        DBGroup dbgroup = active_dbs.get(id);
                        String db_string = "<db>";
                        db_string += "<id>" + id.toString() + "</id>";
                        db_string += "<path>\"" + dbgroup.manager.getName() + "\"</path>";
                        db_string += "<running>" + Boolean.toString(dbgroup.running) + "</running>";
                        db_string += "</db>";
                        retval += db_string;
                    }
                    
                    retval += "</db_list>";
                    break;
                    
                default:
                    Log.println(Subsystem.CORE,ReturnCode.GENERAL_WARNING,"Getting status for " + obj);
            }
        }
        return retval;
    }
    
    public static void statusReport(Subsystem s, ReturnCode rc, String message) {
        if (core != null) {
            StatusComponent status = new StatusComponent();

            status.setAction(ActionType.NOTIFY);
            // redesign...this code is not correct. StatusReport must take a
            // core_id
            status.setObject(new SystemObject(1, 1));
            status.stamp(s, rc, message);
            core.completedMessage(status);
        }
    }



    /**
     * Container class for a sonification and its enabled status.
     * 
     * @author Kimo Johnson
     */
    // This class is static because it does not depend on an instance of Core
    private static class SonificationGroup {
        public final Sonification s;

        public boolean enabled;

        public SonificationGroup(Sonification s, boolean enabled) {
            this.s = s;
            this.enabled = enabled;
        }
    }

    private static class DBGroup {
        public final DBManager manager;

        public boolean running;
        public long last;

        public DBGroup(DBManager m, boolean running) {
            this(m,running,System.currentTimeMillis());
        }
        
        public DBGroup(DBManager m, boolean running, long last) {
            this.manager = m;
            this.running = running;
            this.last = last;
        }
        
        
    }
}
