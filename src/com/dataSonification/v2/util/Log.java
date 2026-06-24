package com.dataSonification.v2.util;

import com.dataSonification.v2.Const;
import com.dataSonification.v2.Core;

/**
 * Utility class to capture logging info.  The amount of info can be controlled by
 * setting the log level.
 * @author Kimo Johnson
 *
 */
public class Log
{
    
    public static final LogCode P_ALL = LogCode.L_ALL;
    public static final LogCode P_VERBOSE = LogCode.L_VERBOSE;
    public static final LogCode P_INFO = LogCode.L_INFO;
    public static final LogCode P_SUCCESS = LogCode.P_SUCCESS;
    public static final LogCode P_WARNING = LogCode.P_WARNING;
    public static final LogCode P_ERROR = LogCode.P_ERROR;
    
    private static boolean notifyCore = true;
    
    public enum LogCode
    {
        P_ERROR(-1),
        P_WARNING(0),
        P_SUCCESS(1),
        L_INFO(2),
        L_VERBOSE(3),
        L_ALL(4);
        
        int code;
        private LogCode(int code)
        {
            this.code = code;
        }
        
        public int getCode()
        {
            return code;
        }
        
        public static LogCode forCode(int code)
        {
            switch(code)
            {
            case -1:
                return P_ERROR;
            case 0:
                return P_WARNING;
            case 1:
                return P_SUCCESS;
            case 2:
                return L_INFO;
            case 3:
                return L_VERBOSE;
            case 4:
                return L_ALL;
            default:
                return null;
            }
        }
    }
    
    // Suppress default constructor for noninstantiability
    private Log()
    {}
    
    /**
     * Logs a String if the priority is lower that a specified threshold.
     * @param message the String to log
     * @param priority the importance of this String
     */
    public static void println(Subsystem s, ReturnCode rc, String message, LogCode priority)
    {
       
        if(notifyCore && priority.getCode() <= P_WARNING.getCode())
        {
            Core.statusReport(s, rc, message);
        }
        
        if(Const.LOG_SUPPRESS_SYSTEMS != null)
        {
            for(Subsystem subs : Const.LOG_SUPPRESS_SYSTEMS)
            {
                if(s == subs)
                    return;
            }
        }
       
        
        if (priority.getCode() <= Const.LOG_LEVEL.getCode())
            System.out.println(s + ": " + message);
    }
    
    /**
     *Set if core should be notified of P_SUCCESS/P_WARNING/P_ERROR
     */
    public static void setCoreNotification(boolean coreNotification)
    {
       notifyCore = coreNotification; 
    }
    /**
     * Logs a message with default priority.
     * @param message
     */
    public static void println(Subsystem s, ReturnCode rc, String message)
    {
        Log.println(s, rc, message, P_INFO);
    }
    
    
}