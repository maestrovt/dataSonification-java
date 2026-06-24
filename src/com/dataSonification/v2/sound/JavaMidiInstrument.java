package com.dataSonification.v2.sound;

import com.dataSonification.v2.Config;
import com.dataSonification.v2.sound.ConfigurableInstrument;
import com.dataSonification.v2.util.Key;
import com.softsynth.jmsl.score.midi.MidiScoreInstrument;

import com.dataSonification.v2.util.Subsystem;
import com.dataSonification.v2.util.Log;

public class JavaMidiInstrument extends MidiScoreInstrument implements ConfigurableInstrument {

	/**
	 * The config object for the instrument is package-private to allow
	 * SonifiableMidiInstrument to access it.
	 */
	Config config;

	public JavaMidiInstrument() {
		super();
	}

	/*
	 * Javadoc in interface
	 */
	public void setConfig(Config c) {
		this.config = c;
		Integer channel = (Integer) config.getField(Key.CHANNEL);
		Integer program = (Integer) config.getField(Key.PROGRAM);
        Log.println(Subsystem.SOUND,null,"New Midi Instrument: c=" + channel + " p=" + program, Log.P_VERBOSE);
		Integer s_id = (Integer) config.getField(Key.S_ID);
		
		if (channel != null && program != null && s_id != null) {
		    int p = program.intValue();
			super.setChannel(channel.intValue());
			super.setProgram(p);
			super.setName("JavaMidiInstrument_" + s_id);
		} else {
			super.setChannel(1);
			super.setProgram(1);
		}

	}

}