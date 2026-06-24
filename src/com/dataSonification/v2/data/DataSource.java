package com.dataSonification.v2.data;
import com.dataSonification.v2.Config;
import com.dataSonification.v2.ID;
import com.dataSonification.v2.sound.Sonification;

/**
 * Abstract class representing a data source.
 * @author Kimo Johnson
 */
public abstract class DataSource {
    
	
	/**
	 * Config object for this data source.
	 */
//	 package-private access because all subclasses are in the same package
	Config config;
	
	/**
	 * A lock to protect the config object.
	 */
	final Object configLock = new Object();

    boolean connected;
    boolean shutdown;
	
	//protected String id;
	
    /**
     * Adds a data entity to this data source.
     * @param config the configuration for the sonification
     * @param sonification the sonification to which the data should be sent
     * @return true if successful, false if not
     */    
    public abstract boolean addDataEntity(Config config, ID id, Sonification sonification);
    
    /**
     * Removes a data entity from this data source.
     * @param ID the identifier for the sonification to be removed
     */    
    public abstract void removeDataEntity(Integer ID);
    
    /**
     * Connects this data source.
     */    
    public abstract void connect();
  
    public boolean connected() {
        return connected;
    }
    
    public void shutdown() {
        disconnect();
        shutdown = true;
    }
    
    public boolean is_shutdown() {
        return shutdown;
    }
    
    /**
     * Disconnects this data source.
     */    
    public abstract void disconnect();
    
    
    
    /**
     * Sets the configuration for the datasource.
     * @param config the new configuration
     */    
    public void setConfig(Config config) {
    		synchronized(configLock) {
    			this.config = config;
    		}
    }
    
    public String toString() {
    		return "DataSource";
    }

}
