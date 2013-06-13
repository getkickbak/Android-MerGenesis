package com.getkickbak.plugin.wifimanager;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.api.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.json.JSONObject;

import com.getkickbak.merkickbak.MerKICKBAK;

import java.util.List;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.webkit.WebView;

/**
 * @author Eric Chan
 */
public class Manager extends CordovaPlugin
{
	public static final int     ILLEGAL_ACCESS_ERROR = 1;

	private static final String TAG                  = "ConnectionManager";
	private static final String BLANK_MESSAGE        = "";
	private static final String POS_CONNECTED        = "onPosConnected";
	private static final String INIT                 = "init";
	private static final String EST_POS_CONN         = "establishPosConn";
	private static final String SET_POS_ENABLED      = "setIsPosEnabled";
	private static final String POS_AP               = "^.*POS\\d+_AP.*";
	private static Manager      self                 = null;
	private static final String SCRIPT_OBJ           = "window.plugins.WifiConnMgr";

	private WifiManager         manager;
	// private ConnectivityManager cmanager;
	private Boolean             isPosEnabled;

	public static class WifiConnReceiver extends BroadcastReceiver
	{
		static public final String TAG = "ConnectionBroadcastReceiver";

		@Override
		public void onReceive(Context context, Intent intent)
		{
			NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

			//
			// Do Nothing if POS Feature is disabled
			//
			if (networkInfo == null) { return; }
			if ((self == null) || !self.isPosEnabled) { return; }

			Log.d(TAG,
			      "Type : " + networkInfo.getType() + ", State : " + networkInfo.getState() + ", isPosEnabled : "
			            + self.getIsPosEnabled());

			if (self.getIsPosEnabled())
			{
				self.establishPosConn(null);
			}
		}
	}

	public Manager()
	{
		self = this;
		// this.cmanager = (ConnectivityManager) activity.getSystemService(Activity.CONNECTIVITY_SERVICE);
		setIsPosEnabled(false);
	}

	public void onDestroy()
	{
	}

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException
	{
		Log.d(TAG, "execute " + action);

		if (action.equalsIgnoreCase(INIT))
		{
			Activity activity = this.cordova.getActivity();
			this.manager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
			callbackContext.success();
		}
		else if (action.equalsIgnoreCase(EST_POS_CONN))
		{
			establishPosConn(callbackContext);
			callbackContext.success();
		}
		else if (action.equalsIgnoreCase(SET_POS_ENABLED))
		{
			Boolean posEnabled = args.getBoolean(0);
			setIsPosEnabled(posEnabled);
			callbackContext.success();
		}
		else
		{
			return false;
		}

		return true;
	}

	private Boolean getIsPosEnabled()
	{
		return isPosEnabled;
	}

	private void setIsPosEnabled(Boolean isPosEnabled)
	{
		this.isPosEnabled = isPosEnabled;
	}

	/**
	 * This method is used to join the proper WiFi network when necessary. Normally,
	 * the Android retains network configuration and it is not necessary to manually
	 * re-join the desired network on software startup. However, when it is determined
	 * that the Android is not currently attached to the proper network, this function
	 * is used to correct that situation.
	 */
	private Boolean joinNetwork(WifiManager wifiMgr, WifiConfiguration wc)
	{
		Boolean rc = false;
		try
		{
			wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

			wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

			wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

			wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

			wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

			wc.hiddenSSID = false;
			wc.priority = 32;

			// wc.SSID = "\"" + SSID + "\"";
			wc.status = WifiConfiguration.Status.ENABLED;

			// wc.wepKeys[0] = key;
			// wc.wepTxKeyIndex = 0;

			int netID = wc.networkId;

			/*
			 * if (-1 == (netID = wifiMgr.addNetwork (wc)))
			 * {
			 * listener.lostConnection (true);
			 * }
			 * else
			 */
			{
				wifiMgr.updateNetwork(wc);
				wifiMgr.setWifiEnabled(true);
				Log.d(TAG, "Connecting to " + wc.SSID + " ...");
				rc = wifiMgr.enableNetwork(netID, true);
				Log.d(TAG, "enableNetwork returned " + rc);
				// Thread.sleep(5000); // Delay to allow the DHCP process to work
			}
		}
		catch (Exception e)
		{
			Log.d(TAG, e.getMessage());
			// listener.lostConnection(true);
		}

		return rc;
	}

	public void establishPosConn(CallbackContext callbackContext)
	{
		WifiInfo info = manager.getConnectionInfo();
		List<WifiConfiguration> configs = manager.getConfiguredNetworks();
		Boolean connected = (info != null);
		Boolean reconnect = false;

		setIsPosEnabled(true);
		if (connected)
		{
			switch (info.getSupplicantState())
			{
				case DISCONNECTED:
				case DORMANT:
				case INACTIVE:
				case SCANNING:
				{
					Log.d(TAG, "establishPosConn - State(" + info.getSupplicantState() + ")");
					break;
				}
				default:
				{
					for (WifiConfiguration wc : configs)
					{
						if (wc.SSID.matches(Manager.POS_AP))
						{
							Log.d(TAG, "establishPosConn - SSID(" + wc.SSID + ")");
							//
							// Successful connection to the POS, no more processing required
							//
							if (info.getNetworkId() == wc.networkId)
							{
								if (callbackContext != null)
								{
									callbackContext.success();
								}
								manager.saveConfiguration();
								return;
							}
						}
						//
						// Remove all non-POS related AccessPoints
						//
						else
						{
							manager.removeNetwork(wc.networkId);
						}
					}
					reconnect = true;
					break;
				}
			}
		}
		//
		// Reconnect POS
		//
		if ((!connected) || (reconnect))
		{
			Log.d(TAG, "establishPosConn - reconnecting POS ...");
			//
			// Connect to POS, if current connection is not of POS type
			//
			for (WifiConfiguration wc : configs)
			{
				// Log.d(TAG, "establishPosConn - SSID(" + wc.SSID + ") preSharedKey(" + wc.preSharedKey + ")");
				//
				// First POS match that we find
				//
				if (wc.SSID.matches(Manager.POS_AP))
				{
					if (wc.preSharedKey != null)
					{
						Log.d(TAG, "establishPosConn - Found POS Match(" + wc.SSID + ")");
						Boolean b = joinNetwork(manager, wc);
						if (b)
						{
							Log.d(TAG, "establishPosConn - Connection Established(" + wc.SSID + ")");
							this.webView.post(new Runnable()
							{
								@Override
								public void run()
								{
									webView.loadUrl(buildJavaScriptData(POS_CONNECTED, BLANK_MESSAGE));
								}
							});
							break;
						}
					}
				}
				//
				// Remove all non-POS related AccessPoints
				//
				else
				{
					manager.removeNetwork(wc.networkId);
				}

			}
		}

		manager.saveConfiguration();
		if (callbackContext != null)
		{
			callbackContext.success();
		}
	}

	/**
	 * Builds text for javascript engine to invoke proper event method with
	 * proper data.
	 * 
	 * @param event
	 *           websocket event (onOpen, onMessage etc.)
	 * @param msg
	 *           Text message received from websocket server
	 * @return
	 */
	private String buildJavaScriptData(String event, String msg)
	{
		if (msg == null)
		{
			msg = "";
		}
		String _d = "javascript:" + SCRIPT_OBJ + "." + event + "(" + "{" + "\"data\":'" + msg.replaceAll("'", "\\\\'") + "'" + "}"
		      + ")";

		return _d;
	}
}
