/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

/**
 *
 * @author beyza
 */
public class Client {

    private String username;
    private Socket socket;
    private ObjectInputStream clientInput;
    private ObjectOutputStream clientOutput;

    private boolean isConnected;
    private String server_ip;
    private int serverPort;

    private ClientListenThread listenThread;

    public Client(String server_ip, int serverPort) {

        this.server_ip = server_ip;
        this.serverPort = serverPort;
        this.isConnected = false;
        connect();
        // startListenThread();

    }

    private void connect() {

        System.out.println("Connecting to server...");

        try {
            this.socket = new Socket(this.server_ip, this.serverPort);
            System.out.println("Connected");

            this.clientOutput = new ObjectOutputStream(this.socket.getOutputStream());
            this.clientInput = new ObjectInputStream(this.socket.getInputStream());
            this.isConnected = true;
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void startListenThread() {
        this.listenThread = new ClientListenThread(this);
        this.isConnected = true;
        this.listenThread.start();
    }

    public void close() {
        try {
            this.socket.close();
            this.clientOutput.close();
            this.clientInput.close();
            this.isConnected = false;
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendMessage(Object message){
        try {
            clientOutput.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public boolean getIsConnected() {
        return this.isConnected;
    }

    public ObjectInputStream getClientInput() {
        return clientInput;
    }

}


//redundant
class ClientListenThread extends Thread {

    private Client client;

    public ClientListenThread(Client client) {
        this.client = client;
    }

    @Override
    public void run() {

        while (this.client.getIsConnected()) {

        }
    }

}
