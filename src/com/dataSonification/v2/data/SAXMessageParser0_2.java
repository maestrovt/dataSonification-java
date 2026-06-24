package com.dataSonification.v2.data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.dataSonification.v2.util.Key;
import com.dataSonification.v2.util.Log;
import com.dataSonification.v2.util.ReturnCode;
import com.dataSonification.v2.util.Subsystem;
public class SAXMessageParser0_2 extends DefaultHandler {

    private static final boolean DEBUG = false;
    private Message message;

    private Stack<Object> stack;

    private boolean isStackReadyForText;

    private Locator locator;

    public SAXMessageParser0_2() {
        stack = new Stack<Object>();
        isStackReadyForText = false;
    }

    public void setDocumentLocator(Locator rhs) {
        locator = rhs;
    }

    public void startElement(String uri, String nullName, String local,
            Attributes attribs) {

        isStackReadyForText = false;

        // if next element is complex, push a new instance on the stack
        // if element has attributes, set them in the new instance
        if (local.equals("control")) {
            if (DEBUG) System.out.println("start: control");
            stack.push(new ControlComponent());

        } else if (local.equals("data")) {
            if (DEBUG) System.out.println("start: data");
            stack.push(new DataComponent());

        } else if (local.equals("status")) {
            if (DEBUG) System.out.println("start: status");
            stack.push(new StatusComponent());

        } else if (local.equals("object")) {
            if (DEBUG) System.out.println("start: object");
            stack.push(new SystemObject());

        } else if (local.equals("fields")) {
            if (DEBUG) System.out.println("start: fields");
            stack.push(new HashMap<Key,String>());
        } else if (local.equals("field")) {
            if (DEBUG) System.out.println("start: field");
            String tmp = resolveAttrib(uri, "key", attribs, "none");

            Object key;
            try {
                key = Key.valueOf(tmp.toUpperCase());
            } catch (IllegalArgumentException e) {
                Log.println(Subsystem.DATA, ReturnCode.XML_PARSE_ERROR, "SAXParser: found bad key: " + tmp, Log.P_ERROR);
                key = Key.BAD_KEY;
            }
            stack.push(key);
            stack.push(new StringBuffer());
            isStackReadyForText = true;
        } else if (local.equals("message")) {
            if (DEBUG) System.out.println("start: message");
            stack.push(new Message());
            String tmp = resolveAttrib(uri, "direction", attribs, "unspecified");
            ((Message) stack.peek()).setDirection(tmp);
            tmp = resolveAttrib(uri, "version", attribs, "0");
            ((Message) stack.peek()).setVersion(tmp);
        }
        // if next element is simple, push StringBuffer
        // this makes the stack ready to accept character text
        else if (local.equals("sequence")|| local.equals("action") 
                || local.equals("s_id") || local.equals("db_id")) {
            if (DEBUG) System.out.println("start: sequence, request, action, s_id, or db_id");
            stack.push(new StringBuffer());
            isStackReadyForText = true;
        } else if (local.equals("core")) {
            stack.push(null);
        }// if none of the above, it is an unexpected element
        else {
            System.out.println("unexpected element: " + uri + ", " + local
                    + ", " + nullName);
            // do nothing
        }
    }

public void endElement(String uri, String nullName, String local) {

        // recognized text is always content of an element
        // when the element closes, no more text should be expected
        isStackReadyForText = false;

        // pop stack and add to 'parent' element, which is next on the stack
        // important to pop stack first, then peek at top element!
        Object tmp = stack.pop();

        if (local.equals("message")) {
            if (DEBUG) System.out.println("end: message");
            message = (Message) tmp;

        } else if (local.equals("control")) {
            if (DEBUG) System.out.println("end: control");
            ((Message) stack.peek()).addComponent((ControlComponent)tmp);

        } else if (local.equals("data")) {
            if (DEBUG) System.out.println("end: data");
            ((Message) stack.peek()).addComponent((DataComponent)tmp);
            
        } else if (local.equals("status")) {
            if (DEBUG) System.out.println("end: status");
            ((Message) stack.peek()).addComponent((StatusComponent)tmp);
            
        } else if (local.equals("object")) {
            if (DEBUG) System.out.println("end: object");
            Object top = stack.peek();
            if (top instanceof MessageComponent) {
                ((MessageComponent)top).setObject((SystemObject)tmp);
            }
            
        } else if (local.equals("fields")) {
            if (DEBUG) System.out.println("end: fields");
            Object top = stack.peek();
            if (top instanceof MessageComponent) {
                ((MessageComponent)top).setFields((Map) tmp);
            }
            
        } else if (local.equals("field")) {
            if (DEBUG) System.out.println("end: field");
            String value = tmp.toString();
            
            value = (value.length() > 0) ? value : null;
            
            Key top = (Key)stack.pop();
            ((Map<Key,String>)stack.peek()).put(top, value);
            
        } else if (local.equals("s_id")) {
            if (DEBUG) System.out.println("end: s_id");
            int val = Integer.parseInt(tmp.toString());
            Object top = stack.peek();
            if (top instanceof SystemObject) {
                ((SystemObject)top).setS_ID(val);
            }
            
        } else if (local.equals("db_id")) {
            if (DEBUG) System.out.println("end: db_id");
            int val = Integer.parseInt(tmp.toString());
            Object top = stack.peek();
            if (top instanceof SystemObject) {
                ((SystemObject)top).setDB_ID(val);
            }
            
            
        } else if (local.equals("core")) {
            if (DEBUG) System.out.println("end: core");
            // Pop the current ControlObject off the stack and
            // use the prebuilt object
            stack.pop();
            stack.push(SystemObject.CORE);
         
        } else if (local.equals("action")) {
            if (DEBUG) System.out.println("end: action");
            ActionType a = ActionType.valueOf(tmp.toString().toUpperCase());
            // We can allow a to be null because of the isValid method
            Object atTop = stack.peek();
            if (atTop instanceof ControlComponent) {
                ((ControlComponent)atTop).setAction(a);
            } else if (atTop instanceof StatusComponent) {
                ((StatusComponent)atTop).setAction(a);
            }
            
        } else if (local.equals("sequence")) {
            if (DEBUG) System.out.println("end: sequence");
            long l = Long.parseLong(tmp.toString());
            ((MessageComponent) stack.peek()).setSequence(l);
        }
        // if none of the above, it is an unexpected element:
        // necessary to push popped element back!
        else {
            stack.push(tmp);
        }
    }    public Message getMessage() {
        return message;
    }

    public void characters(char[] data, int start, int length) {
        if (isStackReadyForText) {
            ((StringBuffer) stack.peek()).append(data, start, length);
        }
    }

    private String resolveAttrib(String uri, String local, Attributes attribs,
            String defaultValue) {

        String tmp = attribs.getValue(local);
        return (tmp != null) ? (tmp) : (defaultValue);
    }
    
    
    public static void main(String[] args) {
        
        int port = 9001;
        
        javax.xml.parsers.SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        
        try {
            SAXParser parser = factory.newSAXParser();
            ServerSocket server = new ServerSocket(port);
            
            Socket socket = server.accept();
            System.out.println("Connection accepted on " + port);
            
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter socketWriter = new PrintWriter(socket.getOutputStream(), true);
            
            String line = "";
            while (!line.trim().toUpperCase().equals("QUIT")) {
                line = socketReader.readLine();
                
                InputSource src = new InputSource(new StringReader(line));
                SAXMessageParser0_2 sax = new SAXMessageParser0_2();
                try
                {
                    parser.parse(src, sax);
                    Message m = sax.getMessage();
                    System.out.println(m.toXML());
                }
                catch (SAXException e)
                {
                    e.printStackTrace();
                }
            }
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        

        
        
    }
}