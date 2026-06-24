package com.dataSonification.v2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.dataSonification.v2.sound.JavaGM;
import com.dataSonification.v2.util.Converter;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.ReturnCode;
import com.dataSonification.v2.util.Subsystem;

/**
 * A config object that reads and stores configuration information to files.
 * @author Kimo Johnson
 */
public class FileConfig extends Config {

	/**
	 * Internal storage for the fields.
	 */
	private Map<Key,Object> table;

	public FileConfig() {
		super();
		table = new HashMap<Key,Object>();
	}

	/**
	 * Creates a new Config object and loads data from a file.
	 * @param init the file to load from
	 * @throws IOException if there are problems loading from the file
	 */
	public FileConfig(BufferedReader init) throws IOException {
		this();
		loadFromFile(init);
	}

	/* 
	 * Javadoc in superclass
	 */
	public Object getField(Key key) {
		Object fieldValue = null;
		
		/*
		 * redesign...this is a bit ugly but sample instruments
		 * have names and midi instruments have programs that 
		 * need to get mapped to names.
		 */
		if (key == Key.NAME && table.containsKey(Key.PROGRAM)) {
		    int program = ((Integer)table.get(Key.PROGRAM)).intValue();
		    return JavaGM.patch[program];
		}
		
		fieldValue = table.get(key);
	
		/*
		 * Make sure that the object from the database is the correct type
		 */
		if (null != fieldValue) {
		    Class type = key.getType();
		    if (fieldValue.getClass() != type) {
		        fieldValue = Converter.convert(fieldValue, type);
		    }
		} else {
		    Log.println(Subsystem.DATA, null, "FileConfig.getField is null for fieldName: " + key.toString(), Log.P_INFO);
		}
		
		return fieldValue;
	}

	/* 
	 * Javadoc in superclass
	 */
	public boolean setField(Key key, Object fieldValue) {

		boolean retval = false;
		try {
			table.put(key, fieldValue);
			retval = true;
		} catch (Exception e) {}

		if (retval) {
			// Notify the ConfigChangeListeners
			for (int i = 0, n = listeners.size(); i < n; i++) {
				((ChangeListener) listeners.get(i)).configChanged(key);
			}
		}
		return retval;
	}

	/**
	 * Saves the configuration to a file.
	 * 
	 * @param file the file where the configuration will be saved
	 * @throws IOException if unable to write to the file
	 * @return Returns SUCCESS or FAILURE
	 */
	public int saveToFile(java.io.File file) throws IOException {

		BufferedWriter w = new BufferedWriter(new FileWriter(file));
		for (Key key : table.keySet() ) {
			Object value = table.get(key);
			w.write(key.toString() + ":" + value.getClass() + ":"
					+ value.toString());
			w.write("\r\n");
		}
		w.close();
		return Const.SUCCESS;
	}

	/**
	 * Load a configuration from a file.
	 * 
	 * @param r A BufferedReader for the config file
	 * @return Returns SUCCESS or FAILURE
	 * @throws IOException if unable to read the file
	 */
	public int loadFromFile(BufferedReader r) throws IOException {
		int commentIndex;
		String line, name, type, value;
	
		for (line = r.readLine(); line != null; line = r.readLine()) {
			commentIndex = line.indexOf('#');
			if (commentIndex == 0)
				continue;
			else if (commentIndex > 0)
				line = line.substring(0,commentIndex);
			
			StringTokenizer t = new StringTokenizer(line, "|");
			if (t.countTokens() < 3)
				continue;
			
			name = t.nextToken().trim();
			type = t.nextToken().trim();
			value = t.nextToken().trim();

			Key k = Key.valueOf(name.toUpperCase());
			Log.println(Subsystem.DATA, null, "field: " + name + " value: " + value, Log.P_ALL);
			try {
				/*
				/* Integer, Double, Boolean, and String all have a constructor
				 * that takes a string as input.
				 */ 
				Constructor<?> c = Class.forName(type).getConstructor(new Class[] {String.class});
				Object o = c.newInstance((Object[])(new String[] {value}));
				table.put(k, o);
			}
			catch (Exception e) {
				e.printStackTrace();
				Log.println(Subsystem.DATA, ReturnCode.GENERAL_ERROR, "Exception in loadFromFile" + e, Log.P_ERROR);
				table.put(k, value);
			}
		}
		r.close();
		return Const.SUCCESS;
	}

	/*
	 * Javadoc in superclass
	 */
	public boolean containsKey(Key key) {
		return table.containsKey(key);
	}

	/*
	 * Javadoc in superclass
	 */
	public void merge(Config c) {
		if (!(c instanceof FileConfig)) {
			Log.println(Subsystem.CORE,ReturnCode.GENERAL_WARNING,"FileConfig cannot merge non-FileConfig.", Log.P_ERROR);
			return;
		}
		
		FileConfig fc = (FileConfig) c;
		
		table.putAll(fc.table);
	}
    
    public void reload()
    {
        //Nothing here right now... shouldn't be necessary
    }
    
		
}
