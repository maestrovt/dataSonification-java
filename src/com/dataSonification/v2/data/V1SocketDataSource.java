/*
 * V1SocketDataSource.java
 *
 * Created on May 6, 2004, 7:13 PM
 */

package com.dataSonification.v2.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.dataSonification.v2.Config;
import com.dataSonification.v2.Core;
import com.dataSonification.v2.ID;
import com.dataSonification.v2.sound.Sonification;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.Subsystem;
import com.dataSonification.v2.util.ReturnCode;
/**
 * Data source using V1 protocol.
 *
 * @author  Kimo Johnson
 */
public class V1SocketDataSource extends DataSource {
    
    /**
     * Address of the data server.
     */
    private String address;
    
    /**
     * Port for socket connection.
     */
    private int port;
    
    /**
     * Timeout interval for the socket.
     */
    private int timeout;

    /**
     * The list of entities this data source wishes to receive.
     */
    private String entitiesList = null;
    
    /**
     * Storage for sonifications by ID.
     */
    private Map<Integer,Sonification> sonifications;
    
    /**
     * Storage for sonification configs
     */
    private Map<Integer,Config> configs;
    
    /**
     * Storage for ID by ticker.
     */
    private Map<String,Integer> ticker_id;
    
    /**
     * Reference to the data source thread.
     */
    private Thread ds_thread;
    
    /**
     * Connection to server.
     */
    private Socket socket = null;
   
    /**
     * Reads strings from the socket.
     */
    private BufferedReader socketReader = null;
   
    /**
     * Writes strings to the socket.
     */
    private BufferedWriter socketWriter = null;
    
    /**
     * Boolean to stop the thread.
     */
    private volatile boolean shouldStop = false;
    
    public V1SocketDataSource() {
        sonifications = new HashMap<Integer,Sonification>();
        configs = new HashMap<Integer,Config>();
        ticker_id = new HashMap<String,Integer>();
        
        ds_thread = null;
    }

    
    /* 
     * Javadoc in superclass
     */
    public void setConfig(Config config) {
        super.setConfig(config);
        init();   
    }
    
    /**
     * Initializes the address and port for this data source.
     */
    private void init() {
        address = (String)config.getField(Key.ADDRESS);
        port = ((Integer)config.getField(Key.PORT)).intValue();
        timeout = ((Integer)config.getField(Key.TIMEOUT)).intValue();
    }
    
    
    /**
     * The work thread.  Establishes the connection and receives data.
     */
    private void runDataSource() {
        
    		try {
    		    
			socket = new Socket(address, port);
			socket.setSoTimeout(timeout);
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			Log.println(Subsystem.DATA, null, "V1SocketDataSource EntitiesList: " + entitiesList);
			socketWriter.write(entitiesList);
			socketWriter.flush();

			String line;
			while (!shouldStop) {
				line = socketReader.readLine();
				if (line == null && socket.isClosed()) {
					Log.println(Subsystem.DATA, null, "V1SocketDataSource: runDataSource got null line.", Log.P_INFO);
					break;
				}
				if (!shouldStop && line != null)
					processData(line);
			}
			
    		}
    		catch (SocketException e) {
    			Log.println(Subsystem.DATA, null, "V1SocketDataSource: socket closed.", Log.P_INFO);
    		}
    		catch (UnknownHostException e) {
    			Log.println(Subsystem.DATA, ReturnCode.GENERAL_ERROR, "V1SocketDataSource caught UnknownHostException", Log.P_ERROR);
    			e.printStackTrace();
    		}
    		catch (IOException e) {
    			Log.println(Subsystem.DATA, ReturnCode.GENERAL_ERROR, "V1SocketDataSource caught IOException", Log.P_ERROR);
    			e.printStackTrace();
    		}
    		finally {
    			Log.println(Subsystem.DATA, null, "V1SocketDataSource: reached finally", Log.P_VERBOSE);
    			if (socketWriter != null)
    				try { socketWriter.close(); } catch (Exception e) {}
    		
    			if (socketReader != null)
    				try { socketReader.close(); } catch (Exception e) {}
    		
    			if (socket != null && !socket.isClosed())
    				try { socket.close(); } catch (Exception e) {}
    		
    			// Eventually, the core will need to handle this error more
    			// appropriately, for now stop everything.
    			Core.instance().stop();
    		}
    		Log.println(Subsystem.DATA, null, "V1SocketDataSource exiting", Log.P_INFO);
    }
    
