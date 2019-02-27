package com.example.linj.tcpclient;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * @author JLin
 */
public class MainActivity extends Activity {
    private TextView textView, jump;
    private EditText editText;
    private Button connect, disconnect, send;

    StringBuilder stringBuilder = new StringBuilder();

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text);
        editText = findViewById(R.id.edit);
        connect = findViewById(R.id.connect);
        disconnect = findViewById(R.id.disconnect);
        send = findViewById(R.id.send);

        jump = findViewById(R.id.jump);

        final Client client = new Client("192.168.97.116", 8080);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.connect(new SocketListener() {
                    @Override
                    public void connect() {

                    }

                    @Override
                    public void receive(String message) {
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
                        System.out.println("MainActivity " + "connectError " + "----");
                    }
                });
            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.disconnect();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.sendMessage(editText.getText().toString());
            }
        });

        jump.setOnClickListener(v -> {
            startActivity(new Intent(this, UdpClientActivity.class));
        });
    }
}
