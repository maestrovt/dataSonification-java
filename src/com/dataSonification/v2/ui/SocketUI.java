package com.dataSonification.v2.ui;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.dataSonification.v2.Core;
import com.dataSonification.v2.ID;
import com.dataSonification.v2.MainDaemon;
import com.dataSonification.v2.data.ActionType;
import com.dataSonification.v2.data.ControlComponent;
import com.dataSonification.v2.data.DataComponent;
import com.dataSonification.v2.data.DataInfo;
import com.dataSonification.v2.data.DataSource;
import com.dataSonification.v2.data.Message;
import com.dataSonification.v2.data.MessageComponent;
import com.dataSonification.v2.data.SAXMessageParser0_2;
import com.dataSonification.v2.data.StatusComponent;
import com.dataSonification.v2.data.SystemObject;
import com.dataSonification.v2.data.V2SocketDataSource;
import com.dataSonification.v2.util.Converter;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.ReturnCode;
import com.dataSonification.v2.util.Subsystem;
import com.dataSonification.v2.util.SyncBuffer;

/**
 *
 * @author kimo
 */
public class SocketUI implements UI
{ 
    private BufferedReader socketReader = null;
    
    private PrintWriter socketWriter = null;
    
    private volatile boolean shouldStop = true;
    
    private Socket socket = null;
    
    private Thread in_thread = null;
    
    private Thread out_thread = null;
    
    private boolean initialized;
    
    private Map<Integer,V2SocketDataSource> dataSources;
    
    private UIHelper helper;
    
    private SAXParser parser;
    
    private SyncBuffer<MessageComponent> outgoing;
    
    public SocketUI(Socket s)
    {
        try
        {
            javax.xml.parsers.SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            parser = factory.newSAXParser();
            
            socket = s;
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketWriter = new PrintWriter(socket.getOutputStream(), true);
            
            outgoing = new SyncBuffer<MessageComponent>();
            dataSources = new HashMap<Integer,V2SocketDataSource>();
            initialized = false;
        }
        catch (Exception e)
        {
            closeAll();
            throw new IllegalArgumentException("SocketUI: cannot instantiate: "  + e.toString());
        }
    }
    
    
    private void runIncoming()
    {
        try
        {
            String line;
            while (!shouldStop)
            {
                line = socketReader.readLine();
                
                if (line == null)
                {
                    Log.println(Subsystem.UI, null, "SocketUI: runIncoming got null line.");
                    break;
                }
                
                // Check for SHUTDOWN to stop MainDaemon
                if (line.trim().toUpperCase().equals("SHUTDOWN"))
                {
                    MainDaemon.instance().stop();
                    shouldStop = true;
                    break;
                }
                
                InputSource src = new InputSource(new StringReader(line));
                SAXMessageParser0_2 sax = new SAXMessageParser0_2();
                try
                {
                    parser.parse(src, sax);
                    Message m = sax.getMessage();
                    processMessage(m);
                }
                catch (SAXException e)
                {
                    Log.println(Subsystem.UI, ReturnCode.XML_PARSE_ERROR, "SocketUI: failed to parse message.", Log.P_ERROR);
                }
                
            }
        }
        catch (SocketException e)
        {
            Log.println(Subsystem.UI, null, "SocketUI: socket closed.");
        }
        catch (UnknownHostException e)
        {
            Log.println(Subsystem.UI, null, "SocketUI: caught UnknownHostException");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            Log.println(Subsystem.UI, null, "SocketUI: caught IOException");
            e.printStackTrace();
        }
        finally
        {
            closeAll();
        }
        Log.println(Subsystem.UI, null, "SocketUI: socket closed.", Log.P_VERBOSE);
    }
    
    
    private void runOutgoing()
    {
        try
        {
            socketWriter.flush();
            
            MessageComponent processed;
            while (!shouldStop)
            {
                processed = outgoing.get();
                
                Message m = new Message();
                m.addComponent(processed);
                Log.println(Subsystem.UI, null, "SocketUI: got completed message from queue: Sending \n" + m.toXML(), Log.P_VERBOSE);
                
                socketWriter.println(m.toXML());
            }
            
        }
        catch (InterruptedException e)
        {
            Log.println(Subsystem.UI, null, "SocketUI: caught InterruptedException");
        }
        finally
        {
            closeAll();
        }
        Log.println(Subsystem.UI, null, "SocketUI: socket closed.", Log.P_VERBOSE);
    }
    
