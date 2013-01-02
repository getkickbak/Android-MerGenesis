package com.getkickbak.plugin;

import java.util.Arrays;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class Receiver extends Communicator
{
	public static final String TAG              = "ProximityID-Receiver";
	//
	// Grab data fresh from audioInput if we cannot find a signal lock
	//
	private final int          CONSEQ_MISSED_THRESHOLD;
	public final static int    MAX_FRAME_SIZE   = 16 * 1024;
	public final static int    MATCH_THRESHOLD  = 2;
	//
	// Data overlap for each FFT calculation
	//
	private final double       OVERLAP_RATIO;
	//
	// The expected size of an audio buffer (in samples).
	//
	private final int          MAG_THRESHOLD;

	public static int          recMinSize       = AudioRecord.getMinBufferSize((int) fs, AudioFormat.CHANNEL_IN_MONO,
	                                                  AudioFormat.ENCODING_PCM_16BIT) * 32;

	private AudioRecord        audioInput       = null;
	//
	// AudioData buffer used for FFT
	//
	private float[]            audioBuf;

	//
	// AudioData buffer used to read from Speakers
	//
	private final short[]      audioData;
	//
	// Internal variables used to keep track of where we are in the audioBuffer
	//
	private int                overlapIteration = 0;
	private int                offset           = 0;
	private int                matchCount       = 0;
	//
	// FFT instantiation
	//
	private FastFT             alg;

	public Receiver(AudioManager m, int samples, int missedThreshold, int magThreshold, double overlapRatio)
	{
		audioMan = m;
		N = samples;

		CONSEQ_MISSED_THRESHOLD = missedThreshold;
		MAG_THRESHOLD = magThreshold;
		OVERLAP_RATIO = overlapRatio;

		audioData = new short[(int) (N / (OVERLAP_RATIO * 2))];
		audioBuf = new float[MAX_FRAME_SIZE];
		alg = new FastFT((float) fs, Communicator.NUM_SIGNALS, MAG_THRESHOLD, audioBuf.length, (float) loFreq, (float) hiFreq);

		// BandPass filter = new BandPass((float) loFreq, (float) (hiFreq - loFreq), (float) fs);
		// HighPass filter = new HighPass((float) loFreq, (float) fs);
		// filter.process(audioBuf);
		// alg = new MPM(fs, audioBuf.length, MPM.DEFAULT_CUTOFF);
		// alg = new DW(fs, audioBuf.length);
		// alg = new FastYin(fs, audioBuf.length, FastYin.DEFAULT_THRESHOLD);
		// alg = new AMDF(fs, audioBuf.length, loFreq, hiFreq);
	}

	protected void cleanup()
	{
		if (audioInput != null)
		{
			audioInput.stop();
			audioInput.release();
			audioInput = null;
		}
	}

	private void getSound(int conseqMissedCounts)
	{
		overlapIteration = overlapIteration % ((int) (1 / OVERLAP_RATIO) + 1);
		if (overlapIteration == 0 || (conseqMissedCounts >= CONSEQ_MISSED_THRESHOLD))
		{
			int samplesToRead = audioData.length;
			offset = overlapIteration = 0;
			/*
			 * if (conseqMissedCounts > CONSEQ_MISSED_THRESHOLD)
			 * {
			 * Log.i(TAG, "CONSEQ_MISSED_THRESHOLD(" + CONSEQ_MISSED_THRESHOLD + ") Exceeded ...");
			 * }
			 * Log.i(TAG, "Retrieving Sound Data ...");
			 */
			do
			{
				int read = audioInput.read(audioData, audioData.length - samplesToRead, samplesToRead); // record data from mic into
				                                                                                        // buffer
				if ((read != AudioRecord.ERROR_INVALID_OPERATION) && (read != AudioRecord.ERROR_BAD_VALUE) && (read > 0))
				{
					samplesToRead -= read;
				}
				else
				{
					Log.i(TAG, "Error Retrieving Sound Data, discard ...");
				}
			} while (samplesToRead > 0);
		}
		else
		{
			// Log.i(TAG, "Quick Retrieve Sound Data ..., offset = " + offset);
		}

		// System.arraycopy(audioData, offset, audioBuf, 0, N);
		for (int i = 0; i < N; i++)
		{
			audioBuf[i] = audioData[i + offset];
		}
		Arrays.fill(audioBuf, N, audioBuf.length, 0);

		offset += (int) (N * OVERLAP_RATIO);
		overlapIteration++;
		// Log.i(TAG, "Retrieved Sound Data ... samplesToRead=" + samplesToRead + ", offset=" + offset);
	}

	public void preLoad()
	{
	}

	public Integer[] process()
	{
		Integer[] freqs = null, sigFreq = null;
		int conseqMissedCounts = 0, sleepCount = 0;

		Log.i(TAG, "Receiving AudioInput ...");
		Log.i(TAG, "Sample Length= " + N + ", recMinSize= " + recMinSize);
		try
		{
			// construct AudioRecord to record audio from microphone with sample rate of 44100Hz
			audioInput = new AudioRecord(MediaRecorder.AudioSource.MIC, (int) fs, AudioFormat.CHANNEL_IN_MONO,
			      AudioFormat.ENCODING_PCM_16BIT, recMinSize);
			audioInput.startRecording();
			do
			{
				synchronized (this)
				{
					if (paused == true)
					{
						conseqMissedCounts = matchCount = 0;
						sigFreq = null;
						if ((sleepCount = sleepCount % 100) == 0)
						{
							audioInput.stop();
							Log.i(TAG, "Paused ...");
						}
						sleepCount++;
						Thread.sleep((long) (0.1 * 1000));
						continue;
					}
					else if (audioInput.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED)
					{
						audioInput.startRecording();
					}
				}

				getSound(conseqMissedCounts);
				freqs = alg.getPitch(audioBuf);

				synchronized (this)
				{
					if (abort != true)
					{
						if ((freqs != null) && (freqs.length > 0))
						{
							if ((sigFreq == null))
							{
								sigFreq = freqs;
								matchCount++;
							}
							else
							{
								if (Arrays.equals(sigFreq, freqs))
								{
									conseqMissedCounts = 0;
									matchCount++;
								}
								else
								{
									conseqMissedCounts++;
								}
								sigFreq = freqs;
							}
						}
						else
						{
							conseqMissedCounts++;
							matchCount = 0;
							sigFreq = null;
						}
					}
					else
					{
						freqs = null;
						abort = false;
						break;
					}
				}
			} while (matchCount < MATCH_THRESHOLD);
		}
		catch (Exception e)
		{
			Log.i(TAG, "Interrupted! " + e.getMessage());
			freqs = null;
		}

		cleanup();

		return freqs;
	}

	public void pause()
	{
		synchronized (this)
		{
			paused = true;
		}
	}

	public void resume()
	{
		paused = false;
	}

	public void stop()
	{
		abort = true;
		paused = false;
	}
}
