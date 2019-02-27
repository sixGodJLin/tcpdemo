package com.example.linj.tcpclient;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author JLin
 * @date 2019/2/27
 */
public class UDPClient {
    private static final int SERVER_PORT = 8080;
    private DatagramSocket dSocket = null;
    private String msg;

    public UDPClient(String msg) {
        super();
        this.msg = msg;
    }

    public String send() {
        StringBuilder sb = new StringBuilder();
        InetAddress local = null;
        try {
            local = InetAddress.getByName("192.168.97.116");
            sb.append("已找到服务器,连接中...").append("\n");
        } catch (UnknownHostException e) {
            sb.append("未找到服务器.").append("\n");
            e.printStackTrace();
        }
        try {
            // 注意此处要先在配置文件里设置权限,否则会抛权限不足的异常
            dSocket = new DatagramSocket();
            sb.append("正在连接服务器...").append("\n");
        } catch (SocketException e) {
            e.printStackTrace();
            sb.append("服务器连接失败.").append("\n");
        }
        int length = msg == null ? 0 : msg.length();
        System.out.println("UDPClient " + "send " + "----1111111" );
        DatagramPacket dPacket = new DatagramPacket(msg.getBytes(), length,
                local, SERVER_PORT);
        try {
            dSocket.send(dPacket);
            System.out.println("UDPClient " + "send " + "----" + "msg=" + msg);
            sb.append("消息发送成功!").append("\n");
        } catch (IOException e) {
            e.printStackTrace();
            sb.append("消息发送失败.").append("\n");
        }
        dSocket.close();
        return sb.toString();
    }
}
