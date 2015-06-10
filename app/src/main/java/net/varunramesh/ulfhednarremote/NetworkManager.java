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
    public static String TAG = "NetworkManager";
    public static int TICK = 500;

    public AtomicBoolean running = new AtomicBoolean(true);
    public final RemoteState state;
    public final ServerInfo server;

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

                Log.v(TAG, "Sending remote state update packet...");
                byte[] message = state.toString().getBytes();
                DatagramPacket packet = new DatagramPacket(message, message.length, server.address, server.port);
                socket.send(packet);
            }

            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
