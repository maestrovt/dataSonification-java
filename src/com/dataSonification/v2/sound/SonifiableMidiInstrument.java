//
//  SonifiableMidiInstrument.java
//  dataSonificationV2
//
// A Sonifiable Midi Instrument that can be used without MusicShapes
// Used for continous sonification
//
//  6/6/04 Original version by Kimo Johnson
//  6/9/04 Modifications by Stefan Tomic (minor bug fix and stop method)


package com.dataSonification.v2.sound;

import java.util.LinkedList;

import com.dataSonification.v2.Config;
import com.dataSonification.v2.sound.ConfigurableInstrument;
import com.dataSonification.v2.util.Log;
import com.softsynth.jmsl.JMSL;
import com.dataSonification.v2.util.Subsystem;

/**
 * A MidiInstrument designed to be used with ContinuousArranger.  The 
 * instrument will play continuously and allows for program and velocity
 * changes.
 * @author Kimo Johnson
 *
 */
//package-private access
class SonifiableMidiInstrument extends JavaMidiInstrument implements Sonifiable {

	private MidiEvent lastEvent = null;
	private LinkedList<MidiEvent> eventList;
	
	/**
	 * Instantiates a SonifiableMidiInstrument.  The midi channel and
	 * program are called by setConfig on the superclass.
	 */
	SonifiableMidiInstrument() {
		super();
		eventList = new LinkedList<MidiEvent>();
	}
	
	/**
	 * Add a MidiEvent (pitch, velocity) to the list of events for this instrument to play.
	 * @param pitch MIDI pitch
	 * @param velocity MIDI velocity
	 */
	synchronized void addMidiEvent(int pitch, int velocity) {
		eventList.add(new MidiEvent(pitch, velocity));
	}
		
	
	/* 
	 * Javadoc in superclass
	 */
	public void sonify(double startTime) {

		//call noteOff on previous note (if playing)
		stop(JMSL.now());
		
		MidiEvent event;
		synchronized(this) {
			event = eventList.removeFirst();
		}
		if (event != null) {
			super.noteOn(startTime, event.pitch, event.velocity);
			lastEvent = event;
		}
	}
	
	/* 
	 * Javadoc in superclass
	 */
	public void finishAll() {
		stop(JMSL.now());
	}
	
	
	/**
	 * Calls noteOff to stop the instrument.
	 * @param startTime when to stop
	 */
	public void stop(double startTime) {
		if (lastEvent != null && lastEvent.pitch > 0) {
			Log.println(Subsystem.SOUND, null, "SonifiableMidiInstrument: stop called", Log.P_INFO); 
			try {
				super.noteOff(JMSL.now(), lastEvent.pitch, lastEvent.velocity);
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Container class for a MIDI pitch and velocity event.
	 * @author Kimo Johnson
	 */
	private static class MidiEvent {
		public final int pitch;
		public final int velocity;
		public MidiEvent(int pitch, int velocity) {
			this.pitch = pitch;
			this.velocity = velocity;
		}
	}

    /*
     * Javadoc in interface
     */
    public void setInstrument(ConfigurableInstrument ins) {}

    /*
     * Javadoc in interface
     */
    public void setConfig(Config c) {
        super.setConfig(c);
    }
    
    public boolean isBlank()
    {
        return eventList.size() == 0;
    }

}
