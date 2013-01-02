package com.getkickbak.plugin;

import java.util.Arrays;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class Sender extends Communicator
{
	public static final String TAG         = "ProximityID-Sender";
	public static int          playMinSize = AudioTrack.getMinBufferSize((int) fs, AudioFormat.CHANNEL_OUT_MONO,
	                                             AudioFormat.ENCODING_PCM_16BIT) * 384;
	public static final int    duration    = 20;                                       // seconds
	public static final int    numSamples  = duration * fs;
	private Integer[]          freqs       = new Integer[NUM_SIGNALS];

	private AudioTrack         track;

	public Sender(AudioManager m)
	{
		audioMan = m;
		track = null;
	}

	protected void cleanup()
	{
		stop();
	}

	public void preLoad()
	{
		double sampleRate = fs;
		//
		// Distribute frequencies to appropriate sections
		//
		double bw = Communicator.getBandwidth() / NUM_SIGNALS;

		boolean stay;
		do
		{
			stay = false;
			for (int i = 0; i < freqs.length; i++)
			{
				freqs[i] = (int) (Math.random() * bw) + ((int) (i * bw)) + ((int) loFreq);
			}
			for (int i = 0; i < (freqs.length - 1); i++)
			{
				if ((freqs[i] + FREQ_GAP) > freqs[i + 1])
				{
					stay = true;
					break;
				}
			}
		} while (stay);

		cleanup();
		// audioBuf = WaveTools.wavread(inputPath, fs, this);
		track = new AudioTrack(AudioManager.STREAM_MUSIC, (int) fs, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
		      playMinSize, AudioTrack.MODE_STATIC);

		short[] buffer = DTMF.genTone(freqs, sampleRate, numSamples);
		int ret = track.setLoopPoints(0, numSamples, -1);

		// play an AudioTrack in loop
		if (ret != AudioTrack.SUCCESS)
		{
			Log.i(TAG, "Cannot Loop Tone Generator");
		}
		track.write(buffer, 0, numSamples); // write data to audio hardware
		track.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
	}

	public Integer[] process()
	{
		track.play();
		Log.i(TAG, "Generating Tones ...");
		
		return freqs;
	}

	public void pause()
	{
		if (track != null)
		{
			track.pause();
			synchronized (this)
			{
				paused = true;
			}
			Log.i(TAG, "Paused AudioTrack playback ...");
		}
	}

	public void resume()
	{
		if (track != null)
		{
			track.play();
			paused = false;
			Log.i(TAG, "Resumed AudioTrack playback ...");
		}
	}

	public void stop()
	{
		if (track != null)
		{
			paused = false;
			if (track.getState() == AudioTrack.STATE_INITIALIZED)
			{
				track.pause();
			}
			track.flush();
			track.release();
			track = null;
			Log.i(TAG, "Stopped AudioTrack playback ...");
		}
	}
}
