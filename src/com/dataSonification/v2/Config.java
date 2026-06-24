package com.dataSonification.v2;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.dataSonification.v2.util.Key;


/**
 * Abstract class representing config data.
 * @author Kimo Johnson
 * @see Key
 */
//public because used by packages data and sonifications
public abstract class Config {

	/**
	 * The listeners that need to be notified when this config changes.
	 */
//	 package-private because all subclasses of Config are in the same package
	List<ChangeListener> listeners;
	
	public Config() {
		listeners = new LinkedList<ChangeListener>();
	}

	
	/**
	 * Returns the object for the specified key.
	 * @param key the key
	 * @return the object for the specified key
	 */
	public abstract Object getField(Key key);
	
	/**
	 * Sets the value for the specified key.
	 * @param key the key
	 * @param value the value to associate with the key
	 */
	public abstract boolean setField(Key key, Object value);
	
	/**
	 * Returns true if the database contains the key, false if not.
	 * @param key the key
	 * @return true if the key is in the database, false if not
	 */
	public abstract boolean containsKey(Key key);
	
	/**
	 * Merge two configs
	 * @param c the config being merged in
	 */
	public abstract void merge(Config c);
    
    /**
     * Reload this config from its source (db/file etc)
     */
    public abstract void reload();
	
	
	public void addChangeListener(ChangeListener c) {
		listeners.add(c);
	}

	public void removeConfigChangeListener(ChangeListener c) {
		listeners.remove(c);
	}
	
		
	/**
	 * Allows an object to be notified when its configuration changes.
	 * @author Kimo Johnson
	 */
	public interface ChangeListener {
		
	    /**
	     * Method that is called when configuration information changes.  The callee
	     * should query its config object to get the change.
	     * @param key the key for the value that changed
	     */
	    public void configChanged(Key key);
	    
	    public void configChanged(Map<Key,Object> m);
	}
}
