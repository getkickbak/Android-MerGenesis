/*
 * Copyright (c) 2010 Nathan Rajlich (https://github.com/TooTallNate)
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
package com.getkickbak.plugin;

import java.net.URI;

import android.util.Log;
import android.webkit.WebView;

import org.java_websocket.handshake.*;
import org.java_websocket.drafts.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.WebSocketImpl;
import java.lang.reflect.Method;

/**
 * The <tt>WebSocket</tt> is an implementation of WebSocket Client API, and
 * expects a valid "ws://" URI to connect to. When connected, an instance
 * recieves important events related to the life of the connection, like
 * <var>onOpen</var>, <var>onClose</var>, <var>onError</var> and
 * <var>onMessage</var>. An instance can send messages to the server via the
 * <var>send</var> method.
 * 
 * @author Animesh Kumar
 */
public class WebSocket extends WebSocketClient
{
	/**
	 * An empty string
	 */
	private static String      BLANK_MESSAGE    = "";
	/**
	 * The javascript method name for onOpen event.
	 */
	private static String      EVENT_ON_OPEN    = "onopen";
	/**
	 * The javascript method name for onMessage event.
	 */
	private static String      EVENT_ON_MESSAGE = "onmessage";
	/**
	 * The javascript method name for onClose event.
	 */
	private static String      EVENT_ON_CLOSE   = "onclose";
	/**
	 * The javascript method name for onError event.
	 */
	private static String      EVENT_ON_ERROR   = "onerror";
	/**
	 * The default port of WebSockets, as defined in the spec.
	 */
	public static final int    DEFAULT_PORT     = 80;
	/**
	 * The WebSocket protocol expects UTF-8 encoded bytes.
	 */
	public static final String UTF8_CHARSET     = "UTF-8";
	/**
	 * The byte representing Carriage Return, or \r
	 */
	public static final byte   DATA_CR          = (byte) 0x0D;
	/**
	 * The byte representing Line Feed, or \n
	 */
	public static final byte   DATA_LF          = (byte) 0x0A;

	// //////////////// INSTANCE Variables
	/**
	 * The WebView instance from Phonegap DroidGap
	 */
	private final WebView      appView;
	/**
	 * The unique id for this instance (helps to bind this to javascript events)
	 */
	private String             id;

	/**
	 * Constructor.
	 * Note: this is protected because it's supposed to be instantiated from {@link WebSocketFactory} only.
	 * 
	 * @param appView
	 *           {@link android.webkit.WebView}
	 * @param uri
	 *           websocket server {@link URI}
	 * @param id
	 *           unique id fo this instance
	 */
	public WebSocket(WebView appView, URI uri, Draft draft, String id)
	{
		super(uri, draft);

		//WebSocketImpl.DEBUG = true;
		this.appView = appView;
		this.id = id;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////// WEB SOCKET API Methods
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Closes connection with server
	 */
	@android.webkit.JavascriptInterface()
	public void close()
	{
		super.close();
	}

	/**
	 * Sends <var>text</var> to server
	 * 
	 * @param text
	 *           String to send to server
	 */
	@android.webkit.JavascriptInterface()
	public void send(final String text)
	{
		super.send(text);
	}

	/**
	 * Called when an entire text frame has been received.
	 * 
	 * @param msg
	 *           Message from websocket server
	 */
	public void onMessage(final String message)
	{
		appView.post(new Runnable()
		{
			@Override
			public void run()
			{
				appView.loadUrl(buildJavaScriptData(EVENT_ON_MESSAGE, message));
			}
		});
	}

	public void onOpen(ServerHandshake handshakedata)
	{
		appView.post(new Runnable()
		{
			@Override
			public void run()
			{
				appView.loadUrl(buildJavaScriptData(EVENT_ON_OPEN, BLANK_MESSAGE));
			}
		});
	}

	public void onClose(int code, String reason, boolean remote)
	{
		appView.post(new Runnable()
		{
			@Override
			public void run()
			{
				appView.loadUrl(buildJavaScriptData(EVENT_ON_CLOSE, BLANK_MESSAGE));
			}
		});
	}

	public void onError(final Exception ex)
	{
		appView.post(new Runnable()
		{
			@Override
			public void run()
			{
				appView.loadUrl(buildJavaScriptData(EVENT_ON_ERROR, ex.getMessage()));
			}
		});
	}

	@android.webkit.JavascriptInterface()
	public String getId()
	{
		return id;
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
		String _d = "javascript:WebSocket." + event + "(" + "{" + "\"_target\":\"" + id + "\"," + "\"data\":'"
		      + msg.replaceAll("'", "\\\\'") + "'" + "}" + ")";
		return _d;
	}
}
