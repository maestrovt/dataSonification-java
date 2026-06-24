package com.dataSonification.v2.data;



/**
 * Encapsulates the result of Analyzer.analyze(). 
 * @author Kimo Johnson
 */
public class DataInfo {

	/**
	 * The additional information about the analysis results.
	 */
	private final String info;
    
	/**
	 * The type of additional information.
	 */
	public final InfoType type;
	

	/**
	 * Constructor for DataAction.  Usually want to use prebuilt DataActions.
	 * @param type the type of action
	 * @param info the additional information
	 */
	public DataInfo(InfoType type, String info) {
		this.type = type;
		this.info = info;
	}
	
	public String getInfo() {
	    return info;
	}
	
	// prebuilt DataActions
	public static final DataInfo SONIFIED = new DataInfo(InfoType.NO_INFO, null);
	public static final DataInfo IGNORED  = new DataInfo(InfoType.NO_INFO, null);
	public static final DataInfo STOP = new DataInfo(InfoType.EXTERNAL_STOP_RECEIVED, null);
	public static final DataInfo UP_BASE = new DataInfo(InfoType.UP_BASE, null);
	public static final DataInfo DOWN_BASE = new DataInfo(InfoType.DOWN_BASE, null);
	public static final DataInfo EVEN_BASE = new DataInfo(InfoType.EVEN_BASE, null);
	
	public static final DataInfo WEAK_UNDERPERFORM = new DataInfo(InfoType.WEAK_UNDERPERFORM, null);
	public static final DataInfo UNDERPERFORM = new DataInfo(InfoType.UNDERPERFORM, null);
	public static final DataInfo OUTPERFORM = new DataInfo(InfoType.OUTPERFORM, null);
	public static final DataInfo STRONG_OUTPERFORM = new DataInfo(InfoType.STRONG_OUTPERFORM, null);
	
	public static final DataInfo BREAK_LOW = new DataInfo(InfoType.BREAK_LOW, null);
	public static final DataInfo BREAK_HIGH = new DataInfo(InfoType.BREAK_HIGH, null);
	
	public static final DataInfo CROSS = new DataInfo(InfoType.CROSS, null);
	
	
	
}