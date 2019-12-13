package com.example.linj.tcpclient.tcpclient;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.linj.tcpclient.callback.SocketListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author JLin
 * @date 2019/2/27
 */
public class TcpClient {
    private static final String TAG = "TcpClient";

    private static final long TIME_OUT = 5000;

    /**
     * 主机地址
     */
    private String host;
    /**
     * 主机端口
     */
    private int port;
    /**
     * socket
     */
    private Socket socket;
    /**
     * 输出流，客户端发送数据
     */
    private PrintStream output;
    /**
     * 输入流，客户端接收数据
     */
    private InputStream is;

    private byte[] buff = new byte[1024];

    /**
     * 接收文本信息
     */
    private String rcvMsg;
    /**
     * 接收文本长度
     */
    private int rcvLen;

    /**
     * TcpClient 监听器
     */
    private SocketListener socketListener;

    public TcpClient(String host, int port, SocketListener socketListener) {
        this.host = host;
        this.port = port;
        this.socketListener = socketListener;
    }

    private static final int CONNECT_OK = 10001;
    private static final int CONNECT_FAIL = 10004;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECT_OK:
                    socketListener.connectOk();
                    read();
                    break;
                case CONNECT_FAIL:
                    Log.e(TAG, "--> connect fail");
                    socketListener.connectError(TcpClient.this);
                    break;
                default:
                    break;
            }
        }
    };

    public void connect() {
        Log.d(TAG, "--> connect: ");
        new Thread(() -> {
            Message message = handler.obtainMessage();

            try {
                socket = new Socket(host, port);
                socket.setSoTimeout(5000);
                socket.setTcpNoDelay(true);

                // 设置输出流的发送缓冲区大小，默认是4KB，即4096字节
                socket.setSendBufferSize(1024);
                // 设置输入流的接收缓冲区大小，默认是4KB，即4096字节
                socket.setReceiveBufferSize(1024);

                if (socket.isConnected()) {
                    output = new PrintStream(socket.getOutputStream(), true, "utf-8");
                    message.what = CONNECT_OK;
                    handler.sendMessage(message);
                } else {
                    message.what = CONNECT_FAIL;
                    handler.sendMessageDelayed(message, TIME_OUT);
                }
            } catch (IOException e) {
                e.printStackTrace();
                message.what = CONNECT_FAIL;
                handler.sendMessageDelayed(message, TIME_OUT);
            }
        }).start();
    }

    /**
     * 读取线程
     */
    private ScheduledExecutorService readService;
    private LinkedList<String> linkedList = new LinkedList<>();

    private void read() {
        if (readService == null) {
            readService = new ScheduledThreadPoolExecutor(1);
        }
        readService.scheduleAtFixedRate(() -> {
            try {
                is = socket.getInputStream();
                rcvLen = is.read(buff);
                if (rcvLen > 0) {
                    Log.d(TAG, "read: " + Arrays.toString(buff));
                    rcvMsg = new String(buff, 0, rcvLen, StandardCharsets.UTF_8);
                    linkedList.addFirst(rcvMsg);
                    socketListener.receive(rcvMsg, linkedList);
                } else {
                    Log.e(TAG, "--> disConnect");
                    socketListener.disconnect(TcpClient.this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 10, 50, TimeUnit.MILLISECONDS);
    }

    public void disconnect() {
        Log.d(TAG, "--> disconnect: ");
        if (socket.isConnected()) {
            try {
                if (readService != null && !readService.isShutdown()) {
                    readService.shutdownNow();
                    readService = null;
                }

                output.close();
                is.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(final String message) {
        new Thread(() -> {
            try {
//                output.print(message);
                byte[] bytes = {-86, 102, 0, 9, -112, 53, 113, 0, 2, 48, 0, 0, 0, 0, 0, 0, 0, 0, 102, -86};
                output.write(bytes);
                output.flush();
            } catch (Exception e) {
                Log.e(TAG, "sendMessage: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}
