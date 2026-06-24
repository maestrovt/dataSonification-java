package com.dataSonification.v2.data;

import java.util.EmptyStackException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Stack;

import com.dataSonification.v2.Core;
import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.ReturnCode;
import com.dataSonification.v2.util.Subsystem;

public abstract class MessageComponent {
    long sequence;
    Map<Key,String> fields;
    Stack<Stamp> stamps;
    SystemObject object;
    
    ReturnCode.Type stampStatus = ReturnCode.Type.SUCCESS;
    
    public MessageComponent() {
        sequence = 0;
        fields = new EnumMap<Key,String>(Key.class);
        stamps = new Stack<Stamp>();
    }
    
    public void setSequence(long sequence) {
        this.sequence = sequence;
    }
    
    public void setFields(Map<Key,String> fields) {
        this.fields = fields;
    }
    
    public Map<Key,String> getFields() {
        return fields;
    }
    
    public void setObject(SystemObject obj) {
        this.object = obj;
    }
    
    public SystemObject getObject() {
        return object;
    }
    
    public void stamp(Subsystem s, ReturnCode rc, String message) {
        // make defensive copy
        if(rc.getType() == ReturnCode.Type.ERROR)
            stampStatus = ReturnCode.Type.ERROR;
        if(rc.getType() == ReturnCode.Type.WARNING 
                && stampStatus == ReturnCode.Type.SUCCESS)
            stampStatus = ReturnCode.Type.WARNING;
        
        stamps.push(new Stamp(s, rc, message));
    }
    
    public Stamp getStamp(ReturnCode rc)
    {
        for(Stamp s : stamps)
        {
            if(s.returncode == rc)
                return s;
        }
        return null;
    }
    

    public void finished() {
        Core c = Core.instance();
        if(c != null)
            c.completedMessage(this);
    }
    
    public abstract String toXML();
    
    public String fieldsStampsXML() {
		// Generates fields XML and stamps XML
		StringBuffer buf = new StringBuffer(256);
        if (!fields.isEmpty()) {
            buf.append("<fields>\n");
            for (Key key : fields.keySet()) {
                String value = fields.get(key);
                //<field key="%s">%s</field>
                buf.append("<field key=\"");
                buf.append(key.toString());
                buf.append("\">");
                buf.append(value);
                buf.append("</field>\n");
            }
            buf.append("</fields>\n");
        }
        
        buf.append("<stamps status=\"");
        buf.append(stampStatus);
        buf.append("\">\n");
        
        Stack<Stamp> usedStamps = new Stack<Stamp>();
        try
        {
            Stamp s;
            while(true)
            {
                s = stamps.pop();
                buf.append("<stamp key=\"");
                buf.append(s.subsystem);
                buf.append("\" status=\"");
                buf.append(s.returncode.getType());
                buf.append("\" return=\"");
                buf.append(s.returncode);
                buf.append("\">");
                buf.append(s.message);
                buf.append("</stamp>\n");
                usedStamps.push(s);
            }
        }
        catch(EmptyStackException e)
        {            
            buf.append("</stamps>\n");  
        }      
        
        try
        {
            while(true)
            {
              stamps.push(usedStamps.pop());
            }
        }
        catch(EmptyStackException e)
        {            

        }    
        
        return buf.toString();
    }
    
    
    public abstract boolean isValid();
    
    /**
     *Internal data storage class for stamps
     */
    public static class Stamp
    {
        public Subsystem subsystem;
        public ReturnCode returncode;
        public String message;
        
        public Stamp(Subsystem s, ReturnCode c, String message)
        {
            this.subsystem = s;
            this.returncode = c;
            this.message = message;
            if(message == null)
                this.message = c.getMessage();
        }
    
    }
}