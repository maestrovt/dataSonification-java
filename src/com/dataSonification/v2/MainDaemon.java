package com.dataSonification.v2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dataSonification.v2.ui.SocketUI;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.ReturnCode;
import com.dataSonification.v2.util.Subsystem;

public class MainDaemon implements Runnable {
    private static final int PORT = 9001;
    private SocketUI handler = null;
    private ServerSocket server;
    private Map<Key,String> arg_map;
	
    private volatile boolean shouldStop = false;
        
    private static final Object classLock = MainDaemon.class;
    
    private static MainDaemon daemon = null;
    
    private Thread daemon_thread = null;
    
    private List<SocketUI> handlers;
    
    public static MainDaemon instance() {
    		return instance(new HashMap<Key,String>());
    }
    
    public static MainDaemon instance(Map<Key,String> m) {
    		synchronized(classLock) {
    			if (daemon == null) {
    				daemon = new MainDaemon(m);
    			}
    			return daemon;
    		}
    }
	
    private MainDaemon(Map<Key,String> m) {
		arg_map = m;
		handlers = new ArrayList<SocketUI>();
	}
	
    public void run() {
		int port = 0;
		if (arg_map.containsKey(Key.PORT)) {
			try {
				port = Integer.parseInt(arg_map.get(Key.PORT));
			} catch (NumberFormatException e) {
				port = 0;
			}
		}

		port = (port == 0) ? PORT : port;

		try {
			// Start the server socket
            server = new ServerSocket(port);
            System.out.println("RUNNING on port " + port);
            while(!shouldStop) {
                try {
                    Socket connection = server.accept();

                    Log.println(Subsystem.CORE, null, Const.getVersionString());
                    handler = new SocketUI(connection);
                    handler.start();
                    handlers.add(handler);

                } catch (Exception e) {
                    Log.println(Subsystem.CORE, ReturnCode.GENERAL_ERROR, "MainDaemon: caught exception accepting connection or socket closed.");
                }
            }
            
        } catch (IOException e) {
			System.out.println("FAILED on port " + port);
        } finally {
            try { if(server != null) server.close(); } catch (IOException e) {}
            System.exit(0);
        }
    }
    
    public void start() {
    		shouldStop = false;
    		daemon_thread = new Thread(this);
    		daemon_thread.start();
    		
    }
    
    public void stop() {
    		shouldStop = true;
    		
    		for (SocketUI handler : handlers) {
    			handler.stop();
    		}
    		
    		Log.println(Subsystem.CORE, ReturnCode.GENERAL_WARNING, "MainDaemon: Closing socket", Log.P_INFO);
    		try {
			server.close();
		} catch (IOException e) {}

    }
    
    public void removeSocketUI(SocketUI ui)
    {
        handlers.remove(ui);
        if(handler == ui)
        {
            handler = null;
        }
        
    }

    public static void main(String[] args) {
		Map<Key,String> m = Main.processArgs(args);
		MainDaemon md = MainDaemon.instance(m);
		md.start();
    }
    
}