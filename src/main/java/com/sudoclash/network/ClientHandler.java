package com.sudoclash.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Server server;
    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile String username;
    private volatile Room room;
    private volatile boolean closed;

    public ClientHandler(Server server, Socket socket) {
        super("ClientHandler");
        this.server = server;
        this.socket = socket;
        setDaemon(true);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Who is on the other end (used to limit wrong password attempts per computer).
    public String getRemoteAddress() {
        return socket.getInetAddress() == null ? "unknown" : socket.getInetAddress().getHostAddress();
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            while (!closed && !isInterrupted()) {
                Object object = in.readObject();
                if (object instanceof NetworkMessage) {
                    server.handleMessage(this, (NetworkMessage) object);
                }
            }
        } catch (EOFException e) {
            // the player closed the connection
        } catch (IOException | ClassNotFoundException e) {
            if (!closed) {
                System.err.println("Client " + username + " disconnected: " + e);
            }
        } finally {
            server.clientDisconnected(this);
            close();
        }
    }

    // Several threads may send to the same player, one at a time.
    public synchronized void send(NetworkMessage message) {
        if (closed || out == null) {
            return;
        }
        try {
            out.writeObject(message);
            out.flush();
            out.reset();
        } catch (IOException e) {
            close();
        }
    }

    public void close() {
        closed = true;
        try {
            socket.close();
        } catch (IOException e) {
            // already closed
        }
        if (Thread.currentThread() != this) {
            interrupt();
        }
    }
}
