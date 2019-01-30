package com.example.linj.tcpclient;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author JLin
 * @date 2019/1/30
 */
public class Client {
    private String host;
    private int port;

    private Socket socket;
    private PrintStream output;

    private InputStream is;
    private DataInputStream dis;

    private byte[] buff = new byte[1024];

    private String rcvMsg;
    private int rcvLen;

    private ExecutorService executorService;

    private ScheduledThreadPoolExecutor executor;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect(final SocketListener socketListener) {
        if (executorService == null || executorService.isShutdown()) {
            executorService = new ScheduledThreadPoolExecutor(1);
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(host, port);
                    if (socket.isConnected()) {
                        output = new PrintStream(socket.getOutputStream(), true, "utf-8");
                        is = socket.getInputStream();
                        dis = new DataInputStream(is);
                        read(socketListener);
                    }
                } catch (IOException e) {
                    socketListener.connectError();
                    e.printStackTrace();
                }
            }
        });
    }

    private void read(final SocketListener socketListener) {
        if (executor == null) {
            executor = new ScheduledThreadPoolExecutor(1);
        }
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    rcvLen = dis.read(buff);
                    rcvMsg = new String(buff, 0, rcvLen, "utf-8");
                    socketListener.receive(rcvMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }

    public void disconnect() {
        if (socket.isConnected()) {
            try {
                output.close();
                is.close();
                dis.close();
                socket.close();

                executor.shutdown();
                executor = null;
                executorService.shutdown();
                executorService = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(final String message) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                output.print(message);
            }
        });
    }
}
