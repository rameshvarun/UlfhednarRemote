package net.varunramesh.ulfhednarremote;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by Varun on 6/9/2015.
 */
public class DiscoverServersTask extends AsyncTask<Object, Object, Object> {
    private static String DISCOVERY_MESSAGE = "ULFHEDNAR_REMOTE_DISCOVERY";
    private static String ANNOUNCE_MESSAGE = "ULFHEDNAR_REMOTE_ANNOUNCE";

    private static int DISCOVERY_PORT = 3779;

    private Context context;
    public DiscoverServersTask(Context context) {
        this.context = context;
    }

    @Override
    protected Object doInBackground(Object... params) {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buffer = DISCOVERY_MESSAGE.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
            socket.send(packet);
        } catch (SocketException e) {
            // TODO: Handle SocketException
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO: Handle UnkownHostException
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: Handle IOException
            e.printStackTrace();
        }

        return null;
    }
}