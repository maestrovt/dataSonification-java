package com.dataSonification.v2;

public class ID {
    public enum type {
        Sonification,
        DataSource
    };
    
    private final int db;
    private final int id;
    private final type t;
    
    public ID(int c_val, int s_val, type t_val) {
        db = c_val;
        id = s_val;
        t = t_val;
    }
    
    public boolean equals(Object rhs) {
        if ((rhs == null) || (rhs.getClass() != this.getClass()))
            return false;
        
        ID tmp = (ID)rhs;
        return db == tmp.db && id == tmp.id && t == tmp.t;
    }
    
    public int hashCode() {
        int result = 17;
        result = 37 * result + db;
        result = 37 * result + id;
        return result;
    }
    
    public int getDB_ID() {
        return db;
    }
    
    public int getID() {
        return id;
    }
    
    public type getType() {
        return t;
    }
    
    public String toString() {
        return "(" + Integer.toString(db) + "," + Integer.toString(id) + ")";
    }
}
