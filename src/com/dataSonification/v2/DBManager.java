package com.dataSonification.v2;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.ReturnCode;
import com.dataSonification.v2.util.Subsystem;


/**
 * Interfaces with a database.
 * @author Kimo Johnson
 *
 */
public class DBManager
{   
    /**
     * Initialization status.
     */
    private volatile boolean initialized = false;
    
    private Config config;
    
    /**
     * The connection to the database.
     */
    private Connection connection;
    
    
    /**
     * A list of tables available in the database.
     */
    private List<String> db_tables;
    
        
    public DBManager(Config c)
    {
        config = c;
        String driver = (String) config.getField(Key.DB_DRIVER);
        String name = (String) config.getField(Key.DB_NAME);
        String user = (String) config.getField(Key.DB_USER);
        String password = (String) config.getField(Key.DB_PASSWORD);
        
        // A null username or password is an empty string
        if (user == null)
        {
            user = "";
        }
        if (password == null)
        {
            password = "";
        }
        

        /*
         * If we're using ODBC, don't want to have to set up a DSN on each
         * installation, so call the driver with all the necessary params,
         * that is, use a DSN-less call.
         */
        if (driver.equals("sun.jdbc.odbc.JdbcOdbcDriver"))
        {
            if(name.endsWith(".xls"))
            {
                String origName = name;
                name = "jdbc:odbc:Driver={Microsoft Excel Driver (*.xls)};" +
                    "DBQ=" + origName + ";" +
                    "DriverID=790;READONLY=1";
        
            }
            else
            {
                name = "jdbc:odbc:" + name;
            }
       }

        config.setField(Key.DB_DRIVER, driver);
        config.setField(Key.DB_NAME, name);
        config.setField(Key.DB_USER, user);
        config.setField(Key.DB_PASSWORD, password);
    }
    
    public boolean initialize() {
        String driver = (String) config.getField(Key.DB_DRIVER);
        String name = (String) config.getField(Key.DB_NAME);
        String user = (String) config.getField(Key.DB_USER);
        String password = (String) config.getField(Key.DB_PASSWORD);
        
        /*
         * According to the MySQL Connector/J docs, the call to newInstance()
         * is a work-around from some broken Java implementations.
         */
        try {
            Class.forName(driver).newInstance();
            connection = DriverManager.getConnection(name, user, password);

            Log.println(Subsystem.DATA, null, "DBManager initialized:" + name, Log.P_INFO);
           
            initDBTables();
            initialized = true;
        } catch (Exception e ) {
            e.printStackTrace();
            initialized = false;
        }
       
        return initialized;
    }
     
    
    /**
     * Get a list of tables in the database
     */
    private void initDBTables()
    {
        db_tables = new LinkedList<String>();
        try
        {
            DatabaseMetaData dm = connection.getMetaData();
            ResultSet rs = dm.getTables(connection.getCatalog(),null,"%",null);
            ResultSetMetaData rsmd = rs.getMetaData();
            int nColumns = rsmd.getColumnCount();
            while (rs.next())
            {
                // Column 3 contains the table name
                String name = rs.getString(3);
                db_tables.add(name.toLowerCase());
            }
        }
        catch (SQLException e)
        {}
    }
    
    /**
     * Queries the database.
     * @param sql the query string
     * @return {@link DBResult} containing the result of the query
     * @throws SQLException if problems with query
     * @throws IllegalStateException if database is not initialized
     */
    public DBResult queryDB(String sql) throws SQLException
    {
        if (!initialized)
            throw new IllegalStateException();
        
        Statement st = null;
        ResultSet rs = null;
        String dbName = (String)config.getField(Key.DB_NAME);
	
        Log.println(Subsystem.DATA, null, "DBManager: queryDB " + dbName + ": " + sql, Log.P_ALL);
        
        synchronized (this)
        {
            st = connection.createStatement();
            rs = st.executeQuery(sql);
        }
        return new DBResult(st, rs);
    }
    
    /**
     * Updates the database.
     * @param sql the query string
     * @return true if successful, else false
     * @throws SQLException if problems with update
     * @throws IllegalStateException if database not initialized
     */
    public boolean updateDB(String sql) throws SQLException
    {
        if (!initialized)
            throw new IllegalStateException();
        
        Statement st = null;
        
        int i = -1;
        synchronized (this)
        {
            st = connection.createStatement();
            i = st.executeUpdate(sql);
        }
        
        if (st != null)
            st.close();
        
        return i != -1;
    }
    
    /**
     * Shuts down the connection to the database.
     * @throws SQLException if there is a problem closing the connection
     */
    public void shutdown()
    {
        try {
            if (connection != null && !connection.isClosed())
            {
                connection.close();
            }
        } catch (SQLException e) {
            Log.println(Subsystem.CORE,ReturnCode.GENERAL_WARNING,"DBManager caught exception in shutdown");
        }
    }
    

    public boolean isTable(String table)
    {
        if (null == table)
        {
            return false;
        }
        return db_tables.contains(table.toLowerCase());
    }
    
    public boolean equals(Object rhs) {
        if (!(rhs instanceof DBManager))
            return false;
        
        String x1 = (String) config.getField(Key.DB_DRIVER);
        String x2 = (String) config.getField(Key.DB_NAME);
        
        DBManager m = (DBManager) rhs;
        String y1 = (String) m.config.getField(Key.DB_DRIVER);
        String y2 = (String) m.config.getField(Key.DB_NAME);
        
        return x1.equals(y1) && x2.equals(y2);
    }
    
    public String getName() {
        return (String) config.getField(Key.DB_NAME);
    }
}