package com.example.linj.tcpclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.linj.tcpclient.callback.SocketListener;
import com.example.linj.tcpclient.tcpclient.TcpClient;
import com.example.linj.tcpclient.udp.UdpClientActivity;

import java.util.LinkedList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author JLin
 */
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private TextView textView;

    StringBuilder stringBuilder = new StringBuilder();

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text);
        Button connect = findViewById(R.id.connect);
        Button disconnect = findViewById(R.id.disconnect);
        Button clear = findViewById(R.id.clear);
        Button send = findViewById(R.id.send);

        TextView jump = findViewById(R.id.jump);

        TcpClient client = new TcpClient("192.168.1.100", 8080, new SocketListener() {
            @Override
            public void connectOk() {
                Log.d(TAG, "connectOk: ");
            }

            @Override
            public void receive(String message, LinkedList<String> linkedList) {
                Log.d(TAG, "receive: " + message);

                count++;
                stringBuilder.append(count).append(":").append(linkedList.pollFirst()).append("\n");
                textView.setText(stringBuilder.toString());
                if (count % 40 == 0) {
                    stringBuilder = new StringBuilder();
                }
            }

            @Override
            public void disconnect(TcpClient tcpClient) {
                Log.e(TAG, "disconnect: ");
                tcpClient.disconnect();

                tcpClient.connect();
            }

            @Override
            public void connectError(TcpClient tcpClient) {
                Log.e(TAG, "connectError: ");
                tcpClient.connect();
            }
        });

        connect.setOnClickListener(v -> client.connect());

        disconnect.setOnClickListener(v -> {
            client.disconnect();
            if (executor != null) {
                executor.shutdownNow();
                executor = null;
            }
        });

        clear.setOnClickListener(v -> {
            stringBuilder = new StringBuilder();
            textView.setText("");
            count = 0;
        });

        send.setOnClickListener(v -> {
//            if (executor == null) {
//                executor = new ScheduledThreadPoolExecutor(1);
//            }
//            executor.scheduleAtFixedRate(() -> client.sendMessage(""), 10, 5000, TimeUnit.MILLISECONDS);
            client.sendMessage("");
        });

        jump.setOnClickListener(v -> startActivity(new Intent(this, UdpClientActivity.class)));
    }

    ScheduledThreadPoolExecutor executor;
}
