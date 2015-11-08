package net.varunramesh.ulfhednarremote;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Varun on 6/10/2015.
 */
public class NetworkManager implements Runnable {
    private static final String TAG = "NetworkManager";
    private static final int TICK = 500;

    private AtomicBoolean running = new AtomicBoolean(true);
    private final RemoteState state;
    private final ServerInfo server;

    public NetworkManager(RemoteState state, ServerInfo server) {
        this.state = state;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();

            while (running.get()) {
                synchronized(this) {
                    try { wait(TICK); }
                    catch (InterruptedException e) { }
                }

                byte[] message = state.toString().getBytes();
                DatagramPacket packet = new DatagramPacket(message, message.length, server.getAddress(), server.getPort());
                socket.send(packet);
            }

            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running.set(false);
    }
}
