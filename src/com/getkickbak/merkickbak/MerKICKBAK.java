package com.getkickbak.merkickbak;

import android.os.Bundle;
import android.webkit.CookieManager;
// import android.app.Activity;
import android.view.Menu;
import org.apache.cordova.*;

public class MerKICKBAK extends DroidGap
{

	/*********************************************************
	 * SSCL Native Function
	 *********************************************************/
	// Load jni .so on initialization
	static
	{
		// System.loadLibrary("ssc");
		// System.loadLibrary("sscl");
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_kick_bak);
		super.setIntegerProperty("loadUrlTimeoutValue", 60000);
		super.setIntegerProperty("splashscreen", R.drawable.splash);
		super.loadUrl("file:///android_asset/www/index.html", 10000);
		try
		{
			CookieManager.getInstance().setAcceptCookie(true);
			// CookieManager.setAcceptFileSchemeCookies(true);
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
