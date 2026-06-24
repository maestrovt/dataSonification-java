package com.dataSonification.v2.data;

import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author Kimo Johnson
 *
 */
public enum InfoType {
	// prebuilt DataInfo objects
	EXTERNAL_STOP_RECEIVED,
	ERROR,
	UNRECOGNIZED,
	NO_INFO,
	UP_BASE,
	EVEN_BASE,
	DOWN_BASE,
	IGNORED,

	STRONG_OUTPERFORM,
	OUTPERFORM,
	UNDERPERFORM,
	WEAK_UNDERPERFORM,
	BREAK_LOW,
	BREAK_HIGH,

	CROSS;
	
	private static Map<String,InfoType> string_type;
	
	// static initializer to initialize string -> key mapping
	static {
		string_type = new HashMap<String,InfoType>();
		for (InfoType type : InfoType.values()) {
			string_type.put(type.toString().toUpperCase(),type);
		}
	}
	
	public static boolean isType(String t) {
	    return string_type.keySet().contains(t.toUpperCase());
	}

	public static InfoType typeForString(String s) {
		InfoType t = string_type.get(s.toUpperCase());

		if (t == null)
			throw new IllegalArgumentException(s);
		return t;
	}
}