    private void closeAll()
    {
        if (socketWriter != null)
        {
            try
            { socketWriter.close(); }
            catch (Exception e)
            {}
        }
        
        if (socketReader != null)
        {
            try
            { socketReader.close(); }
            catch (Exception e)
            {}
        }
        
        if (socket != null && !socket.isClosed())
        {
            try
            { socket.close(); }
            catch (Exception e)
            {}
        }
        socket = null;
    }
    
    private void processMessage(Message m)
    {
        for (int i = 0, n = m.size(); i < n; ++i)
        {
            MessageComponent mc = m.getComponent(i);
            if (!mc.isValid())
            {
                mc.stamp(Subsystem.UI, ReturnCode.MESSAGE_COMPONENT_INVALID, "SocketUI: Message Invalid");
                // mc.finished();
                completedMessage(mc);
                continue;
            }
            
            // only control messages for core can be called when the core is null
            if (!initialized && !(mc.getObject().isCore() && mc instanceof ControlComponent)) {
                mc.stamp(Subsystem.UI, ReturnCode.CORE_IS_NULL, "only control messages for core can be sent when core is null");
                completedMessage(mc);
                continue;
            }
            
            if (mc instanceof ControlComponent)
            {
                ControlComponent c = (ControlComponent) mc;
                SystemObject object = c.getObject();
                if (object.isCore()) {
                    controlCore(c);
                } else {
                    processControlComponent(c, object);
                }
            }
            else if (mc instanceof StatusComponent)
            {
                processStatusComponent((StatusComponent) mc);  
            }
            else if (mc instanceof DataComponent)
            {
                processDataComponent((DataComponent) mc);
            }
            else
            {
                mc.stamp(Subsystem.UI, ReturnCode.GENERAL_ERROR,"SocketUI: unrecognized MessageComponent.");
                completedMessage(mc);
                Log.println(Subsystem.UI, ReturnCode.GENERAL_ERROR, "SocketUI: unrecognized MessageComponent.", Log.P_VERBOSE);
            }
        }
    }
    
    
    /**
     * Process a status message
     * @param c
     */
    private void processStatusComponent(StatusComponent c)
    {
        ActionType action = c.getAction();
        SystemObject obj  = c.getObject();
        
        ReturnCode rc = ReturnCode.NO_CODE;
        
        if (obj.isValid() && action == ActionType.REQUEST) {
            Map<Key,String> fields = c.getFields();
            for (Key key : fields.keySet()) {
                String retval = Core.instance().getStatus(key, obj);
                if (retval != null) {
                    fields.put(key, retval);
                    rc = ReturnCode.STATUS_SUCCESS;
                } else {
                    rc = ReturnCode.STATUS_FAILED;
                }
                c.stamp(Subsystem.UI,rc,null);
            }
            c.setFields(fields);
            
        }
        completedMessage(c);
        
    }
    
    /**
     * Process a control message for a sonification
     * @param c the control component
     * @param obj the object of the action, validity checked in processMessage()
     */
    private void processControlComponent(ControlComponent c, SystemObject obj)
    {
        ActionType action = c.getAction();
        Integer id = Integer.valueOf(obj.getS_ID());
        int db_id = obj.getDB_ID();

        // Stage 6 instrumentation: every per-sonification control action received from peer.
        Log.println(Subsystem.UI, null,
            "RX control action=" + action + " db_id=" + db_id + " s_id=" + id
            + " thread=" + Thread.currentThread().getName()
            + " peer=" + (socket != null ? socket.getRemoteSocketAddress() : "null"));

        switch(action)
        {
            case REPLAY:
                Core.instance().replay(id);
                c.stamp(Subsystem.UI, ReturnCode.CONTROL_SUCCESS, null);
                break;
            case UPDATE:
                Map<Key,String> fields = c.getFields();
                
                Map<Key,Object> m = new EnumMap<Key,Object>(Key.class);
                for (Key key : fields.keySet())
                {
                    Object value = Converter.convert(fields.get(key), key.getType());
                    if (value != null)
                    {
                        m.put(key, value);
                    }
                }
                Core.instance().update(id, m);
                break;
            case START:
                if (Core.instance().startID(db_id)) {
                    c.stamp(Subsystem.UI,ReturnCode.CORE_START_SUCCESS,"Core started for ID: " + db_id);
                } else {
                    c.stamp(Subsystem.UI,ReturnCode.CORE_START_FAILED,"Core start failed for ID: " + db_id);
                }
                break;
            case STOP:
                if (Core.instance().stopID(db_id)) {
                    c.stamp(Subsystem.UI,ReturnCode.CONTROL_SUCCESS,"Core stopped for ID: " + db_id);
                } else {
                    c.stamp(Subsystem.UI,ReturnCode.CONTROL_FAILED,"Core stop failed for ID: " + db_id);
                }
                break;
            case EXIT:
                Core.instance().exitID(db_id);
                c.stamp(Subsystem.UI,ReturnCode.CONTROL_SUCCESS,"Core exited for ID: " + db_id);
                break;
            default:
                Log.println(Subsystem.UI,ReturnCode.GENERAL_WARNING,"Action not supported: " + action);
                
        }
        completedMessage(c);
        
    }
    
