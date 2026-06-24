/*
 * SonifiableMusicShape.java
 *
 * Created on May 25, 2004
 */
package com.dataSonification.v2.sound;
import com.dataSonification.v2.util.Log;
import com.softsynth.jmsl.DimensionNameSpace;
import com.softsynth.jmsl.MusicShape;
import com.dataSonification.v2.util.ReturnCode;
import com.dataSonification.v2.util.Subsystem;
/**
 * Wrapper class for a MusicShape.  
 * @author Kimo Johnson
 */
// package-private access
class SonifiableMusicShape extends MusicShape implements Sonifiable {
		
	/**
	 * Instantiates a SonifiableMusicShape with a specified dimension.
	 * @param dim the dimension of the MusicShape
	 * @see MusicShape
	 */
	SonifiableMusicShape(int dim) {
		super(dim);
	}
	/**
	 * Instantiates a SonifiableMusicShape with a specified DimensionNameSpace.
	 * @param space the DimensionNameSpace of the MusicShape
	 * @see MusicShape
	 */
	SonifiableMusicShape(DimensionNameSpace space) {
		super(space);
	}
	
	/* 
	 * Javadoc in interface
	 */
	public void sonify(double startTime) throws InterruptedException {
		try {
	       super.play(startTime);
		}
		catch(InterruptedException e) {
			Log.println(Subsystem.SOUND, ReturnCode.GENERAL_ERROR, "Exception caught: SonifiableMusicShape: " + e, Log.P_ERROR);
			throw e;
		}
	}
	
	/* 
	 * Javadoc in interface
	 */
	public void finishAll() {
		super.finishAll();
	}
    /*
     * Javadoc in interface
     */
    public void setInstrument(ConfigurableInstrument ins) {
        super.setInstrument(ins); 
    }
    
    public boolean isBlank()
    {
        return getData().size() == 0;
    }

}
