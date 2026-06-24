package com.dataSonification.v2.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.dataSonification.v2.Core;


public class PackageInspector {
    public static Map<String,String> getTableClassMap() throws IOException, URISyntaxException {
        URL res = Core.class.getResource("Core.class");
        
        List<String> allClasses = new LinkedList<String>();
        String protocol = res.getProtocol();
        
        if (protocol.equals("jar")) {
            getClassesFromJar(allClasses, res);
        } else if (protocol.equals("file")) {
            
            File dir = new File(new URI(res.toExternalForm())).getParentFile();
           
            if (dir.isDirectory()) {
                getClassesFromDirectory(allClasses, dir);
            } else {
                Log.println(Subsystem.DATA, ReturnCode.GENERAL_ERROR, "PackageInspector: Error getting package", Log.P_ERROR);
            }
        }
        
        Map<String,String> m = new HashMap<String,String>();
        for (String path : allClasses) {
            
            // only use classes with dataSonification in name
            String className = convertPathToClass("dataSonification", path);
            
            // Find the last dot in the class name
            int ix_dot = className.lastIndexOf(".");
            String shortName = className.substring(ix_dot+1).trim();
            
            // If the shortName contains a dollar sign, it's an anonymous or inner class
            if (shortName.indexOf("$") < 0 && shortName.length() > 0) {
                m.put(shortName.toLowerCase(), "com." + className);
            }
            
        }
		
        return m;
    }
    
    private static void getClassesFromJar(List<String> allClasses, URL res) throws IOException, URISyntaxException {
        
//      Parse the jarfile name
        String path = res.getPath();
        int ed_ix = path.indexOf("!");
        String jarname = path.substring(0,ed_ix);
      
        JarFile jar = new JarFile(new File(new URI(jarname)));
        
        for(Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.endsWith(".class")) {
                    allClasses.add(name.substring(0,name.length()-6));
                }
            }
    }
    
    private static void getClassesFromDirectory(List<String> allClasses, File dir) throws URISyntaxException {
        File[] files = dir.listFiles();
  
        for (int i = 0; i < files.length; i++) {
            URI name = files[i].toURI();
            String fullPath = name.toASCIIString();
            if (fullPath.endsWith(".class")) {
                allClasses.add(fullPath.substring(0,fullPath.length()-6));
            } else if (files[i].isDirectory()) {
                getClassesFromDirectory(allClasses, files[i]);
            }
        }
    }
    
    private static String convertPathToClass(String packageTop, String fullPath) {
        String[] pathElements = fullPath.split("/");
       
        boolean foundTop = false;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < pathElements.length; i++) {
            String element = pathElements[i];
            if (foundTop) {
                buf.append("." + element);
            } else if (element.equals(packageTop)) {
                foundTop = true;
                buf.append(element);
            }
            
        }
        return buf.toString();
        
    }
    
    public static void main(String[] args) {
        try {
            Map<String,String> m = PackageInspector.getTableClassMap();
            
            for (String key : m.keySet() ) {
                System.out.println("key: " + key + ", value: " + m.get(key));
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}