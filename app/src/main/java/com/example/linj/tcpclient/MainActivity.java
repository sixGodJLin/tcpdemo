package com.example.linj.tcpclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.linj.tcpclient.tcpclient.TcpClient;

import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author JLin
 */
public class MainActivity extends Activity {
    private TextView textView, jump;
    private EditText editText;
    private Button connect, disconnect, clear, send;

    StringBuilder stringBuilder = new StringBuilder();
    private LinkedList<String> strings;

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text);
        editText = findViewById(R.id.edit);
        connect = findViewById(R.id.connect);
        disconnect = findViewById(R.id.disconnect);
        clear = findViewById(R.id.clear);
        send = findViewById(R.id.send);

        jump = findViewById(R.id.jump);

        final TcpClient client = new TcpClient("192.168.97.116", 8080);

        connect.setOnClickListener(v ->
                client.connect(new SocketListener() {
                    @Override
                    public void connect() {

                    }

                    @Override
                    public void receive(String message, LinkedList<String> linkedList) {
                        strings = linkedList;

                        count++;
                        stringBuilder.append(count).append(":").append(message).append("\n");
                        textView.setText(stringBuilder.toString());
                        if (count % 40 == 0) {
                            stringBuilder = new StringBuilder();
                        }
                    }

                    @Override
                    public void disconnect() {

                    }

                    @Override
                    public void connectError() {
                        Toast.makeText(MainActivity.this, "连接失败，正在尝试重新连接", Toast.LENGTH_SHORT).show();
                    }
                }));

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
            if (executor == null) {
                executor = new ScheduledThreadPoolExecutor(1);
            }
            final int[] sendCount = {0};
            executor.scheduleAtFixedRate(() -> {
                System.out.println("MainActivity " + "onCreate " + "----" + sendCount[0]++);
                client.sendMessage(editText.getText().toString());
            }, 10, 50, TimeUnit.MILLISECONDS);
        });

        jump.setOnClickListener(v -> startActivity(new Intent(this, UdpClientActivity.class)));
    }

    ScheduledThreadPoolExecutor executor;
}