    /**
     * Process a control message for the core
     * @param c the control component
     */
    private void controlCore(ControlComponent c)
    {
        ActionType action = c.getAction();
        ReturnCode rc = ReturnCode.NO_CODE;

        // Stage 6 instrumentation: every core-level control action (INIT, EXIT) received from peer.
        Log.println(Subsystem.UI, null,
            "RX core action=" + action
            + " thread=" + Thread.currentThread().getName()
            + " peer=" + (socket != null ? socket.getRemoteSocketAddress() : "null"));

        switch (action) {
        case INIT:
            rc = initCore(c);
            // This clause reached when init is called twice to the same SocketUI
            Log.println(Subsystem.UI, null, "SocketUI: init called", Log.P_VERBOSE);
            Core.instance().start();
            
            // Get current list of data sources from the core
            setDataSources();
            break;
        case EXIT:
            if (initialized) {
                Core.instance().exit();
                initialized = false;;
            }
            rc = ReturnCode.GENERAL_SUCCESS;
            break;
        default:
            Log.println(Subsystem.UI,rc,"SocketUI: controlCore does not support action: " + action.toString());  
        }
  
        completedMessage(c);
    }
    
    private ReturnCode initCore(ControlComponent c) {
        if (c.getAction() != ActionType.INIT) {
            Log.println(Subsystem.UI, ReturnCode.CORE_NULL_ERROR, "SocketUI: core is null", Log.P_ERROR);
            return ReturnCode.CORE_IS_NULL;
        }
        
        ReturnCode rc = ReturnCode.NO_CODE;
        Map<Key,String> fields = c.getFields();
        
        if (fields == null) {
            fields = new HashMap<Key,String>();
        }
 
        int db_id = 0;
        try
        {
            // This instantiation has no effect if the core already exists
            Core core = Core.instance();
            db_id = core.init(fields);
            System.out.println("DB_ID: " + db_id);
           // redesign...This call is incompatible with simultaneous connections
            // to the same core
            core.setUI(this, db_id);
            rc = ReturnCode.CORE_INIT_SUCCESS;
            c.stamp(Subsystem.UI, rc, Integer.toString(db_id));
            initialized = true;
        }
        catch(NoClassDefFoundError e)
        {
           // e.printStackTrace();
           // Log.println(Subsystem.UI, ReturnCode.CORE_INIT_FAILED, "Fatal Error: Missing Class: " + e.getMessage(), Log.P_ERROR);
            c.stamp(Subsystem.UI, ReturnCode.GENERAL_ERROR, "Fatal Error: Missing Class: " + e.getMessage());
            rc = ReturnCode.CORE_INIT_FAILED;
            initialized = false;
        }
        catch (Exception e)
        {
            Log.println(Subsystem.UI, ReturnCode.CORE_INIT_FAILED, "SocketUI: caught exception trying to instantiate core", Log.P_ERROR);
            initialized = false;
//            if (core != null)
//            {
//                core.exit();
//                core = null;
//            }
            rc = ReturnCode.CORE_INIT_FAILED;
        }

        return rc;
    }
    
