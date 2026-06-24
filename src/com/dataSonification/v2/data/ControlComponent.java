package com.dataSonification.v2.data;


public class ControlComponent extends MessageComponent {
    ActionType action;
    
    public ControlComponent() {
        action = null;
        object = null;
    }
    public void setAction(ActionType action) {
        this.action = action;
    }
    public ActionType getAction() {
        return action;
    }
    
    public String toString() {
        return "ControlComponent: " + action.toString() + " -> " + object.toString();
    }
    
    public String toXML() {
        StringBuffer buf = new StringBuffer(512);
        buf.append("<control>\n<sequence>");
        buf.append(Long.toString(sequence));
        buf.append("</sequence>\n<action>");
        buf.append(action.toString());
        buf.append("</action>\n<object>");
        buf.append(object.toXML());
        buf.append("</object>\n");
        buf.append(fieldsStampsXML());
        buf.append("</control>\n");
        return buf.toString();
    }
    
    public boolean isValid() {
        return (action != null) && (object != null) && object.isValid();
    }
}