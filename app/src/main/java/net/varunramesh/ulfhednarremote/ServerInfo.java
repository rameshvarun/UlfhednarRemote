package net.varunramesh.ulfhednarremote;

import android.widget.SearchView;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by Varun on 6/10/2015.
 */
public final class ServerInfo implements Serializable {
    private final int port;
    private final InetAddress address;
    private final String hostname;

    public ServerInfo(InetAddress address, int port) {
        this.port = port;
        this.address = address;
        this.hostname = address.getHostName();
    }

    public String getHostname() { return hostname; }
    public int getPort() { return port; }
    public InetAddress getAddress() { return address; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ServerInfo)) return false;
        ServerInfo that = (ServerInfo) o;
        return port == that.port && address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return port ^ address.hashCode();
    }
}