    private void processDataComponent(DataComponent c)
    {
        int db_id = c.getObject().getDB_ID();
        V2SocketDataSource ds = dataSources.get(db_id);

        // Stage 6 fix: data arrival is the heartbeat. Mark the db active
        // so the Core.runCore() watchdog does not tear down a healthy session.
        Core.instance().touchActivity(db_id);

        if (ds != null && ds.is_shutdown()) {
            Log.println(Subsystem.UI,ReturnCode.GENERAL_WARNING,"data source for id " + db_id + " has been shutdown, reinitializing data source list");
            setDataSources();
            ds = dataSources.get(db_id);
        }
        
        if (ds != null) {
            if (!ds.connected()) {
                // Get running status of this db_id
                SystemObject obj = new SystemObject(db_id);
                String rc = Core.instance().getStatus(Key.RUNNING, obj);
                
                if (rc.equals("FALSE")) {
                    Log.println(Subsystem.UI,ReturnCode.GENERAL_WARNING,"data source is not connected, call start for db_id " + db_id);
                } else {
                    Log.println(Subsystem.UI,ReturnCode.GENERAL_ERROR,"data source reference is no longer valid for db_id " + db_id + ". reinitialize core."); 
                    c.stamp(Subsystem.UI,ReturnCode.CORE_IS_NULL,"data source reference is no longer valid for db_id " + db_id + ". reinitialize core.");
                    completedMessage(c);
                    return;
                }
            }
            ds.put(c);
        }
        else {
            c.stamp(Subsystem.UI, ReturnCode.DATA_SOURCE_NULL_ERROR, "data source is null for db_id " + db_id);
            completedMessage(c);
            Log.println(Subsystem.UI, ReturnCode.DATA_SOURCE_NULL_ERROR, "SocketUI: data source is null for db_id: " + db_id, Log.P_ERROR);
        }
    }
    
    
    /**
     * Get a reference to a V2SocketDataSource from the Core.  This method does
     * not correctly support multiple V2SocketDataSources.
     * @return true if successful, false if not
     */
    private void setDataSources()
    {
        Map<ID,DataSource> temp = Core.instance().getDataSources();
        
        dataSources.clear();
        
        for (ID id : temp.keySet()) {
            DataSource ds = temp.get(id);
            if (ds instanceof V2SocketDataSource) {
                dataSources.put(id.getDB_ID(), (V2SocketDataSource)ds);
            }
        }  
    }
    
    
    public void start()
    {
        shouldStop = false;
        in_thread = new Thread(new Runnable()
        {
            public void run()
            {
                runIncoming();
            }
        });
        
        in_thread.start();
        
        out_thread = new Thread(new Runnable()
        {
            public void run()
            {
                runOutgoing();
            }
        });
        
        out_thread.start();
    }
    
    public void stop()
    {
        shouldStop = true;
        if (in_thread != null && in_thread.isAlive())
        {
            in_thread.interrupt();
        }
        
        if (out_thread != null && out_thread.isAlive())
        {
            out_thread.interrupt();
        }
    }
    
    
    public boolean isAlive()
    {
        return (in_thread != null) && in_thread.isAlive();
    }
    
    /*
     * Javadoc in interface
     */
    public void setMode(int mode)
    {}
    
    /*
     * Javadoc in interface
     */
    public void reset()
    {}
    
    /*
     * Javadoc in interface
     */
    public void uiJob(ID id, DataInfo info)
    {
        
        switch(info.type)
        {
            case ERROR:
                Log.println(Subsystem.UI, null, "SocketUI: Error: " + id);
                break;
            case NO_INFO:
                Log.println(Subsystem.UI, null, "SocketUI: No info for: " + id);
                break;
            case IGNORED:
                Log.println(Subsystem.UI, null, "SocketUI: Ignoring: " + id);
                break;
            default:
                helper.help(id, info);
        }
        
    }
    
    /*
     * Javadoc in interface
     */
    public void setHelper(UIHelper helper)
    {
        this.helper = helper;
    }
    
    /*
     * Javadoc in interface
     */
    public void completedMessage(MessageComponent message)
    {
        Log.println(Subsystem.UI, null, "Adding Message to Outgoing", Log.P_VERBOSE);
        outgoing.put(message);
    }
    
}