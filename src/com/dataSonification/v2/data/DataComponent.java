package com.dataSonification.v2.data;

import com.dataSonification.v2.util.Converter;
import com.dataSonification.v2.util.Key;

public class DataComponent extends MessageComponent {

    public DataComponent() {
        super();
        object = new SystemObject();
    }
    
    public String toXML() {
        StringBuffer buf = new StringBuffer(512);
        buf.append("<data>\n<sequence>");
        buf.append(Long.toString(sequence));
        buf.append("</sequence>\n");
        buf.append("<object>");
        buf.append(object.toXML());
        buf.append("\n</object>");
        buf.append(fieldsStampsXML());
        buf.append("</data>\n");
        return buf.toString();
    }

    public String toString() {
        return "DataComponent";
    }
   

    public Object getField(Key key) {
        Object value = null;
        
        if (fields != null) {
            value = fields.get(key);
        }
        
        if (value == null) {
            throw new IllegalArgumentException();
        }
        
		// Convert the value to the appropriate type
		Class type = key.getType();
        if (value.getClass() != type) {
            value = Converter.convert(value, type);
        }
        return value;
    }

    public boolean isValid() {
        return (fields != null) && (fields.size() > 0) && object.isValid();
    }

}