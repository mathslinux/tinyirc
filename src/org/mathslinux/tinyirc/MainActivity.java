package org.mathslinux.tinyirc;

import java.io.IOException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "tinyirc";
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private EditText sendText;
    private Button sendButton;
    private StringBuffer sendBuffer;
    IRC irc;

    private final Handler IRCHandler =  new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO: login sucess/fail handle

            String message = (String)msg.obj;
            MainActivity.this.adapter.add(message);
            MainActivity.this.adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        // Setup GUI
        this.initGUI();

        // Initialize IRC
        this.initIRC();
    }

    private void initGUI() {
        Log.v(MainActivity.TAG, "Initialize GUI");

        this.listView = (ListView)this.findViewById(R.id.ListView);
        this.listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        this.adapter = new ArrayAdapter<String>(this, R.layout.message);
        this.listView.setAdapter(this.adapter);

        this.sendText = (EditText)this.findViewById(R.id.SendText);
        this.sendButton = (Button)this.findViewById(R.id.SendButton);
        this.sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(MainActivity.TAG, "[sendButton clicked]");
                String message = MainActivity.this.sendText.getText().toString();
                if (message.length() > 0 ) {
                    MainActivity.this.sendBuffer.setLength(0);
                    MainActivity.this.sendText.setText(MainActivity.this.sendBuffer);
                    MainActivity.this.irc.sendMessage(message);
                } else {
                    Toast.makeText(MainActivity.this.getApplicationContext(),
                            "Please input a message", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add input message to chat window
                MainActivity.this.adapter.add("Me: " + message);
            }
        });
    }

    private void initIRC() {
        Log.v(MainActivity.TAG, "Initialize IRC");

        this.sendBuffer = new StringBuffer("");

        // Define our listener for IRC
        IRCEventListener listener = new IRCEventListener() {
            @Override
            public void onPrivmsg(String message) {
                Message msg = Message.obtain();
                msg.obj = message;
                MainActivity.this.IRCHandler.sendMessage(msg);
            }
            @Override
            public void onLoginSuccess() {
            }
            @Override
            public void onLoginFailed() {
            }
        };
        try {
            // Register our listener to IRC
            this.irc = new IRC("192.168.1.101", 6667, "dunrong", "cloudtimes");
            this.irc.addIRCEventListener(listener);

            // FIXME, hardcode channel
            this.irc.join("#ct");

            // Run IRC main-loop
            this.irc.start();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
