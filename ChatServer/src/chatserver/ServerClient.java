/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import communication.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author beyza
 */
public class ServerClient {

    private Server server;
    private String username;
    private Socket socket;
    private ObjectOutputStream clientOutput;
    private ObjectInputStream clientInput;
    private boolean isConnected;
    private ServerClientListenThread listenThread;
    private Thread broadcastThread;
    private Message messageToBeBroadcasted;
    public ArrayList<ServerClient> direct_chats;
    public ArrayList<String> clientChatrooms;

    public ServerClient(Socket socket, Server server) {

        this.server = server;
        this.socket = socket;

        try {
            this.clientOutput = new ObjectOutputStream(this.socket.getOutputStream());
            this.clientInput = new ObjectInputStream(this.socket.getInputStream());

        } catch (IOException ex) {
            Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.listenThread = new ServerClientListenThread(this);
        this.isConnected = false;

        init_server_client();
    }

    public void init_server_client() {
        direct_chats = new ArrayList<>();
        clientChatrooms = new ArrayList<>();
    }

    public boolean getIsConnected() {
        return this.isConnected;
    }

    public Message createMessage(Message.MessageType msg_type, String receiver, String content) {

        Message newMessage = new Message(msg_type);
        newMessage.setSender(this.username);
        newMessage.setReceiver(receiver);
        newMessage.setMsg_content(content);

        return newMessage;

    }

    public boolean sendMessage(Message message) { //returns true if message sent succesfully
        if (this.isConnected) {
            try {
                clientOutput.writeObject(message);
                return true;
            } catch (IOException ex) {
                Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Server client could not send the message.");
        return false;
    }

    public ObjectInputStream getClientInput() {
        return clientInput;
    }

    public void broadcast_message(Message m) {
        final Message messageToBeBroadcasted = m;
        broadcastThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (ServerClient sc : Server.connectedClients) {
                    sc.sendMessage(messageToBeBroadcasted);
                }
            }
        });
        this.broadcastThread.start();
    }

    public void listen() {
        this.isConnected = true;
        this.listenThread.start();
    }

    public void close() {
        try {
            this.socket.close();
            this.clientInput.close();
            this.clientOutput.close();
            this.isConnected = false;
            
            //remove from existing groups
            for(String groupname: clientChatrooms){
                Server.chatrooms.get(groupname).removeMember(this);
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(ServerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void add_new_direct_chat(String new_direct, Message m) {
        for (ServerClient sc : Server.connectedClients) {
            if (sc.getUsername().equals(new_direct)) {
                sc.direct_chats.add(this);
                sc.sendMessage(m);
                this.direct_chats.add(sc);
                break;
            }
        }
    }

    public void request_new_member(String newMember, String groupName) {
        for (ServerClient sc : Server.connectedClients) {
            if (sc.getUsername().equals(newMember)) {
                Server.chatrooms.get(groupName).addMember(sc);
                sc.clientChatrooms.add(groupName);
                break;
            }
        }
    }

    public void send_direct(Message msg) {
        final Message m = msg;
        Thread newT = new Thread(new Runnable() {
            @Override
            public void run() {
                for (ServerClient sc : direct_chats) {
                    if (m.getReceiver().equals(sc.getUsername())) {
                        System.out.println(m.getSender() + " kişisi " + m.getReceiver() + " kişisine " + m.getMsg_content() + " mesajını yolluyor");
                        sc.sendMessage(m);
                        break;
                    }
                }
            }
        });
        newT.start();

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}

class ServerClientListenThread extends Thread {

    private ServerClient serverClient;
    Message message;

    public ServerClientListenThread(ServerClient serverClient) {
        this.serverClient = serverClient;
    }

    @Override
    public void run() {
        while (this.serverClient.getIsConnected()) {
            System.out.println("Waiting for message from client...");

            try {
                Message message = (Message) (this.serverClient.getClientInput().readObject());

                switch (message.getMsg_type()) {

                    case NEW_USERNAME:
                        if (!Server.usernameExists(message.getMsg_content().toString())) {
                            serverClient.setUsername(message.getMsg_content().toString());
                            serverClient.sendMessage(serverClient.createMessage(Message.MessageType.NEW_USERNAME, null, "Accepted"));
                            Server.broadcastConnectedClients();
                        } else {
                            serverClient.sendMessage(serverClient.createMessage(Message.MessageType.NEW_USERNAME, null, "Username exists"));
                        }
                        break;
                    case BROADCAST_MESSAGE:
                        serverClient.broadcast_message(message);
                        break;
                    case BROADCAST_FILE:
                        serverClient.broadcast_message(message);
                        break;
                    case DIRECT_CHAT:
                        serverClient.send_direct(message);
                        break;
                    case DIRECT_CHAT_REQUEST:
                        serverClient.add_new_direct_chat(message.getReceiver(), message);
                        break;
                    case DIRECT_FILE:
                        serverClient.send_direct(message);
                        break;
                    case GROUP_CHAT_START:
                        if (Server.chatrooms.containsKey(message.getReceiver())) {
                            this.serverClient.sendMessage(serverClient.createMessage(Message.MessageType.GROUP_CHAT_START, message.getReceiver(), "Groupchat name exists."));
                        } else {
                            Server.addGroup(message.getReceiver(), this.serverClient);
                            this.serverClient.clientChatrooms.add(message.getReceiver());
                        }
                        break;
                    case GROUP_CHAT_REQUEST://receiver is the group name, content is the new members username
                        this.serverClient.request_new_member(message.getMsg_content(), message.getReceiver());
                        break;
                    case GROUP_CHAT:
                        try {
                            Server.chatrooms.get(message.getReceiver()).sendToGroupChat(message, this.serverClient);
                        } catch (Exception e) {
                            System.out.println("Group no longer exists");
                        }

                        break;
                    case GROUP_CHAT_FILE:
                        try {
                            Server.chatrooms.get(message.getReceiver()).sendToGroupChat(message, this.serverClient);
                        } catch (Exception e) {
                            System.out.println("Group no longer exists");
                        }

                        break;
                    default:
                        System.out.println("Default");
                        break;

                }

            } catch (IOException ex) {
                this.serverClient.close();
                Server.removeClient(this.serverClient);
                Logger.getLogger(ServerClientListenThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                this.serverClient.close();
                Logger.getLogger(ServerClientListenThread.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

}
