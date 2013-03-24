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
        this.listenerList = new ArrayList<IRCEventListener>();
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

    private boolean login() {
        try {
            Log.v(this.TAG, "Prepare to login to: " + this.host + ":" + this.port);

            Socket socket;
            String line;
            socket = new Socket(this.host, this.port);
            this.writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            // Login to the irc server.
            this.writer.write("PASS " + this.password + "\r\n");
            this.writer.write("NICK " + this.nick + "\r\n");
            this.writer.write("USER " + this.nick + " 8 * : Android IRC Hacks\r\n");
            this.writer.flush();

            // Read lines from the server until it tells us we have connected.
            while ((line = this.reader.readLine( )) != null) {
                if (line.indexOf("004") >= 0) {
                    // We are now logged in.
                    Log.v(this.TAG, "We are now logged in");
                    break;
                } else if (line.indexOf("433") >= 0) {
                    // If the nickname has been used, choose a new one
                    Log.w(this.TAG, "Nickname is already in use");
                    this.nick = this.nick + "1";
                    this.send("NICK " + this.nick + "\r\n");
                } else {
                    Log.v(this.TAG, "Response during logining: " + line);
                }
            }
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            Log.e(this.TAG, "Hostname error");
            for (Object listener : this.listenerList) {
                ((IRCEventListener)listener).onLoginFailed();
            }
            return false;
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            Log.e(this.TAG, "Login error: IOException");
            for (Object listener : this.listenerList) {
                ((IRCEventListener)listener).onLoginFailed();
            }
            return false;
        }
        for (Object listener : this.listenerList) {
            ((IRCEventListener)listener).onLoginSuccess();
        }
        return true;
    }
    @Override
    public void run() {
        Log.v(this.TAG, "Enter IRC thread");

        if (this.login() == false) {
            return;
        }

        Log.v(this.TAG, "Enter IRC main-loop");
        String line = null;
        try {
            // Join the channel
            Log.w(this.TAG, "Join channel: " + this.channel);
            this.send("JOIN " + this.channel + "\r\n");

            while ((line = this.reader.readLine( )) != null) {
                Log.d(this.TAG, "Receive server message: " + line);
                if (line.toUpperCase().startsWith("PING ")) {
                    // We must respond to PINGs to avoid being disconnected.
                    Log.v(this.TAG, "Reveive PING from server, send a PONG");
                    this.send("PONG " + line.substring(5) + "\r\n");
                }
                else {
                    // Print the raw line received by the bot.
                    for (Object listener : this.listenerList) {
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
        this.listenerList.add(listener);
    }

    public void removeIRCEventListener(IRCEventListener listener) {
        this.listenerList.remove(listener);
    }
}
