package com.destroystokyo.paper.network;

import net.minecraft.network.NetworkManager;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;

public class PaperNetworkClient implements NetworkClient {

    private final NetworkManager networkManager;

    PaperNetworkClient(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public InetSocketAddress getAddress() {
        return (InetSocketAddress) this.networkManager.getSocketAddress();
    }

    @Override
    public int getProtocolVersion() {
        return this.networkManager.protocolVersion.id();
    }

    @Nullable
    @Override
    public InetSocketAddress getVirtualHost() {
        //return this.networkManager.virtualHost;
        return (InetSocketAddress) this.networkManager.socketAddress; //Crucible - Stub
    }

    public static InetSocketAddress prepareVirtualHost(String host, int port) {
        int len = host.length();

        // FML appends a marker to the host to recognize FML clients (\0FML\0)
        int pos = host.indexOf('\0');
        if (pos >= 0) {
            len = pos;
        }

        // When clients connect with a SRV record, their host contains a trailing '.'
        if (len > 0 && host.charAt(len -  1) == '.') {
            len--;
        }

        return InetSocketAddress.createUnresolved(host.substring(0, len), port);
    }

}
