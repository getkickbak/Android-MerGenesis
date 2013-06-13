/*
 * Copyright (c) 2010 Animesh Kumar (https://github.com/anismiles)
 * Copyright (c) 2010 Strumsoft (https://strumsoft.com)
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.getkickbak.plugin.websocket;

import java.net.URI;
import java.util.Map.Entry;
import java.util.Hashtable;
import java.util.Random;

import android.webkit.WebView;

import org.java_websocket.drafts.*;

// import com.strumsoft.websocket.phonegap.WebSocket;

/**
 * The <tt>WebSocketFactory</tt> is like a helper class to instantiate new
 * WebSocket instaces especially from Javascript side. It expects a valid
 * "ws://" URI.
 * 
 * @author Animesh Kumar
 */
public class WebSocketFactory
{

	/** The app view. */
	WebView                              appView;

	private Hashtable<String, WebSocket> sTable;

	/**
	 * Instantiates a new web socket factory.
	 * 
	 * @param appView
	 *           the app view
	 */
	public WebSocketFactory(WebView appView)
	{
		this.appView = appView;
		sTable = new Hashtable<String, WebSocket>();
	}

	@android.webkit.JavascriptInterface
	public WebSocket getInstance(String url)
	{
		// use Draft10 by default
		return getInstance(url, new Draft_10());
	}

	@android.webkit.JavascriptInterface()
	public WebSocket getInstance(String url, Draft draft)
	{
		WebSocket socket = null;
		Thread th = null;
		try
		{
			socket = new WebSocket(appView, new URI(url), draft, getRandonUniqueId());
			socket.connect();
			this.sTable.put(socket.getId(), socket);
			return socket;
		}
		catch (Exception e)
		{
			// Log.v("websocket", e.toString());
		}
		return null;
	}

	@android.webkit.JavascriptInterface()
	public void removeInstance(String id)
	{
		WebSocket socket = sTable.remove(id);
		if (socket != null)
		{
			socket.close();
		}
	}

	public void onDestroy()
	{
		for (Entry<String, WebSocket> entry : sTable.entrySet())
		{
			WebSocket socket = entry.getValue();
			socket.close();
		}
		sTable.clear();
	}

	/**
	 * Generates random unique ids for WebSocket instances
	 * 
	 * @return String
	 */
	private String getRandonUniqueId()
	{
		return "WEBSOCKET." + new Random().nextInt(100);
	}

}
