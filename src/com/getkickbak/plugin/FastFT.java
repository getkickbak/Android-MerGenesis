package com.getkickbak.plugin;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.badlogic.gdx.audio.analysis.FFT;

import android.util.Log;
import java.util.*;

public class FastFT
{
	/**
	 * The expected size of an audio buffer (in samples).
	 */
	private final int       MAG_THRESHOLD;
	/**
	 * The expected size of an audio buffer (in samples).
	 */
	public static final int ERROR_THRESHOLD     = 175;
	/**
	 * The expected size of an audio buffer (in samples).
	 */
	public static final int DEFAULT_BUFFER_SIZE = 1024;

	/**
	 * Overlap defines how much two audio buffers following each other should
	 * overlap (in samples). 75% overlap is advised in the MPM article.
	 */
	public static final int DEFAULT_OVERLAP     = 768;

	/**
	 * The audio sample rate. Most audio has a sample rate of 44.1kHz.
	 */
	private final float     sampleRate;

	/**
	 * Fourier Transform object.
	 */
	private FFT             fft;
	/**
	 * Fourier Transform object.
	 */
	private float           startFreq;
	/**
	 * Fourier Transform object.
	 */
	private float           endFreq;
	private int             matchCount;

	/**
	 * Create a new pitch detector.
	 * 
	 * @param audioSampleRate
	 *           The sample rate of the audio.
	 * @param audioBufferSize
	 *           The size of one audio buffer 1024 samples is common.
	 */
	public FastFT(final float audioSampleRate, final int mCount, final int magThreshold, final int length, float loFreq, float hiFreq)
	{
		this.MAG_THRESHOLD = magThreshold;
		this.matchCount = mCount;
		this.sampleRate = audioSampleRate;
		fft = new FFT(length, audioSampleRate);
		startFreq = loFreq;
		endFreq = hiFreq;
	}

	public Integer[] getPitch(float[] audioBuffer)
	{
		float freq;
		List<Integer[]> maxMag = new ArrayList<Integer[]>();

		fft.forward(audioBuffer);
		float[] imag = fft.getImaginaryPart();
		float[] real = fft.getRealPart();
		float fstart = startFreq / (sampleRate / audioBuffer.length);
		float fend = endFreq / (sampleRate / audioBuffer.length);

		BigDecimal start = new BigDecimal(fstart).setScale(0, RoundingMode.HALF_UP);
		BigDecimal end = new BigDecimal(fend).setScale(0, RoundingMode.HALF_UP);
		// Log.i("FastFT", "buffer.length=" + buffer.length + ", complex.length=" + real.length);
		for (int index = (start.intValue() - 1); index < (end.intValue() + 1); index++)
		{
			int mag = (int) Math.abs((Math.sqrt((real[index] * real[index]) + (imag[index] * imag[index]))));

			if (mag > MAG_THRESHOLD)
			{
				//
				// Sort on Index (asc)
				//
				Collections.sort(maxMag, new Comparator<Integer[]>()
				{
					public int compare(Integer[] i1, Integer[] i2)
					{
						return i1[1] - i2[1];
					}
				});
				//
				// Find any nearby Index
				//
				int foundIndex = Collections.binarySearch(maxMag, new Integer[] { mag, index }, new Comparator<Integer[]>()
				{
					public int compare(Integer[] i1, Integer[] i2)
					{
						//
						// Are they in the neighbourhood?
						//
						boolean isNeighbor = Math.abs(i1[1] - i2[1]) <= ERROR_THRESHOLD;

						if (isNeighbor) { return 0; }
						return (i1[1] - i2[1]);
					}
				});
				if (foundIndex >= 0)
				{
					Integer[] tmp = maxMag.get(foundIndex);
					//
					// Found an existing power even larger than adjacent power values
					//
					if (tmp[0] < mag)
					{
						tmp[0] = mag;
						tmp[1] = index;
						// Log.i("FastFT", "Update Index Freq Found= " + index * (sampleRate / buffer.length) + "Hz, Magitutde= " + mag);
					}
				}
				else
				{
					maxMag.add(new Integer[] { mag, index });
					// Log.i("FastFT", "Add to Index Freq Found= " + index * (sampleRate / buffer.length) + "Hz, Magitutde= " + mag);
				}
			}
		}
		if (maxMag.size() >= matchCount)
		{
			//
			// Sort on Magnitude (desc)
			//
			Collections.sort(maxMag, new Comparator<Integer[]>()
			{
				public int compare(Integer[] i1, Integer[] i2)
				{
					return i2[0] - i1[0];
				}
			});

			Integer[] ret = new Integer[matchCount];
			for (int i = 0; i < matchCount; i++)
			{
				freq = maxMag.get(i)[1] * (sampleRate / audioBuffer.length);
				BigDecimal d = new BigDecimal(freq).setScale(0, RoundingMode.HALF_UP);
				ret[i] = d.intValue();
			}
			//
			// Sort on Frequency (asc)
			//
			Arrays.sort(ret);
			String pitch = new String("");
			for (int i = 0; i < ret.length; i++)
			{
				pitch += " " + Integer.toString(ret[i]);
				if (i < ret.length - 1)
				{
					pitch += ",";
				}
			}
			// Log.i("FastFT", "PostFFT - Freq Resolution= " + (sampleRate / buffer.length) + "Hz, Pitch=" + pitch + "Hz");

			return ret;
		}

		return null;
	}
}
