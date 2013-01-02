/**
 * 
 */
package com.getkickbak.plugin;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.api.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;

import android.media.AudioManager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

/**
 * @author Eric Chan
 */
public class ProximityIDPlugin extends CordovaPlugin
{
	public static final int     ILLEGAL_ACCESS_ERROR = 1;
	public double               SEND_VOL_RATIO;
	public double               RECV_VOL_RATIO;

	private static final String TAG                  = "ProximityID";
	private static final String INIT                 = "init";
	private static final String PRELOAD              = "preLoadIdentity";
	private static final String SEND                 = "sendIdentity";
	private static final String SCAN                 = "scanIdentity";
	private static final String STOP                 = "stop";
	private static final String VOLUME               = "setVolume";

	private boolean             isSender;
	private int                 s_vol                = -1;
	private Communicator        comm;
	private CommunicatorTask    task;
	private AudioManager        audioMan;

	/**
	 * 
	 */
	public ProximityIDPlugin()
	{
	}

	public void onDestroy()
	{
		stop();
	}

	/**
	 * Called when the system is about to start resuming a previous activity.
	 * 
	 * @param multitasking
	 *           Flag indicating if multitasking is turned on for app
	 */
	@Override
	public void onPause(boolean multitasking)
	{
		super.onPause(multitasking);
		if (task != null)
		{
			comm.pause();
		}
		Log.i(TAG, "Pause Triggered!");
	}

	/**
	 * Called when the activity will start interacting with the user.
	 * 
	 * @param multitasking
	 *           Flag indicating if multitasking is turned on for app
	 */
	@Override
	public void onResume(boolean multitasking)
	{
		super.onResume(multitasking);
		if (task != null)
		{
			comm.resume();
		}
		Log.i(TAG, "Resume Triggered!");
	}

	private class CommunicatorTask extends AsyncTask<Void, Void, Integer[]>
	{
		private Communicator    communicator;
		private CallbackContext callbackContext;

		public CommunicatorTask(Communicator c, CallbackContext cb)
		{
			communicator = c;
			callbackContext = cb;
		}

		@Override
		protected Integer[] doInBackground(Void... dummy)
		{
			Integer[] freqs = null;
			try
			{
				freqs = communicator.process();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return freqs;
		}

		@Override
		protected void onPostExecute(Integer[] freqs)
		{
			String pitch = new String("");
			if (freqs != null)
			{
				PluginResult progressResult = new PluginResult(PluginResult.Status.OK, createMatches(freqs));
				progressResult.setKeepCallback(true);
				callbackContext.sendPluginResult(progressResult);

				for (int i = 0; i < freqs.length; i++)
				{
					pitch += " " + Integer.toString(freqs[i]);
					if (i < freqs.length - 1)
					{
						pitch += ",";
					}
				}
				if (isSender)
				{
					// Log.i(TAG, "Playing Repeat Tone at " + pitch + "Hz");
				}
				else
				{
					stop();
					Log.i(TAG, "Signal Match! Signals= " + pitch + "Hz");
				}
			}
			else
			{
				PluginResult progressResult = new PluginResult(PluginResult.Status.NO_RESULT);
				progressResult.setKeepCallback(true);
				callbackContext.sendPluginResult(progressResult);
				stop();
				Log.i(TAG, "No Signal Found!");
			}
		}
	}

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException
	{
		Log.d(TAG, "execute " + action);

		if (action.equalsIgnoreCase(INIT))
		{
			SEND_VOL_RATIO = Double.valueOf(args.getString(0));
			RECV_VOL_RATIO = Double.valueOf(args.getString(1));
			Log.i(TAG, "SEND_VOL_RATIO= " + SEND_VOL_RATIO + ", RECV_VOL_RATIO= " + RECV_VOL_RATIO);
			init();
			callbackContext.success();
		}
		else if (action.equalsIgnoreCase(PRELOAD))
		{
			preLoadIdentity(callbackContext);
		}
		else if (action.equalsIgnoreCase(SEND))
		{
			sendIdentity(callbackContext);
		}
		else if (action.equalsIgnoreCase(SCAN))
		{
			Integer samples = args.getInt(0);
			Integer missedThreshold = args.getInt(1);
			Integer magThreshold = args.getInt(2);
			Double overlapRatio = args.getDouble(3);
			Log.i(TAG, "samples= " + samples + ", missedThreshold= " + missedThreshold + ", magThreshold= " + magThreshold
			      + ", overlapRatio= " + overlapRatio);
			scanIdentify(samples, missedThreshold, magThreshold, overlapRatio, callbackContext);
		}
		else if (action.equalsIgnoreCase(STOP))
		{
			stop();
			callbackContext.success();
		}
		else if (action.equalsIgnoreCase(VOLUME))
		{
			Integer vol = args.getInt(0);
			if (vol < 0)
			{
				vol = (isSender) ? (int) (SEND_VOL_RATIO * 100) : (int) (RECV_VOL_RATIO * 100);
			}
			setVolume(vol);
			callbackContext.success();
		}
		else
		{
			return false;
		}

		return true;
	}

