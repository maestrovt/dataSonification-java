package com.dataSonification.v2.data;

import com.dataSonification.v2.ID;

public class SystemObject {
    private int s_id;
    private int db_id;
    private String name;
    // This value must be negative.  Core assumes that a negative s_id indicates that
    // the value was not specified.
    private static final int INIT = -1;
    
    public SystemObject() {
        this(INIT,INIT,"");
    }
    
    public SystemObject(int db_val) {
        this(db_val, INIT, "");
    }
    
    public SystemObject(int db_val, int s_val) {
        this(db_val, s_val, "");
    }
    
    public SystemObject(int db_val, int s_val, String nm) {
        db_id = db_val;
        s_id = s_val;
        name = new String(nm);
    }
    
    public boolean isValid() {
        return db_id != INIT;
    }
    
    public boolean isCore() {
        return s_id == 0 && db_id == 0;
    }
    
    public void setS_ID(int val) {
        s_id = val;
    }
    
    public int getS_ID() {
        return s_id;
    }
    
    public void setDB_ID(int val) {
        db_id = val;
    }
    
    public int getDB_ID() {
        return db_id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String toXML() {
        String xml_string;
        if (isCore()) {
            xml_string = "<core/>";
        } else {
            xml_string = "<db_id>" + Integer.toString(db_id) + "</db_id>";
            if (s_id != INIT)
                xml_string += "<s_id>" + Integer.toString(s_id) + "</s_id>";
        }
        return xml_string;
    }
    
    public String toString() {
        if (isCore()) {
            return "CORE";
        } else {
            return "(" + db_id + "," + s_id + ")";
        }
    }

    public static final SystemObject CORE = new SystemObject(0,0,"core");
    
}