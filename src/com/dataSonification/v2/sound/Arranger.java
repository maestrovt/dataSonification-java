package com.dataSonification.v2.sound;

import java.util.Map;

import com.dataSonification.v2.util.Key;


/**
 * Abstract class outlining the Arranger component of a Sonification.
 * @author Kimo Johnson
 */
//package-private because all subclasses are in this package
abstract class Arranger extends SonificationComponent {
	
	/**
	 * Arrange using the shared cache for parameters.
	 * @return a sonifiable
	 */
	Sonifiable arrange() {
		return this.arrange(cache);
	}
	
	/**
	 * Creates an arrangement based on the parameters specified in the input Map.
	 * @param map provides the parameters for the arranger
	 * @return a sonifiable
	 */
	abstract Sonifiable arrange(Map<Key,Object> map);
	
}