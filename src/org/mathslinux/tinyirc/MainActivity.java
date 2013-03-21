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
            String message = (String)msg.obj;
            adapter.add(message);
            adapter.notifyDataSetChanged();
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

        listView = (ListView)this.findViewById(R.id.ListView);
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        adapter = new ArrayAdapter<String>(this, R.layout.message);
        listView.setAdapter(adapter);

        sendText = (EditText)this.findViewById(R.id.SendText);
        sendButton = (Button)this.findViewById(R.id.SendButton);
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(MainActivity.TAG, "[sendButton clicked]");
                String message = sendText.getText().toString();
                if (message.length() > 0 ) {
                    sendBuffer.setLength(0);
                    sendText.setText(sendBuffer);
                    irc.sendMessage(message);
                } else {
                    Toast.makeText(MainActivity.this.getApplicationContext(),
                            "Please input a message", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add input message to chat window
                adapter.add("Me: " + message);
            }
        });
    }

    private void initIRC() {
        Log.v(MainActivity.TAG, "Initialize IRC");

        sendBuffer = new StringBuffer("");

        // Define our listener for IRC
        IRCEventListener listener = new IRCEventListener() {
            @Override
            public void onPrivmsg(String message) {
                Message msg = Message.obtain();
                msg.obj = message;
                IRCHandler.sendMessage(msg);
            }
        };
        try {
            // Register our listener to IRC
            irc = new IRC("192.168.0.200", 6667, "dunrong", "cloudtimes");
            irc.addIRCEventListener(listener);

            // FIXME, hardcode channel
            irc.join("#ct");

            // Run IRC main-loop
            irc.start();
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
