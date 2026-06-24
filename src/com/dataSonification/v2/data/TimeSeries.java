/*
 * TimeSeries.java
 *
 * Created on April 29, 2004, 3:56 PM
 */

package com.dataSonification.v2.data;

/**
 *
 * @author  Edward Childs
 */

 class TimeSeries {
    /**
     * ID of DataEntity that the TimeSeries is associated with
     */    
    private String id;
    
    /**
     * Array of DataItems, holds current data
     */    
    private DataPoint[] data;
    
    /**
     * Maximum # of past DataItems to keep in memory
     */    
    private final int MAX_DATA_SIZE = 100;
    
    /**
     * Counter for current position in data array
     */    
    private int currentPos = -1;
    
    /**
     * Name of this TimeSeries ie Price or Moving Average etc
     */    
    private String name;
    
    /**
     * Return value indicates unsuccesful update
     */    
    public static final int UPDATE_INVALID = 0;
    
    /**
     * Return value, indicates successful update
     */    
    public static final int UPDATE_SUCCESS = 1;
    
    /**
     * Creates a new instance of TimeSeries
     * @param tsName Name of TimeSeries to be created
     * @param tsID DataEntity ID that this TimeSeries should be associated with
     */
    public TimeSeries(java.lang.String tsName, java.lang.String tsID) {
    }
    
    /**
     * Adds new DataItem to timeSeries
     * @param newData new data to be added. should have same id and name as TimeSeries
     * @return Returns UPDATE_INVALID if invalid data,
     * UPDATE_SUCCESS if success
     */    
    public int addNewData(DataPoint newData) {
        return UPDATE_INVALID;
    }

    
}
