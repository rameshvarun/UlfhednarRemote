package net.varunramesh.ulfhednarremote;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements NsdManager.DiscoveryListener {
    public static final String TAG = "MainActivity";
    public static final String SERVICE_TYPE = "_ulfhednar._udp.";


    private NsdManager nsdManager;
    private ListView serverlist;

    private ArrayList<NsdServiceInfo> servers = new ArrayList<>();
    private ArrayAdapter<NsdServiceInfo> serverListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        serverlist = (ListView) findViewById(R.id.serverlist);

        serverListAdapter = new ArrayAdapter<NsdServiceInfo>(this, R.layout.server_item, servers) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final NsdServiceInfo server = getItem(position);

                if (convertView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(R.layout.server_item, parent, false);
                }
                ((TextView)convertView.findViewById(R.id.serverName)).setText(server.getServiceName());
                ((ImageButton)convertView.findViewById(R.id.playButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Launching Remote Activity");
                        Intent intent = new Intent(getContext(), RemoteActivity.class);
                        intent.putExtra("server", new ServerInfo(server.getHost(), server.getPort()));
                        getContext().startActivity(intent);
                    }
                });
                return convertView;
            }
        };
        serverlist.setAdapter(serverListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Pausing activity...");
        if (nsdManager != null) {
            nsdManager.stopServiceDiscovery(this);
        }
        super.onPause();
        }

    public void rerenderServerList() {
        serverlist.post(new Runnable() {
            @Override
            public void run() {
                serverListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Resuming activity...");

        servers.clear();
        rerenderServerList();

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, this);
        super.onResume();
    }

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        Log.e(TAG, "Failed to start discovery for " + serviceType + ". Error code: " + errorCode);
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
        Log.e(TAG, "Failed to stop discovery for " + serviceType + ". Error code: " + errorCode);
    }

    @Override
    public void onDiscoveryStarted(String serviceType) {
        Log.d(TAG, "Discovery started: " + serviceType);
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.d(TAG, "Discovery stopped: " + serviceType);
    }

    @Override
    public void onServiceFound(NsdServiceInfo service) {
        Log.d(TAG, "Service found: " + service);
        nsdManager.resolveService(service, new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed: " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                resolvedServiceFound(serviceInfo);
            }
        });
    }

    @Override
    public void onServiceLost(NsdServiceInfo service) {
        Log.d(TAG, "Service lost: " + service);
        for(int i = 0; i < servers.size(); ++i) {
            if (servers.get(i).getServiceName().equals(service.getServiceName())) {
                servers.remove(i);
                break;
            }
        }
        rerenderServerList();
    }

    public void resolvedServiceFound(NsdServiceInfo service) {
        Log.d(TAG, "Resolve Succeeded: " + service);

        for(int i = 0; i < servers.size(); ++i) {
            if (servers.get(i).getServiceName().equals(service.getServiceName()))
                return;
        }
        servers.add(service);
        rerenderServerList();
    }
}
