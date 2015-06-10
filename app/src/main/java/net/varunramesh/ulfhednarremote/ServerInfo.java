package net.varunramesh.ulfhednarremote;

import android.widget.SearchView;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by Varun on 6/10/2015.
 */
public final class ServerInfo implements Serializable {
    public final int port;
    public final InetAddress address;

    public ServerInfo(InetAddress address, int port) {
        this.port = port;
        this.address = address;
    }

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
