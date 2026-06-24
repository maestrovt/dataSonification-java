package com.dataSonification.v2.sound;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.dataSonification.v2.util.Key;


/**
 * Superclass for the Analyzer and Arranger sonification components.
 * @author Kimo Johnson
 *
 * @see Analyzer
 * @see Arranger
 */
//package-private because all subclasses are in this package
class SonificationComponent {

	
	/**
	 * The keys that this component reads.
	 */
	List<Key> reads;

	/**
	 * The keys that this component writes.
	 */
	List<Key> writes;
	
	/**
	 * Cache for sharing parameters with another SonificationComponent.
	 */
	Map<Key,Object> cache;
	
	/**
	 * Contains default settings for parameters that the component reads.
	 */
	Map<Key,Object> defaults;
	
	SonificationComponent() {
		reads = new LinkedList<Key>();
		writes = new LinkedList<Key>();
		defaults = new EnumMap<Key,Object>(Key.class);
	}
	
	
	/**
	 * Sets the shared cache for this component.
	 * @param cache a shared cache
	 */
	void setCache(Map<Key,Object> cache) {
		this.cache = cache;
	}

	/**
	 * Returns the config keys that this component reads as a list.
	 * @return the read list
	 */
	List<Key> readList() {
		return reads;
	}
	
	
	/**
	 * Returns the keys that this component writes as a list.
	 * @return
	 */
	List<Key> writeList() {
		return writes;
	}
	
	/**
	 * Returns true if the key is in the write list, else false.
	 * @param key the requested key
	 * @return true or false
	 */
	boolean keyInWrites(Key key) {
		return writes.contains(key);
	}
	
	/**
	 * Returns true if the key is in the read list, else false.
	 * @param key the requested key
	 * @return true or false
	 */
	boolean keyInReads(Key key) {
		return reads.contains(key);
	}
	
	
	/**
	 * Returns true if the key is in the defaults list, else false.
	 * @param key the requested key
	 * @return true or false
	 */
	boolean keyInDefaults(Key key) {
		return defaults.containsKey(key);
	}
	
	/**
	 * Initializes the cache with default values.
	 */
	void initCache() {
		for (Key key : defaults.keySet()) {
			cache.put(key, defaults.get(key));
		}
	}
}