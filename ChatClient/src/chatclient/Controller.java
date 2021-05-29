/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import communication.Message;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author beyza
 */
public class Controller {

    ClientFrame clientFrame;
    Client client;
    Object lastMessage;
    Thread lisThread;
    String connectedClients;
    ArrayList<ChattingFrame> chats;

    public Controller(ClientFrame clientFrame, Client client) {
        this.clientFrame = clientFrame;
        chats = new ArrayList<>();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                clientFrame.setVisible(true);
            }
        });

    }

    public void init_controller() {
        //  clientFrame.getConnectButton().addActionListener(ae -> saveUsername());
        //connect button listeners
        lisThread = new Thread(new Runnable() {
            public void run() {
                while (client.getIsConnected()) {
                    try {
                        System.out.println("Waiting for message from server.");

                        Message newmsg = (Message) (client.getClientInput().readObject());
                        System.out.println("sender: " + newmsg.getSender() + " receiver:" + newmsg.getReceiver());

                        switch (newmsg.getMsg_type()) {
                            case NEW_USERNAME:
                                if (newmsg.getMsg_content().equals("Accepted")) {
                                    clientFrame.cardLayout.show(clientFrame.getContentPane(), "onlineCard");
                                } else {
                                    clientFrame.getMenuMessageLabel().setText("Kullanıcı adı kullanılıyor.");
                                    clientFrame.getConnectButton().setText("Tekrar dene");
                                    clientFrame.getConnectButton().addActionListener(al -> {
                                        System.out.println("Username sent again.");
                                        client.setUsername(clientFrame.getUsernameTextField().getText());
                                        sendMessage(Message.MessageType.NEW_USERNAME, null, client.getUsername());
                                    });
                                }
                                break;
                            case CONNECTED_CLIENTS:
                                connectedClients = newmsg.getMsg_content();
                                update_connected_list();
                                break;
                            case BROADCAST_MESSAGE:
                                String msgToShow = "";
                                msgToShow += "\n" + newmsg.getSender() + ":\n" + newmsg.getMsg_content();
                                clientFrame.getChatBoxField().append(msgToShow);
                                break;
                            case DIRECT_CHAT_REQUEST:
                                //to do: accept request
                                System.out.println("istek gelene kişi: " + newmsg.getSender());
                                create_chat_frame(newmsg.getSender());
                                break;
                            case DIRECT_CHAT:
                                for (ChattingFrame cf : chats) {
                                    if (cf.friend.equals(newmsg.getSender())) {
                                        cf.getChatBoxField().append("\n" + newmsg.getMsg_content());
                                        break;
                                    }
                                }
                                break;
                            default:
                                System.out.println("default");
                                break;
                        }

                    } catch (IOException ex) {
                        client.close();
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        client.close();
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        clientFrame.getConnectButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                connectToServer();
                System.out.println("connect e basıldı");

            }
        });

        clientFrame.getSendButton().addActionListener(al -> {
            sendMessage(Message.MessageType.BROADCAST_MESSAGE, null, clientFrame.getWriteMessageField().getText());

        });
        clientFrame.getWriteMessageField().addActionListener(al -> {
            sendMessage(Message.MessageType.BROADCAST_MESSAGE, null, clientFrame.getWriteMessageField().getText());

        });

        clientFrame.getStartChat_button().addActionListener(al -> {
            String dm_partner;
            System.out.println("seçilen: " + clientFrame.getConnectedClientList().getSelectedValue());
            dm_partner = clientFrame.getConnectedClientList().getSelectedValue();
            try {
                if (dm_partner.equals("null") || dm_partner.equals(client.getUsername() + " (ben)")) {
                    JOptionPane.showMessageDialog(clientFrame, "Konuşma başlatacağınız kişiyi listeden seçin.");
                } else {
                    sendMessage(Message.MessageType.DIRECT_CHAT_REQUEST, dm_partner, null);
                    create_chat_frame(dm_partner);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(clientFrame, "Konuşma başlatacağınız kişiyi listeden seçin.");

            }
        });

    }

    private void create_chat_frame(String dm_partner) {
        ChattingFrame newChatFrame = new ChattingFrame(dm_partner);

        newChatFrame.getSendButton().addActionListener(al -> {
            sendMessage(Message.MessageType.DIRECT_CHAT, newChatFrame.friend, newChatFrame.getWriteMessageField().getText());
            System.out.println("frame friende yollanıyor: " + newChatFrame.friend);
            newChatFrame.getChatBoxField().append("\n" + newChatFrame.getWriteMessageField().getText());
            newChatFrame.getWriteMessageField().setText("");
        });

        newChatFrame.getWriteMessageField().addActionListener(al -> {
            sendMessage(Message.MessageType.DIRECT_CHAT, newChatFrame.friend, newChatFrame.getWriteMessageField().getText());
            System.out.println("frame friende yollanıyor: " + newChatFrame.friend);
            newChatFrame.getChatBoxField().append("\n" + newChatFrame.getWriteMessageField().getText());
            newChatFrame.getWriteMessageField().setText("");
        });
        chats.add(newChatFrame);
    }

    private void connectToServer() {

        this.client = new Client("127.0.0.1", 5000);
        client.setUsername(clientFrame.getUsernameTextField().getText());
        System.out.println("client username: " + client.getUsername());

        this.clientFrame.getConnectButton().removeActionListener(this.clientFrame.getConnectButton().getActionListeners()[0]);

        lisThread.start();
        sendMessage(Message.MessageType.NEW_USERNAME, null, client.getUsername());
        System.out.println("Username sent.");
    }

    private void update_connected_list() {
        clientFrame.getDlm().clear();

        String usernames[] = connectedClients.split("\n");

        for (String un : usernames) {
            if (un.equals(client.getUsername())) {
                clientFrame.getDlm().addElement(un + " (ben)");
            } else {
                clientFrame.getDlm().addElement(un);
            }

        }

        clientFrame.getConnectedClientList().setModel(clientFrame.getDlm());

    }

    private Message createMessage(Message.MessageType msg_type, String receiver, String content) {

        Message newMessage = new Message(msg_type);
        newMessage.setSender(client.getUsername());
        newMessage.setReceiver(receiver);
        newMessage.setMsg_content(content);

        return newMessage;

    }

    private void sendMessage(Message.MessageType msg_type, String receiver, String msg) {
        client.sendMessage(createMessage(msg_type, receiver, msg));
    }

}