    /**
     * Parses the String received from the socket and dispatches DataEvent to
     * a sonification object.
     * @param line the String to process
     */
    private void processData(String line) {
        //Get the name of the security
        StringTokenizer stringT = new StringTokenizer(line, ",");
        String fields[] = new String[stringT.countTokens()];
        
        for(int i = 0; i < fields.length; i++) {
            fields[i] = stringT.nextToken();
        }
        
        // Handle STOP command from client
        if (fields.length < 2) {
            if (fields[0].equals("STOP")) {
                shouldStop = true;
                
                // prep a uiJob to pass this STOP message to whatever UI is out there
                Integer id = Integer.valueOf(0);
                //Core.instance().uiJob(id., DataInfo.STOP);
                return;
            } else if (fields[0].substring(0,6).equals("REPLAY")) {
                stringT = new StringTokenizer(fields[0], "*");
                if (stringT.countTokens() != 2) {
                    Log.println(Subsystem.DATA, ReturnCode.GENERAL_ERROR,"V1SocketDataSource: error in REPLAY", Log.P_ERROR);
                    return;
                }
                stringT.nextToken();
                
                String tickerName = (String)stringT.nextToken();
                Integer id = (Integer)ticker_id.get(tickerName);
                if (id == null) {
                    Log.println(Subsystem.DATA, ReturnCode.GENERAL_ERROR, "V1SocketDataSource: id not found for tickerName: " + tickerName, Log.P_ERROR);
                    return;
                }
                Core.instance().replay(id);
                return;
            }
        }
        
        String fieldsParsed[][] = new String[2][fields.length];
        long time = (new Date()).getTime();
        String tickerName = "";
        
        for(int i = 0; i < fields.length; i++)
        {
            stringT = new StringTokenizer(fields[i], "*"); 
            if(stringT.countTokens() != 2)
                continue;
            
            
            fieldsParsed[0][i] = stringT.nextToken();
            fieldsParsed[1][i] = stringT.nextToken(); 
            if(fieldsParsed[0][i].equals("ID"))
                tickerName = fieldsParsed[1][i];
        }
        
        Integer id = (Integer)ticker_id.get(tickerName);
        if (id == null) {
        		Log.println(Subsystem.DATA, null, "V1SocketDataSource: ignoring " + tickerName, Log.P_VERBOSE);
        		return;
        }
        
        Sonification s = sonifications.get(id);
        Config c = configs.get(id);
       
        if (s == null || c == null) {
        		Log.println(Subsystem.DATA, null, "V1SocketDataSource: Sonification is null", Log.P_VERBOSE);
            return;
        }
        
        Log.println(Subsystem.DATA, null, "V1SocketDataSource: processing data for: " + tickerName, Log.P_VERBOSE);
        Map<Key,String> points = new HashMap<Key,String>(fields.length-1);
       
        
        for (int i=0; i < fields.length; i++) {
        		if (fieldsParsed[0][i].equals("ID")) {
				continue;
        		}
        		String name = fieldsParsed[0][i];
			// Need to add error checking code here in case string is not a key
			Key key = Key.valueOf(name);
			points.put(key, fieldsParsed[1][i]);
        }

        DataComponent dc = new DataComponent();
        dc.setFields(points);
        // dc.setS_ID(id.intValue());
        
        s.update(dc);
    }

    /* 
     * Javadoc in superclass
     */
    public void connect() {  
        if (ds_thread != null && ds_thread.isAlive())
        		return;
        
        shouldStop = false;
        ds_thread = new Thread(new Runnable() {
        		public void run() {
    				runDataSource();
        		}
        });
        ds_thread.start();  
    }
    
    /* 
     * Javadoc in superclass
     */
    public void disconnect() {
    		shouldStop = true;
    		if (ds_thread == null)
    			return;
    		
        if (ds_thread.isAlive()) {
	        try {
	        		ds_thread.interrupt();
//	        		 close the socket since the thread is probably blocked on I/O
		        	socket.close();
				ds_thread.join();
			} catch (Exception e) {}
			ds_thread = null;
        }
    }
    
    /* 
     * Javadoc in superclass
     */
    public synchronized boolean addDataEntity(Config dataEntity, ID unused, Sonification sonification) {
        
    			String ticker = (String)dataEntity.getField(Key.TICKER);
    			Integer id = (Integer)dataEntity.getField(Key.S_ID);
    			if (ticker != null && id != null && sonification != null) {
    				ticker_id.put(ticker, id);
    				sonifications.put(id, sonification);
    				configs.put(id, dataEntity);

    			}	
            else
            		return false;
    			
            if(entitiesList != null)
                entitiesList = entitiesList + ",";
            else
                entitiesList = "";
            
            entitiesList += ticker;
            
//            if(mode == RUNNING || mode == STARTING) {
//                disconnect();
//                connect();
//            }
          
            Log.println(Subsystem.DATA, null, "V1SocketDataSource: adding : " + id + " to " + (String)config.getField(Key.DESCRIPTION), Log.P_INFO);
            return true;    
    }
    

    /* 
     * Javadoc in superclass
     */
    public synchronized void removeDataEntity(Integer ID) {
    		
    		if (ID != null) {
    			Config config = (Config)configs.get(ID);
    			String ticker = (String)config.getField(Key.TICKER);
    			Sonification s = (Sonification)sonifications.remove(ID);
    			configs.remove(ID);
    			
    			ticker_id.remove(ticker);
    			int position = entitiesList.indexOf(ticker) - 1;
             entitiesList = entitiesList.substring(0,position) + entitiesList.substring(position + ticker.length(),entitiesList.length()-1);
    		}
    }

}