	private void init()
	{
		audioMan = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * Create an error object based on the passed in errorCode
	 * 
	 * @param errorCode
	 *           the error
	 * @return JSONObject containing the error
	 */
	private static JSONObject createAccessError()
	{
		JSONObject error = null;
		try
		{
			error = new JSONObject();
			error.put("code", ILLEGAL_ACCESS_ERROR);
		}
		catch (JSONException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		return error;
	}

	/**
	 * Create an error object based on the passed in errorCode
	 * 
	 * @param errorCode
	 *           the error
	 * @return JSONObject containing the error
	 */
	private static JSONObject createMatches(Integer[] freqs)
	{
		JSONObject success = null;
		try
		{
			success = new JSONObject();
			JSONArray array = new JSONArray();
			for (Integer freq : freqs)
			{
				array.put(freq);
			}
			success.put("freqs", array);
		}
		catch (JSONException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		return success;
	}

	private void preLoadIdentity(CallbackContext callbackContext)
	{
		if ((task != null) && (!isSender))
		{
			callbackContext.sendPluginResult(new PluginResult(Status.ILLEGAL_ACCESS_EXCEPTION, createAccessError()));
		}
		else if (task == null)
		{
			comm = new Sender(audioMan);
			comm.preLoad();
			isSender = true;
		}
		else
		{
			callbackContext.success();
		}
	}

	private void sendIdentity(CallbackContext callbackContext)
	{
		if (((task != null) && (!isSender)) || ((task != null) && (comm == null)))
		{
			callbackContext.sendPluginResult(new PluginResult(Status.ILLEGAL_ACCESS_EXCEPTION, createAccessError()));
		}
		else if (task == null)
		{
			s_vol = audioMan.getStreamVolume(AudioManager.STREAM_MUSIC);
			int vol = (int) (audioMan.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * SEND_VOL_RATIO);
			audioMan.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
			Log.i(TAG, "Setting Volume to [" + vol + "]");
			task = new CommunicatorTask(comm, callbackContext);
			task.execute();
		}
		else
		{
			callbackContext.success();
		}
	}

	private void scanIdentify(Integer samples, Integer missedThreshold, Integer magThreshold, Double overlapRatio,
	      CallbackContext callbackContext)
	{
		if ((task != null) && (isSender))
		{
			callbackContext.sendPluginResult(new PluginResult(Status.ILLEGAL_ACCESS_EXCEPTION, createAccessError()));
		}
		else if (task == null)
		{
			s_vol = audioMan.getStreamVolume(AudioManager.STREAM_MUSIC);
			int vol = (int) (audioMan.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * RECV_VOL_RATIO);
			audioMan.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
			Log.i(TAG, "Setting Volume to [" + vol + "]");
			comm = new Receiver(audioMan, samples, missedThreshold, magThreshold, overlapRatio);
			task = new CommunicatorTask(comm, callbackContext);
			task.execute();
			isSender = false;
		}
		else
		{
			callbackContext.sendPluginResult(new PluginResult(Status.ILLEGAL_ACCESS_EXCEPTION, createAccessError()));
		}
	}

	private void stop()
	{
		if ((task != null) && (s_vol > 0))
		{
			//
			// Restore original volume, ready to take on more tasks
			//
			if (audioMan.getStreamVolume(AudioManager.STREAM_MUSIC) != s_vol)
			{
				audioMan.setStreamVolume(AudioManager.STREAM_MUSIC, s_vol, 0);
				Log.i(TAG, "Setting Volume Back to [" + s_vol + "]");
			}
			if (comm != null)
			{
				comm.stop();
			}
			task = null;
			s_vol = -1;
		}
	}

	private void setVolume(Integer v)
	{
		double volume = v / 100.0;
		volume *= audioMan.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		BigDecimal d = new BigDecimal(volume).setScale(0, RoundingMode.HALF_UP);
		if (audioMan.getStreamVolume(AudioManager.STREAM_MUSIC) != d.intValue())
		{
			audioMan.setStreamVolume(AudioManager.STREAM_MUSIC, d.intValue(), 0);
			Log.i(TAG, "Setting Volume to [" + d.intValue() + "]");
		}
	}
}
