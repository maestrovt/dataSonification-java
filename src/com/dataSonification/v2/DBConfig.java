package com.dataSonification.v2;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

import com.dataSonification.v2.util.Converter;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.ReturnCode;
import com.dataSonification.v2.util.Subsystem;


/**
 * Config object that uses an SQL database to store configuration information.
 * @see DBManager
 * @author Kimo Johnson
 *
 */
public class DBConfig extends Config {
	
    //private DBManager dbm;
    
	/**
	 * A map to store primary keys and their values.
	 */
	private Map<Key,PrimaryInfo> primary;
	
	/**
	 * The keys available in this config object and the tables they come from.
	 */
	private Map<Key,TableInfo> keys;
	
	/**
	 * A cache of the values in the database.
	 */
	private Map<Key,Object> cache;
	
    //private String table;
    //private Key id_key;
    //private Integer id;
    
	/**
	 * Defines a config object for an object in an SQL database table.
	 * @param table the name of the table
	 * @param key the primary key for the table
	 * @param id the id for this config object
	 */
	public DBConfig(DBManager dbm, String table, Key id_key, Integer id) {
		
        primary = new EnumMap<Key,PrimaryInfo>(Key.class);
        primary.put(id_key, new PrimaryInfo(dbm, table, id));
             
        keys = new EnumMap<Key,TableInfo>(Key.class);
        load(id_key);
	}

