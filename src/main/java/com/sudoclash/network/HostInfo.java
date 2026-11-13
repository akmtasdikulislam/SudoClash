package com.sudoclash.network;

// A host found on the network: where to connect and what it is called.
public class HostInfo {
    private final String address;
    private final int port;
    private final String name;

    public HostInfo(String address, int port, String name) {
        this.address = address;
        this.port = port;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (" + address + ":" + port + ")";
    }
}
