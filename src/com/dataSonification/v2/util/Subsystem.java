/*
 * Subsystem.java
 *
 * Created on May 20, 2005, 9:42 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.dataSonification.v2.util;

/**
 *
 * @author Owner
 */
public enum Subsystem
{
    CORE(),
    DATA(),
    SOUND(),
    UI();
    
    /** Creates a new instance of Subsystem */
    private Subsystem()
    {
    }
    
}
