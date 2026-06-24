package com.dataSonification.v2.data;

import java.util.HashMap;
import java.util.Map;

import com.dataSonification.v2.Config;
import com.dataSonification.v2.Core;
import com.dataSonification.v2.ID;
import com.dataSonification.v2.sound.Sonification;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.SyncBuffer;
import com.dataSonification.v2.util.Subsystem;
import com.dataSonification.v2.util.ReturnCode;

public class V2SocketDataSource extends DataSource
{
    private Map<ID,Sonification> sonifications;
    private Object dataLock = new Object();
    
    private Config config;
    
    private volatile boolean shouldStop = true;
    private Thread ds_thread;
    private final SyncBuffer<DataComponent> data;
    
    
    public V2SocketDataSource()
    {
        super();
        sonifications = new HashMap<ID,Sonification>();
        data = new SyncBuffer<DataComponent>();
        connected = false;
    }
    
    public V2SocketDataSource(Config newConfig)
    {
        this();
        setConfig(newConfig);
    }
    
    public synchronized void setConfig(Config newConfig)
    {
        config = newConfig;
    }
    
    public synchronized boolean addDataEntity(Config config, ID id, Sonification sonification)
    {
        if (id == null || sonification == null)
        {
            return false;
        }
        
        Log.println(Subsystem.DATA, null, "V2SocketDataSource adding: " + id, Log.P_VERBOSE);
        
        sonifications.put(id, sonification);
        return true;
    }
    
    
    
    public void connect()
    {
        if (ds_thread != null && ds_thread.isAlive())
            return;
        
        shouldStop = false;
        data.clear();
        ds_thread = new Thread(new Runnable()
        {
            public void run()
            {
                runDataSource();
            }
        });
        ds_thread.start();
        connected = true;
    }
    
    public void put(DataComponent c)
    {
        if (ds_thread != null && ds_thread.isAlive())
            data.put(c);
    }
    
    public void disconnect()
    {
        // Stage 6 instrumentation: who called disconnect, on which thread.
        Log.println(Subsystem.DATA, null,
            "V2SocketDataSource.disconnect called. caller_thread=" + Thread.currentThread().getName()
            + " ds_thread_alive=" + (ds_thread != null && ds_thread.isAlive()));

        shouldStop = true;

        if (ds_thread == null)
            return;

        if (ds_thread.isAlive())
        {
            try
            {
                ds_thread.interrupt();
                ds_thread.join();
            }
            catch (Exception e)
            {}
            ds_thread = null;
        }
        connected = false;
    }
    
    public void runDataSource()
    {
        try
        {
            while (!shouldStop)
            {
                DataComponent currentData = data.get();
                Log.println(Subsystem.DATA, null, "Data Received: " + currentData.toXML(), Log.P_VERBOSE);
                
                if (currentData == null)
                    break;
                
                processData(currentData);
                
            }
        }
        catch (InterruptedException e)
        {
            Log.println(Subsystem.DATA, null, "V2SocketDataSource: interrupted.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void processData(DataComponent c)
    {
        try
        {
            SystemObject obj = c.getObject();
            int db_id = obj.getDB_ID();
            int s_id = obj.getS_ID();
            
            ID id = new ID(db_id, s_id, ID.type.Sonification);
            Sonification s = sonifications.get(id);
            
            // Could implement message validation here
            if (s != null)
                s.update(c);
            else
                Log.println(Subsystem.DATA,ReturnCode.GENERAL_WARNING,"V2SocketDataSource cannot process data for null sonification");
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            Log.println(Subsystem.DATA, ReturnCode.GENERAL_ERROR, "V2 Socket Data Source: " + e, Log.P_ERROR);
        }
       
    }
    
    /* (non-Javadoc)
     * @see com.dataSonification.v2.DataSource#reinitialize()
     */
    public int reinitialize()
    {
        return 0;
    }
    
    /* (non-Javadoc)
     * @see com.dataSonification.v2.data.DataSource#removeDataEntity(java.lang.Integer)
     */
    public void removeDataEntity(Integer ID)
    {
        if (ID != null)
        {
            Config config = Core.instance().getSonificationConfigs().get(ID);
            Sonification s = sonifications.remove(ID);
            
        }
    }
}
