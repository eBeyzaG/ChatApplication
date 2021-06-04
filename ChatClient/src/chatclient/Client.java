/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import communication.Message;
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
 * client model
 */
public class Client {

    private String username;
    private Socket socket;
    private ObjectInputStream clientInput;
    private ObjectOutputStream clientOutput;

    private boolean isConnected;
    private String server_ip;
    private int serverPort;


    public Client(String server_ip, int serverPort) {

        this.server_ip = server_ip;
        this.serverPort = serverPort;
        this.isConnected = false;
        connect();
        // startListenThread();

    }

    private void connect() {//connects the client to server

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



    public void close() {//terminate the client
        try {
            this.socket.close();
            this.clientOutput.close();
            this.clientInput.close();
            this.isConnected = false;
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendMessage(Message message){//send message from client to server
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



