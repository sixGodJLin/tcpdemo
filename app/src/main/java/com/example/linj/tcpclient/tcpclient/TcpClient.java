package com.example.linj.tcpclient.tcpclient;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.example.linj.tcpclient.SocketListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author JLin
 * @date 2019/2/27
 */
public class TcpClient {
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

    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private static final int CONNECT_OK = 10001;
    private static final int CONNECT_FAIL = 10004;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            SocketListener socketListener = (SocketListener) msg.obj;
            switch (msg.what) {
                case CONNECT_OK:
                    read(socketListener);
                    break;
                case CONNECT_FAIL:
                    socketListener.connectError();
                    break;
                default:
                    break;
            }
        }
    };

    public void connect(final SocketListener socketListener) {
        new Thread(() -> {
            Message message = handler.obtainMessage();

            try {
                socket = new Socket(host, port);
                socket.setSoTimeout(5000);
                socket.setTcpNoDelay(true);

                // 设置输出流的发送缓冲区大小，默认是4KB，即4096字节
                socket.setSendBufferSize(4096);
                // 设置输入流的接收缓冲区大小，默认是4KB，即4096字节
                socket.setReceiveBufferSize(4096);

                if (socket.isConnected()) {
                    output = new PrintStream(socket.getOutputStream(), true, "utf-8");

                    message.what = CONNECT_OK;
                    message.obj = socketListener;
                    handler.sendMessage(message);
                } else {
                    message.what = CONNECT_FAIL;
                    message.obj = socketListener;
                    handler.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private ScheduledThreadPoolExecutor executor;
    private LinkedList<String> linkedList = new LinkedList<>();

    private void read(final SocketListener socketListener) {
        if (executor == null) {
            executor = new ScheduledThreadPoolExecutor(1);
        }
        executor.scheduleAtFixedRate(() -> {
            try {
                is = socket.getInputStream();
                rcvLen = is.read(buff);
                if (rcvLen > 0) {
                    rcvMsg = new String(buff, 0, rcvLen, "utf-8");
                    linkedList.addFirst(rcvMsg);
                    socketListener.receive(rcvMsg, linkedList);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 10, 50, TimeUnit.MILLISECONDS);
    }

    public void disconnect() {
        if (socket.isConnected()) {
            try {
                executor.shutdownNow();
                executor = null;

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
                output.print(message);
                output.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
