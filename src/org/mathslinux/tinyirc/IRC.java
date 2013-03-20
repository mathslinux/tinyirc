package org.mathslinux.tinyirc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.util.Log;

@SuppressLint("DefaultLocale")
public class IRC extends Thread {
	private final ArrayList<IRCEventListener> listenerList;
	private BufferedWriter writer;
	private BufferedReader reader;
	private final String TAG = "IRC";
	private final String host;
	private final int port;
	private String nick;
	private final String password;
	private String channel;

	public IRC (String host, int port, String nick, String password)
			throws UnknownHostException, IOException {
		listenerList = new ArrayList<IRCEventListener>();
		this.host = host;
		this.port = port;
		this.nick = nick;
		this.password = password;
	}

	public void join(String channel) throws IOException {
		this.channel = channel;
	}

	private void send(String buffer) {
		try {
			this.writer.write(buffer);
			this.writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendMessage(String message) {
		this.send("PRIVMSG " + this.channel + " :" + message + "\n\r\n");
	}

	private void login() {
		Socket socket;
		try {
			socket = new Socket(host, port);
			writer = new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));

			// Login to the irc server.
			this.writer.write("PASS " + password + "\r\n");
			this.writer.write("NICK " + nick + "\r\n");
			this.writer.write("USER " + nick + " 8 * : Android IRC Hacks\r\n");
			this.writer.flush();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	@Override
	public void run() {
		Log.v(TAG, "Enter IRC thread");

		this.login();

		Log.v(TAG, "Enter IRC main-loop");
		String line = null;
		try {
			// Read lines from the server until it tells us we have connected.
			while ((line = reader.readLine( )) != null) {
				Log.e(TAG, "Error response11: " + line);
				if (line.indexOf("004") >= 0) {
					// We are now logged in.
					Log.w(TAG, "We have been logged in");
					break;
				} else if (line.indexOf("433") >= 0) {
					// If the nickname has been used, choose a new one
					Log.w(TAG, "Nickname is already in use");
					nick = nick + "1";
					this.send("NICK " + nick + "\r\n");
				} else {
					Log.e(TAG, "Error response: " + line);
				}
			}

			// Join the channel
			Log.w(TAG, "Join channel: " + this.channel);
			this.send("JOIN " + this.channel + "\r\n");

			while ((line = reader.readLine( )) != null) {
				Log.d(TAG, "Receive server message: " + line);
				if (line.toUpperCase().startsWith("PING ")) {
					// We must respond to PINGs to avoid being disconnected.
					Log.v(TAG, "Reveive PING from server, send a PONG");
					this.send("PONG " + line.substring(5) + "\r\n");
				}
				else {
					// Print the raw line received by the bot.
					for (Object listener : listenerList) {
						((IRCEventListener)listener).onPrivmsg(line);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void addIRCEventListener(IRCEventListener listener) {
		listenerList.add(listener);
	}

	public void removeIRCEventListener(IRCEventListener listener) {
		listenerList.remove(listener);
	}
}
