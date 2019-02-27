package com.example.linj.tcpclient;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * @author JLin
 * @date 2019/2/27
 */
public class UDPServer implements Runnable {
    private static final int PORT = 8080;
    private byte[] msg = new byte[2048];
    private boolean life = true;

    public UDPServer() {
    }

    public boolean isLife() {
        return life;
    }

    public void setLife(boolean life) {
        this.life = life;
    }

    @Override
    public void run() {
        DatagramSocket dSocket = null;
        DatagramPacket dPacket = new DatagramPacket(msg, msg.length);
        try {
            dSocket = new DatagramSocket(PORT);
            while (life) {
                try {
                    dSocket.receive(dPacket);
                    String receive = new String(dPacket.getData(), 0, dPacket.getLength(), "utf-8");
                    System.out.println("UDPServer " + "收到的内容为： " + "----" + receive);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
