package com.example.linj.tcpclient.callback;

import com.example.linj.tcpclient.tcpclient.TcpClient;

import java.util.LinkedList;

/**
 * @author JLin
 * @date 2019/1/30
 */
public interface SocketListener {
    void connectOk();

    void receive(String message, LinkedList<String> linkedList);

    void disconnect(TcpClient tcpClient);

    void connectError(TcpClient tcpClient);
}