	/* 
	 * Javadoc comment in superclass
	 */
	public Object getField(Key key) {
		// Check the cache first
		Object test = cache.get(key);
		if (test != null) {
			return test;
		}
		
		// Bypass a query if we are requesting id of primary key
        PrimaryInfo pi = primary.get(key);
		if (pi != null) {
			return pi.id;
		}
		
		/*
		 * Get the table and primary key
		 */
		TableInfo ti = keys.get(key);
		
		if (ti == null)
		    throw new IllegalArgumentException("DBConfig.getField: key " + key + " not found.");
		
		pi = primary.get(ti.primary);
		
		if (pi == null)
			throw new IllegalArgumentException("DBConfig.getField: cannot get primary key for " + key);
		String sql = "SELECT " + key + " FROM " + ti.table + " WHERE " + ti.table + "." + ti.primary + " = " + pi.id;

		DBResult result = null;
		Object obj = null;
		try {
			result = pi.dbm.queryDB(sql);
			if (result != null) {
				result.rs.next();
				obj = result.rs.getObject(1);				
				result.st.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.println(Subsystem.DATA, ReturnCode.DATA_SQL_ERROR, "DBConfig.getField() caught exception on query: " + sql);
		}
		
		/*
		 * Make sure that the object from the database is the correct type
		 */
		obj = convert(key, obj);
		
		// Cache non-null results
		if (obj != null) {
			cache.put(key, obj);
		}
				
		return obj;
	}
	

	private Object convert(Key key, Object obj) {
		if (obj == null) {
			return obj;
		}
		Class type = key.getType();
	    if (obj.getClass() != type) {
	        obj = Converter.convert(obj, type);
	    }
	    return obj;
	}

	/* 
	 * Javadoc comment in superclass
	 */
	public boolean setField(Key key, Object value) {
		// Get the table for this key to make sure the key is valid for the object
		TableInfo ti = keys.get(key);
		if (ti == null) {
			return false;
		}
		
		// Convert the object just in case
		value = convert(key, value);
		
		if (value == null) {
			return false;
		}

		cache.put(key, value);
        
//        if(key == Key.INST_ID)
//        {
//           reloadInstrument();
//        }
		return true;
	}
	
	/**
	 * This method needs to be called at some point.
	 * @param key
	 * @param value
	 * @return
	 */
	private boolean writeBack(Key key, Object value) {
		// Get the table for this key
		TableInfo ti = keys.get(key);
		PrimaryInfo pi = primary.get(ti.primary);
		
		if (pi == null) {
			Log.println(Subsystem.CORE,ReturnCode.GENERAL_WARNING,"DBConfig.setField() cannot find primary key for " + key, Log.P_ERROR);
			return false;
		}
		
		String sql = "UPDATE " + ti.table + " SET " + key + " = " + value;
		sql += " WHERE " + ti.primary + " = " + pi.id;
		Log.println(Subsystem.DATA, ReturnCode.NO_CODE, "DBConfig.setField query: " + sql, Log.P_INFO);
		try {
			return pi.dbm.updateDB(sql);
		} catch (SQLException e) {
			return false;
		}
	}


	/* 
	 * Javadoc comment in superclass
	 */
	public boolean containsKey(Key key) {
		return keys.containsKey(key);
	}
	
	/*
	 * Javadoc comment in superclass.
	 */
	public void merge(Config c) {
		if (!(c instanceof DBConfig)) {
			Log.println(Subsystem.CORE,ReturnCode.GENERAL_WARNING, "DBConfig: cannot merge non-DBConfig", Log.P_ERROR);
			return;
		}
		
		DBConfig db = (DBConfig) c;
		        
		// First delete any existing data for this primary key
		for (Key primary_key : db.primary.keySet())
			prunePrimaryKey(primary_key);
		
		// Merge info from other DBConfig
		primary.putAll(db.primary);
		keys.putAll(db.keys);
        
	}
    
//    private void reloadInstrument()
//    {
//        try
//        {
//            Integer inst_id = (Integer) getField(Key.INST_ID);
//            // Get instrument config         
//            // redesign...this won't work
//            Config inst_config = new DBConfig(dbm, "AllInstruments", Key.INST_ID, inst_id);
//            merge(inst_config);
//            
//        }
//        catch(Exception e)
//        {
//            //Ok no instrument id...
//        } 
//    }
    
    public void reload() {
        cache.clear();
        keys.clear();
        
        for (Key key : primary.keySet()) {
            load(key);
        }
        
    }
    
    private void load(Key id_key)
    {
        PrimaryInfo pi = primary.get(id_key);
        
        String where = pi.table + "." + id_key + " = " + pi.id;
        String tables = pi.table;
		
        int col, nColumns;
        try {
            String sql = "SELECT * FROM " + tables + " WHERE " + where;
            Log.println(Subsystem.DATA, null, sql, Log.P_VERBOSE);
            DBResult result = pi.dbm.queryDB(sql);
            ResultSetMetaData md = result.rs.getMetaData();
            nColumns = md.getColumnCount();
           while (result.rs.next()) {
                for (col = 1; col <= nColumns; col++) {
                    int typeCode = md.getColumnType(col);
                    
                    String tableName = result.rs.getString(col);
                    boolean isTable = pi.dbm.isTable(tableName);
                    
					/* 
					 * If typeCode is VARCHAR and the DBManager
					 * has a mapping for this string to a class name,
					 * then this class may contain config data.
					 */
					if (typeCode == Types.VARCHAR && isTable) {
						tables += "," + tableName;
						where += " AND " + pi.table + "." + id_key + " = " + tableName + "." + id_key;
					}
                             
				}
			}
			result.st.close();
			
			TableInfo ti = new TableInfo(id_key, pi.table);
            
			// Now get the list of keys for all the tables
			sql = "SELECT * FROM " + tables + " WHERE " + where;
			result = pi.dbm.queryDB(sql);
			md = result.rs.getMetaData();
			nColumns = md.getColumnCount();
			for (col = 1; col <= nColumns; col++) {
				String column_name = md.getColumnName(col);
				
				try {
					Key k = Key.valueOf(column_name.toUpperCase());
					// Map the primary key to the main table even though it is in every table
					if (k == id_key) {
						keys.put(k, ti);
					} else {
						keys.put(k, new TableInfo(id_key, md.getTableName(col)));
					}
				} catch (IllegalArgumentException e) {
					// Ignore columns that do not map to keys
					Log.println(Subsystem.DATA, ReturnCode.DATA_COLUMN_MISMATCH, "DBConfig: found column that does not map to key: " + column_name, Log.P_ERROR);
				}
			
			}
						
			result.st.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.println(Subsystem.DATA, ReturnCode.GENERAL_ERROR, "DBConfig caught an exception." + e, Log.P_ERROR);
		}

		cache = new EnumMap<Key,Object>(Key.class);
              
        /**
         *
         *  This is a bit of a hack, eventually we should
         *  redesign the database so that this can be automatic
         *
         *  How do we link INST_ID to "AllInstruments"... right now
         *  we can't
         *
         */
//        if (id_key == Key.S_ID)
//        {
//            try
//            {
//                //Load istrument if it exists...
//                Integer inst_id = (Integer) getField(Key.INST_ID);
//
//                // Get instrument config            
//                if (inst_id != null) {
//                    // redesign...this won't work
//                    Config inst_config = new DBConfig(dbm, "AllInstruments", Key.INST_ID, inst_id);
//                    merge(inst_config);
//                }
//            }
//            catch(Exception e)
//            {
//                //Ok no instrument id...
//            }
//        }
        
    }
	
	/**
	 * Remove all keys associated with this primary key
	 * @param key
	 */
	private void prunePrimaryKey(Key key) {
		if (!primary.containsKey(key)) {
			return;
		}
		
		// Should really lock the object for this
		for (Iterator<Key> it = keys.keySet().iterator(); it.hasNext(); ) {
			Key k = it.next();
			TableInfo tableInfo = keys.get(k);
			if (tableInfo.primary == key) {
				cache.remove(k);
				it.remove();
			}
		}
		
		primary.remove(key);
	}
	
	
	/**
	 * A container for info used to query DB.
	 * @author kimo
	 *
	 */
	private static class TableInfo {
		public final Key primary;
		public final String table;
		public TableInfo(Key primary, String table) {
			this.primary = primary;
			this.table = table;
		}
	}
    
    private static class PrimaryInfo {
        public final Integer id;
        public final DBManager dbm;
        public final String table;
        public PrimaryInfo(DBManager dbm, String table, Integer id) {
            this.dbm = dbm;
            this.table = table;
            this.id = id;
        }
    }
}
