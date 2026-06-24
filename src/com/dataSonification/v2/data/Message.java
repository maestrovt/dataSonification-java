package com.dataSonification.v2.data;
import java.util.ArrayList;


public class Message {
    String direction = null;
    String version = null;
    ArrayList<MessageComponent> components;
    
    public Message() {
        components = new ArrayList<MessageComponent>();
    }
    
    public void addComponent(MessageComponent component) {
        components.add(component);
    }
    
    public void setDirection(String direction) { this.direction = direction; }
    public void setVersion(String version) { this.version = version; }
    
    public int size() {
        return components.size();
    }
    
    public MessageComponent getComponent(int index) {
        if (index >= components.size())
            throw new IllegalArgumentException();

        return (MessageComponent) components.get(index);  
    }
    
    public String toXML() {
        StringBuffer buf = new StringBuffer(512);
        buf.append("<message>\n");
        for (MessageComponent mc : components) {
            buf.append(mc.toXML());
        }
        buf.append("</message>");
        return buf.toString();
    }
}
