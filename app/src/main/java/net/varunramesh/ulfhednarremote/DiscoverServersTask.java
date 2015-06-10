package net.varunramesh.ulfhednarremote;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Debug;
import android.util.Log;
import android.widget.ListView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Varun on 6/9/2015.
 */
public class DiscoverServersTask extends AsyncTask<Object, ServerInfo, Collection<ServerInfo>> {
    public static String DISCOVERY_MESSAGE = "ULFHEDNAR_REMOTE_DISCOVERY";
    public static String ANNOUNCE_MESSAGE = "ULFHEDNAR_REMOTE_ANNOUNCE";
    public static int DISCOVERY_PORT = 3779;
    public static long SEARCH_TIME = 3*1000;
    public static int SEND_TIMEOUT = 500;
    public static String TAG = "DiscoverServersTask";

    private Context context;
    private ListView serverlist;

    public DiscoverServersTask(Context context, ListView serverlist) {
        this.context = context;
        this.serverlist = serverlist;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onPostExecute(Collection<ServerInfo> result) {
        if(result != null && result.size() > 0) {
            ServerInfo server = result.iterator().next();

            Log.d(TAG, "Launching Remote Activity");
            Intent intent = new Intent(context, RemoteActivity.class);
            intent.putExtra("server", server);
            context.startActivity(intent);
        }
    }

    @Override
    protected void onProgressUpdate(ServerInfo... servers) {

    }

    @Override
    protected Collection<ServerInfo> doInBackground(Object... params) {
        Set<ServerInfo> servers = new HashSet<ServerInfo>();
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(SEND_TIMEOUT);
            byte[] buffer = DISCOVERY_MESSAGE.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);

            long start_time = System.currentTimeMillis();
            while(System.currentTimeMillis() < start_time + SEARCH_TIME) {
                Log.d(TAG, "Sending discovery message to broadcast address...");
                socket.send(packet);

                try {
                    byte[] buf = new byte[1024];
                    DatagramPacket response = new DatagramPacket(buf, buf.length);
                    socket.receive(response);
                    String msg = new String(buf, 0, response.getLength());

                    Log.v(TAG, msg);
                    if(msg.equals(ANNOUNCE_MESSAGE)) {
                        ServerInfo info = new ServerInfo(response.getAddress(), response.getPort());
                        if(!servers.contains(info)) {
                            Log.d(TAG, "Discovered new server...");
                            servers.add(info);
                            publishProgress(info);
                        }
                    }
                } catch(SocketTimeoutException e) {
                    Log.d(TAG, "No response received...");
                }
            }

            Log.d(TAG, servers.size() + " servers found...");
            socket.close();

            return servers;
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