package com.example.linj.tcpclient;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author JLin
 * @date 2019/2/27
 */
public class UdpClientActivity extends AppCompatActivity {
    EditText msg_et = null;
    Button send_bt = null;
    TextView info_tv = null;
    private static final String TAG = "MainAct";
    private UDPClient client;
    private String sendInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udp_client);
        msg_et = findViewById(R.id.edit_msg);
        send_bt = findViewById(R.id.send_bt);
        info_tv = findViewById(R.id.receive_msg);
        info_tv.setText("source");

        // 发送消息
        send_bt.setOnClickListener(v -> {
            MyThread thread = new MyThread();
            new Thread(thread).start();
        });


        // 开启服务器
        ExecutorService exec = Executors.newCachedThreadPool();
        UDPServer server = new UDPServer();
        exec.execute(server);

    }

    @SuppressLint("HandlerLeak")
    final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            info_tv.setText(sendInfo);
        }
    };

    class MyThread implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "MyThread send :" + msg_et.getText().toString());
            client = new UDPClient(msg_et.getText().toString());
            sendInfo = client.send();
            Message msg = mHandler.obtainMessage();
            msg.arg1 = 1;
            mHandler.sendMessage(msg);
        }
    }
}