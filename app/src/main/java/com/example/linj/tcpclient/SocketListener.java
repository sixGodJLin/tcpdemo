package com.example.linj.tcpclient;

/**
 * @author JLin
 * @date 2019/1/30
 */
public interface SocketListener {
    void connect();

    void receive(String message);

    void disconnect();

    void connectError();
}
