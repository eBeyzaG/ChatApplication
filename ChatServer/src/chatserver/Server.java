/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import communication.Message;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author beyza
 */
public class Server {

    public static List<ServerClient> connectedClients;
    public static TreeMap<String, Chatroom> chatrooms;

    private ServerSocket serverSocket;
    private int port;
    private ListenThread listenThread;
    private Thread groupCleaner;

    public Server(int port) {
        try {
            this.port = port;
            this.serverSocket = new ServerSocket(port);
            this.listenThread = new ListenThread(this);

        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        init_server();
    }

    public void init_server() {
        connectedClients = new ArrayList<>();
        chatrooms = new TreeMap<String, Chatroom>();

        groupCleaner = new Thread(new Runnable() {
            @Override
            public void run() {
                //todo
                for (Map.Entry<String, Chatroom> entry : chatrooms.entrySet()) {
                    if (entry.getValue().getMemberCount() == 0) {
                        removeGroup(entry.getValue().getGroupName());
                    }
                }
            }
        });

        groupCleaner.start();

    }

    public static synchronized void addGroup(String groupName, ServerClient sc) {
        chatrooms.put(groupName, new Chatroom(groupName, sc));

    }

    public synchronized void removeGroup(String groupName) {
        chatrooms.remove(groupName);
    }

    public synchronized void addClient(ServerClient newClient) {
        connectedClients.add(newClient);
    }

    public static synchronized void removeClient(ServerClient removedClient) {

        for (int i = 0; i < connectedClients.size(); i++) {

            if (connectedClients.get(i).getUsername().equals(removedClient.getUsername())) {
                connectedClients.remove(i);
            }

        }

    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void listen() {
        this.listenThread.start();
    }

    public static boolean usernameExists(String username) {

        for (ServerClient scli : connectedClients) {
            if (username.equals(scli.getUsername())) {
                return true;
            }
        }

        return false;
    }

    public static void broadcastConnectedClients() {

        String clientListText = "";
        for (ServerClient sc : connectedClients) {
            if (!sc.getUsername().equals(null)) {
                clientListText += sc.getUsername() + "\n";
            }
        }

        for (ServerClient sc : connectedClients) {
            sc.sendMessage(sc.createMessage(Message.MessageType.CONNECTED_CLIENTS, null, clientListText));
        }

    }
}

class ListenThread extends Thread {

    private Server server;

    public ListenThread(Server server) {
        this.server = server;
    }

    @Override
    public void run() {

        while (!this.server.getServerSocket().isClosed()) {
            System.out.println("Server is listening for clients...");
            try {
                Socket newSocket = this.server.getServerSocket().accept();
                System.out.println("A new client has connected.");

                ServerClient newClient = new ServerClient(newSocket, this.server);
                newClient.listen();
                server.addClient(newClient);

            } catch (IOException ex) {
                Logger.getLogger(ListenThread.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

}
