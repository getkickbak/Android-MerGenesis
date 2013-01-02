package com.getkickbak.plugin;

import android.media.AudioManager;

public abstract class Communicator
{
	protected AudioManager     audioMan;
	protected int              N;
	public static final int    fs     = 44100;
	public static final double FREQ_GAP    = 500.0;
	public static final int    NUM_SIGNALS = 3;

	public static final double loFreq = 17000.0;
	public static final double hiFreq = 20000.0;

	protected boolean          abort  = false;
	protected boolean          paused = false;

	abstract protected void cleanup();

	abstract public void preLoad();
	
	abstract public Integer[] process();

	abstract public void stop();

	abstract public void pause();

	abstract public void resume();

	static public double getBandwidth()
	{
		return hiFreq - loFreq;
	}
}
