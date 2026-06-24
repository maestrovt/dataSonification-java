package com.dataSonification.v2.sound;

/**
 * A container class for sample info.
 * @author kimo
 */
public class SampleInfo {
	public final int index;
	public final String filename;
	public SampleInfo(String filename, int index) {
		this.index = index;
		this.filename = filename;
	}
}