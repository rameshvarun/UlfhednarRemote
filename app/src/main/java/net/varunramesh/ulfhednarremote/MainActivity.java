package net.varunramesh.ulfhednarremote;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity {
    public static final String TAG = "MainActivity";
    // private ArrayList<NsdServiceInfo> servers = new ArrayList<>();
    // private ArrayAdapter<NsdServiceInfo> serverListAdapter;

    private DiscoverServersTask discoverServersTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        FloatingActionButton refreshButton = (FloatingActionButton) findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshServers();
            }
        });
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
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Resuming activity...");
        super.onResume();
        refreshServers();
    }

    public void refreshServers() {
        // If there is already a task running, cancel it.
        if (discoverServersTask != null) {
            discoverServersTask.cancel(true);
        }

        // Start a new DiscoverServersTask.
        discoverServersTask = new DiscoverServersTask(this,
                (ListView) findViewById(R.id.serverlist),
                (ProgressBar) findViewById(R.id.progressBar),
                (TextView) findViewById(R.id.noserversfound));
        discoverServersTask.execute();
    }
}
