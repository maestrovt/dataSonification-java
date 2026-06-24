/*
 * DataPoint.java
 *
 * Created on May 2, 2004, 6:25 PM
 */

package com.dataSonification.v2.data;

/**
 *
 * @author  ben
 */
public class DataPoint {
    
    /**
     * timestamp (in milliseconds) of the point
     */    
    public final long time;
    
    /**
     * Name of TimeSeries with which this DataItem is associated
     */    
    public final String name;
    
    /**
     * ID of DataEntity with which this DataItem is associated
     */    
    public final Integer ID;
    
    /**
     * Value of the data at this point
     */    
    public final Object value;
    
    /**
     * Creates a new instance of DataItem
     * @param diTime TimeStamp of the new DataItem
     * @param diName Name of the new DataItem
     * @param diID ID of the new DataItem
     * @param diValue Value of the new DataItem
     */
    public DataPoint(long diTime, String diName, Integer diID, Object diValue) {
        time = diTime;
        name = new String(diName);
        ID = diID;
        value = diValue;   
    }
    
}
