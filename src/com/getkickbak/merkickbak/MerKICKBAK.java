package com.getkickbak.merkickbak;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
// import android.app.Activity;
import android.view.Menu;
import org.apache.cordova.*;

import com.getkickbak.plugin.WebSocketFactory;

public class MerKICKBAK extends DroidGap
{
	static public Boolean singleTask = false;
	/*********************************************************
	 * SSCL Native Function
	 *********************************************************/
	// Load jni .so on initialization
	static
	{
		// System.loadLibrary("ssc");
		// System.loadLibrary("sscl");
	}

	public void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);

			// setContentView(R.layout.activity_kick_bak);
			super.setIntegerProperty("loadUrlTimeoutValue", 60000);
			super.setIntegerProperty("splashscreen", R.drawable.splash);
			super.loadUrl("file:///android_asset/www/index.html", 10000);

			// 4.2.2 and up, WebSocket is built-in
			//if (((Build.VERSION_CODES.JELLY_BEAN_MR1 == Build.VERSION.SDK_INT) && (!Build.VERSION.RELEASE.startsWith("4.2.2"))) || //
			//      (Build.VERSION_CODES.JELLY_BEAN_MR1 > Build.VERSION.SDK_INT))
			{
				appView.addJavascriptInterface(new WebSocketFactory(appView), "WebSocketFactory");
			}

			appView.getSettings().setGeolocationDatabasePath(this.getContext().getFilesDir().getPath() + "/");

			DisplayMetrics metrics = new DisplayMetrics();
			Display display = getWindowManager().getDefaultDisplay();

			display.getMetrics(metrics);
			float density = metrics.density;
			float width = metrics.widthPixels / density;
			Log.i("MerKICKBAK", "Display Width " + width + "px");

			int orientation = getResources().getConfiguration().orientation;
			switch (orientation)
			{
				case Configuration.ORIENTATION_LANDSCAPE:
				{
					this.getActivity().setRequestedOrientation(
					      (width > 1024.0) ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					break;
				}
				case Configuration.ORIENTATION_PORTRAIT:
				default:
				{
					this.getActivity().setRequestedOrientation(
					      (width > 640.0) ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					break;
				}
			}
		}
		catch (Throwable e)
		{

		}
		try
		{
			CookieManager.getInstance().setAcceptCookie(true);
			// CookieManager.setAcceptFileSchemeCookies(true);
			singleTask = true;
		}
		catch (Throwable e)
		{}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// getMenuInflater().inflate(R.menu.menu, menu);
		// return true;
		return false;
	}
}
