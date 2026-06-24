package com.dataSonification.v2.data;

public class StatusComponent extends MessageComponent {
    ActionType action;
    
    public StatusComponent() {
        action = null;
        object = null;
    }
    
    public void setAction(ActionType action) {
        this.action = action;
    }
    
    public ActionType getAction() {
        return action;
    }
    
    public String toXML() {
        StringBuffer buf = new StringBuffer(512);
        buf.append("<status>\n<sequence>");
        buf.append(Long.toString(sequence));
        buf.append("</sequence>\n<action>");
        buf.append(action.toString());
        buf.append("</action>\n<object>");
        buf.append(object.toXML());
        buf.append("</object>\n");
        buf.append(fieldsStampsXML());
        buf.append("</status>\n");
        return buf.toString();
    }
    
    public String toString() {
        return "StatusComponent: " + action.toString();
    }
    
    public boolean isValid() {
        return (action != null) && (object != null) && object.isValid();
    }
}