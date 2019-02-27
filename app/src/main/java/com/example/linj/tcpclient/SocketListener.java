package com.example.linj.tcpclient;

import java.util.LinkedList;

/**
 * @author JLin
 * @date 2019/1/30
 */
public interface SocketListener {
    void connect();

    void receive(String message, LinkedList<String> linkedList);

    void disconnect();

    void connectError();
}
