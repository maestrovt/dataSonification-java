package com.dataSonification.v2.sound;

import java.util.Iterator;

public interface Remapper {
	public void setState(int st_p, int s, int ed_p, String base, String type);
	public Iterator<SampleInfo> iterator();
	public int getAlternativeSampleIndex(int pitchIndex, double timeStretch, double[] data);
    public SampleInfo[] getSamplesFor(int pitchIndex, double timeStretch, double[] data);
}