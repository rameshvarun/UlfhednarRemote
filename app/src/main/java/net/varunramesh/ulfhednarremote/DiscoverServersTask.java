package net.varunramesh.ulfhednarremote;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Debug;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    public static final String DISCOVERY_MESSAGE = "ULFHEDNAR_REMOTE_DISCOVERY";
    public static final String ANNOUNCE_MESSAGE = "ULFHEDNAR_REMOTE_ANNOUNCE";
    public static final int DISCOVERY_PORT = 3779;
    public static final long SEARCH_TIME = 5*1000;
    public static final int SEND_TIMEOUT = 500;
    public static final String TAG = "DiscoverServersTask";

    private final Context context;

    private final ListView serverlist;
    private final ProgressBar spinner;
    private final TextView noservers;

    private final ArrayAdapter<ServerInfo> serverAdapter;

    public DiscoverServersTask(Context context, ListView serverlist, ProgressBar spinner,
                               TextView noservers) {
        this.context = context;
        this.serverlist = serverlist;
        this.spinner = spinner;
        this.noservers = noservers;

        this.serverAdapter = new ArrayAdapter<ServerInfo>(context, R.layout.server_item) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final ServerInfo server = getItem(position);

                if (convertView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(R.layout.server_item, parent, false);
                }
                ((TextView)convertView.findViewById(R.id.serverName)).setText(server.getHostname());
                ((ImageButton)convertView.findViewById(R.id.playButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Launching Remote Activity");
                        Intent intent = new Intent(getContext(), RemoteActivity.class);
                        intent.putExtra("server", server);
                        getContext().startActivity(intent);
                    }
                });
                return convertView;
            }
        };
    }

    @Override
    protected void onPreExecute() {
        serverlist.setAdapter(this.serverAdapter);
        spinner.setVisibility(View.VISIBLE);
        noservers.setVisibility(View.GONE);
    }

    @Override
    protected void onPostExecute(Collection<ServerInfo> result) {
        spinner.setVisibility(View.GONE);
        if(result == null || result.size() == 0) {
            noservers.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onProgressUpdate(ServerInfo... servers) {
        for (ServerInfo s : servers) this.serverAdapter.add(s);
    }

    @Override
    protected Collection<ServerInfo> doInBackground(Object... params) {
        Set<ServerInfo> servers = new HashSet<ServerInfo>(); // Keep track of the servers seen so far.

        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(SEND_TIMEOUT);
            byte[] buffer = DISCOVERY_MESSAGE.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                    InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);

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
                    if(msg.startsWith(ANNOUNCE_MESSAGE)) {
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