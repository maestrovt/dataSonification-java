package com.dataSonification.v2.util;

public enum ReturnCode {
	/*
	 * Used in SocketUI 
	 */
	NO_CODE (Type.WARNING),
	MESSAGE_COMPONENT_VALID (Type.SUCCESS),
	MESSAGE_COMPONENT_INVALID (Type.ERROR),
	CORE_INIT_SUCCESS (Type.SUCCESS),
	CORE_INIT_FAILED (Type.ERROR),
	CORE_START_SUCCESS (Type.SUCCESS),
	CORE_START_FAILED (Type.ERROR),
	CORE_IS_NULL (Type.ERROR),
	CONTROL_SUCCESS (Type.SUCCESS),
	CONTROL_FAILED (Type.ERROR),
    DB_ID         (Type.INFO),
    STATUS_SUCCESS (Type.SUCCESS),
    STATUS_FAILED (Type.ERROR),
    
	CORE_NULL_ERROR (Type.ERROR),
	DATA_SOURCE_NULL_ERROR (Type.ERROR),
	XML_PARSE_ERROR (Type.ERROR),
	
	RUNNING (Type.SUCCESS),
    STOPPED (Type.SUCCESS),
    INVALID (Type.ERROR),    
    
	//Data Return Codes
	DATA_COLUMN_MISMATCH (Type.ERROR),
	DATA_SQL_ERROR (Type.ERROR),
	DATA_TABLE_ERROR (Type.ERROR),
	
	//Sonification Return Codes
	DATA_FIELD_MISSING (Type.ERROR),
	SOUND_COMPONENT_MISMATCH(Type.ERROR),
    SONIFIED (Type.SUCCESS),
    NOT_SONIFIED (Type.SUCCESS),
	
    //General Codes for use w/ log
    GENERAL_SUCCESS (Type.SUCCESS),
    GENERAL_WARNING (Type.WARNING),
    GENERAL_ERROR (Type.ERROR);
    
    


        
        
	/**
	 * An enum representing the type of the ReturnCode.  Errors
	 * should map to negative numbers for easy parsing.
	 * @author kimo
	 */
	public enum Type { 
		ERROR (-1), 
		WARNING (0), 
		SUCCESS (1), 
		INFO (2);

		private int code;
		
		Type(int code) { 
            this.code = code; 
        }	
		
		public int getCode() { return code; }
	}

	
	
    private Type type;
    private String message;
        
    ReturnCode(Type type) {
        this.type = type;
        this.message = this.toString();
    }	
        
    ReturnCode(Type type, String message)
    {       
        this.type = type;
        this.message = message;
    }
    
   public String getMessage()
   {
       return message;
   }
   
   public Type getType()
   {
       return type;
   }
}