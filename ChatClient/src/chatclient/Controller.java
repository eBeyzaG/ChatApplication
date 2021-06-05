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
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author beyza controller class that connects ui and model
 */
public class Controller {

    ClientFrame clientFrame;
    Client client;
    Object lastMessage;
    Thread lisThread;
    String connectedClients;
    ArrayList<ChattingFrame> chats;
    TreeMap<String, GroupChattingFrame> groupchats;
    ArrayList<String> connecteds;
    String dirPath;
    String address;

    public Controller(ClientFrame clientFrame, Client client) {
        new File(System.getProperty("user.home") + "/GelenDosyalar").mkdirs();
        dirPath = System.getProperty("user.home") + "/GelenDosyalar";
        this.clientFrame = clientFrame;
        chats = new ArrayList<>();
        groupchats = new TreeMap<>();
        connecteds = new ArrayList<>();
        address ="18.188.102.9";

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                clientFrame.setVisible(true);
            }
        });

    }

    public void recreate_client() {//recreate client properties when user exits or enters wrong server
        chats = new ArrayList<>();
        groupchats = new TreeMap<>();
        connecteds = new ArrayList<>();
        clientFrame.getChatBoxField().setText("Hey! de");
        clientFrame.getConnectButton()
                .addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae
                    ) {
                        try {
                            connectToServer();
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(clientFrame, "Sunucu yanıt vermiyor.");
                            recreate_client();
                        }

                    }
                }
                );
    }

    public Thread createListenThread(Client c) {//creates listening thread for the current client
        return new Thread(new Runnable() {
            public void run() {
                while (client.getIsConnected()) {
                    try {
                        System.out.println("Waiting for message from server.");

                        Message newmsg = (Message) (client.getClientInput().readObject());
                        System.out.println("sender: " + newmsg.getSender() + " receiver:" + newmsg.getReceiver());

                        switch (newmsg.getMsg_type()) {//switches to appropriate case according to message's type
                            case NEW_USERNAME://check whether the chosen un is accepted by server or not
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
                            case CONNECTED_CLIENTS://receive current connected clients
                                connectedClients = newmsg.getMsg_content();
                                update_connected_list();
                                break;
                            case BROADCAST_MESSAGE://receive the broadcasted msg
                                String msgToShow = "";
                                msgToShow += "\n[" + newmsg.getSender() + "]: " + newmsg.getMsg_content();
                                clientFrame.getChatBoxField().append(msgToShow);
                                break;
                            case BROADCAST_FILE://receive the broadcasted file
                                int confirm2 = JOptionPane.showConfirmDialog(clientFrame, newmsg.getSender() + " kişisi " + newmsg.getMsg_content() + " dosyasını gönderdi. İndirmeyi kabul ediyor musunuz?");
                                if (confirm2 == JOptionPane.YES_OPTION) {
                                    byte[] newFileContent = (byte[]) newmsg.getFileContent();
                                    try {
                                        File newFile = new File(dirPath + "/" + newmsg.getMsg_content());
                                        Files.write(newFile.toPath(), newFileContent);
                                    } catch (Exception e) {
                                        JOptionPane.showMessageDialog(clientFrame, "Gelen dosyayı kaydetmekle ilgili bir sorun oluştu.");
                                        System.out.println(e.toString());
                                    }
                                }
                                clientFrame.getChatBoxField().append("\n[" + newmsg.getSender() + "]: " + newmsg.getMsg_content());

                                break;
                            case DIRECT_CHAT_REQUEST://create frame with incoming request
                                //to do: accept request
                                System.out.println("istek gelene kişi: " + newmsg.getSender());
                                create_chat_frame(newmsg.getSender());
                                break;
                            case DIRECT_CHAT://receive direct chat
                                for (ChattingFrame cf : chats) {
                                    if (cf.friend.equals(newmsg.getSender())) {
                                        cf.getChatBoxField().append("\n[" + newmsg.getSender() + "]: " + newmsg.getMsg_content());
                                        break;
                                    }
                                }
                                break;
                            case DIRECT_FILE://receive direct file
                                for (ChattingFrame cf : chats) {
                                    if (cf.friend.equals(newmsg.getSender())) {
                                        int confirm = JOptionPane.showConfirmDialog(cf, newmsg.getSender() + " kişisi " + newmsg.getMsg_content() + " dosyasını gönderdi. İndirmeyi kabul ediyor musunuz?");
                                        if (confirm == JOptionPane.YES_OPTION) {
                                            byte[] newFileContent = (byte[]) newmsg.getFileContent();
                                            try {
                                                File newFile = new File(dirPath + "/" + newmsg.getMsg_content());
                                                Files.write(newFile.toPath(), newFileContent);
                                            } catch (Exception e) {
                                                JOptionPane.showMessageDialog(cf, "Gelen dosyayı kaydetmekle ilgili bir sorun oluştu.");
                                            }
                                        }
                                        cf.getChatBoxField().append("\n[" + newmsg.getSender() + "]: " + newmsg.getMsg_content());
                                        break;
                                    }
                                }
                                break;
                            case GROUP_CHAT_START://check cwhether chosen group name is ok with server
                                if (groupchats.containsKey(newmsg.getReceiver())) {
                                    GroupChattingFrame problematicGcf = groupchats.get(newmsg.getReceiver());
                                    String newName = JOptionPane.showInputDialog(problematicGcf, "Bu konuşma odası adı zaten var. Lütfen başka bir ad giriniz.");
                                    problematicGcf.group_name = newName;
                                    groupchats.remove(newmsg.getReceiver());
                                    groupchats.put(newName, problematicGcf);
                                    problematicGcf.getChatNameLabel().setText(newName);
                                    sendMessage(Message.MessageType.GROUP_CHAT_START, newName, null);
                                    break;
                                } else {
                                    System.out.println("There is no group with this name.");
                                }

                                break;
                            case GROUP_CHAT_MEMBERS://receive group chat member info
                                update_group_members(groupchats.get(newmsg.getReceiver()), newmsg.getMsg_content());
                                break;
                            case GROUP_CHAT_REQUEST:
                                if (!groupchats.containsKey(newmsg.getReceiver())) {
                                    create_group_chat_frame(newmsg.getReceiver());
                                }
                                break;
                            case GROUP_CHAT://receive group chat message
                                groupchats.get(newmsg.getReceiver()).getChatBoxField().append("\n" + "[" + newmsg.getSender() + "]: " + newmsg.getMsg_content());

                                break;
                            case GROUP_CHAT_FILE://receive group chat file
                                GroupChattingFrame currentFrm = groupchats.get(newmsg.getReceiver());

                                int confirm = JOptionPane.showConfirmDialog(currentFrm, newmsg.getSender() + " kişisi " + newmsg.getMsg_content() + " dosyasını gönderdi. İndirmeyi kabul ediyor musunuz?");
                                if (confirm == JOptionPane.YES_OPTION) {
                                    byte[] newFileContent = (byte[]) newmsg.getFileContent();
                                    try {
                                        File newFile = new File(dirPath + "/" + newmsg.getMsg_content());
                                        Files.write(newFile.toPath(), newFileContent);
                                    } catch (Exception e) {
                                        JOptionPane.showMessageDialog(currentFrm, "Gelen dosyayı kaydetmekle ilgili bir sorun oluştu.");
                                    }
                                }

                                groupchats.get(newmsg.getReceiver()).getChatBoxField().append("\n" + "[" + newmsg.getSender() + "]: " + newmsg.getMsg_content());
                                break;

                            default:
                                System.out.println("default");
                                break;
                        }

                    } catch (IOException ex) {
                        try {
                            client.close();
                        } catch (Exception e) {
                            System.out.println(e.toString());
                            break;
                        }
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        try {
                            client.close();
                        } catch (Exception e) {
                            System.out.println(e.toString());
                            break;
                        }
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        );
    }

    public void init_controller() {

        //server'a bağlanma butonu
        clientFrame.getConnectButton()
                .addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae
                    ) {
                        System.out.println("connect e basıldı");
                        try {
                            connectToServer();
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(clientFrame, "Sunucu yanıt vermiyor.");
                            recreate_client();
                        }

                    }
                }
                );

        //genel chat'e mesaj yollama 
        clientFrame.getSendButton()
                .addActionListener(al -> {
                    sendMessage(Message.MessageType.BROADCAST_MESSAGE, null, clientFrame.getWriteMessageField().getText());
                    clientFrame.getWriteMessageField().setText("");
                }
                );
        clientFrame.getWriteMessageField()
                .addActionListener(al -> {
                    sendMessage(Message.MessageType.BROADCAST_MESSAGE, null, clientFrame.getWriteMessageField().getText());
                    clientFrame.getWriteMessageField().setText("");
                }
                );

        //direct chat başlatma butonu
        clientFrame.getStartChat_button()
                .addActionListener(al -> {
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
                }
                );

        //grup chat başlatma butonu
        clientFrame.getStartchatroom_button()
                .addActionListener(al -> {
                    String newGroupName = JOptionPane.showInputDialog(clientFrame, "Yeni konuşma odası adı girin.");
                    create_group_chat_frame(newGroupName);
                    sendMessage(Message.MessageType.GROUP_CHAT_START, newGroupName, null);
                }
                );

        //genel chat'e dosya gönderme butonu
        clientFrame.getFileChooserButton()
                .addActionListener(al -> {
                    JFileChooser jfc = new JFileChooser();
                    int user_choice = jfc.showDialog(clientFrame, "Send");

                    if (user_choice == JFileChooser.APPROVE_OPTION) {
                        File chosenFile = jfc.getSelectedFile();
                        try {
                            byte[] fileContent = Files.readAllBytes(chosenFile.toPath());
                            Message newFileMessage = createMessage(Message.MessageType.BROADCAST_FILE, null, chosenFile.getName());
                            newFileMessage.setFileContent(fileContent);
                            client.sendMessage(newFileMessage);
                            clientFrame.getChatBoxField().append("\n[Ben]: " + chosenFile.getName());

                        } catch (IOException ex) {
                            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                            JOptionPane.showMessageDialog(clientFrame, "Dosya gönderimi başarısız.");
                        }

                    }

                });
        //çıkış yap
        clientFrame.getExitButton().addActionListener(al -> {
            this.lisThread.interrupt();
            client.close();
            client = null;
            recreate_client();
        });
        //server değiştir
        clientFrame.getSettingsButton().addActionListener(al -> {
            address = JOptionPane.showInputDialog(clientFrame, "Sunucu değiştir", address);
        });
    }

    private void create_chat_frame(String dm_partner) {//create chat frame with partner
        ChattingFrame newChatFrame = new ChattingFrame(dm_partner);

        newChatFrame.getSendButton().addActionListener(al -> {
            sendMessage(Message.MessageType.DIRECT_CHAT, newChatFrame.friend, newChatFrame.getWriteMessageField().getText());
            System.out.println("frame friende yollanıyor: " + newChatFrame.friend);
            newChatFrame.getChatBoxField().append("\n[Ben]: " + newChatFrame.getWriteMessageField().getText());
            newChatFrame.getWriteMessageField().setText("");
        });

        newChatFrame.getWriteMessageField().addActionListener(al -> {
            sendMessage(Message.MessageType.DIRECT_CHAT, newChatFrame.friend, newChatFrame.getWriteMessageField().getText());
            System.out.println("frame friende yollanıyor: " + newChatFrame.friend);
            newChatFrame.getChatBoxField().append("\n[Ben]: " + newChatFrame.getWriteMessageField().getText());
            newChatFrame.getWriteMessageField().setText("");
        });

        newChatFrame.getFileChooserButton().addActionListener(al -> {

            JFileChooser jfc = new JFileChooser();
            int user_choice = jfc.showDialog(newChatFrame, "Send");

            if (user_choice == JFileChooser.APPROVE_OPTION) {
                File chosenFile = jfc.getSelectedFile();
                try {
                    byte[] fileContent = Files.readAllBytes(chosenFile.toPath());
                    Message newFileMessage = createMessage(Message.MessageType.DIRECT_FILE, newChatFrame.friend, chosenFile.getName());
                    newFileMessage.setFileContent(fileContent);
                    client.sendMessage(newFileMessage);
                    newChatFrame.getChatBoxField().append("\n[Ben]: " + chosenFile.getName());

                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(newChatFrame, "Dosya gönderimi başarısız.");
                }

            }

        });

        chats.add(newChatFrame);
    }

    private void create_group_chat_frame(String groupName) {//create new chat frame for group chatting

        Object[] cnc = connecteds.toArray();
        GroupChattingFrame newGroupFrame = new GroupChattingFrame(groupName);
        newGroupFrame.getAddFriendToGroup_button().addActionListener(al -> {
            String returnedValue = JOptionPane.showInputDialog(newGroupFrame, "Eklemek istediğiniz kullanıcı adını giriniz.");
            if (returnedValue.equals(null)) {
                return;
            }
            sendMessage(Message.MessageType.GROUP_CHAT_REQUEST, groupName, returnedValue);
            System.out.println(returnedValue);
        });
        newGroupFrame.getSendButton().addActionListener(al -> {
            sendMessage(Message.MessageType.GROUP_CHAT, newGroupFrame.group_name, newGroupFrame.getWriteMessageField().getText());
            System.out.println("şu gruba yollanıyor: " + newGroupFrame.group_name);
            newGroupFrame.getChatBoxField().append("\n[Ben]: " + newGroupFrame.getWriteMessageField().getText());
            newGroupFrame.getWriteMessageField().setText("");
        });

        newGroupFrame.getWriteMessageField().addActionListener(al -> {
            sendMessage(Message.MessageType.GROUP_CHAT, newGroupFrame.group_name, newGroupFrame.getWriteMessageField().getText());
            System.out.println("şu gruba yollanıyor: " + newGroupFrame.group_name);
            newGroupFrame.getChatBoxField().append("\n[Ben]: " + newGroupFrame.getWriteMessageField().getText());
            newGroupFrame.getWriteMessageField().setText("");
        });

        newGroupFrame.getFileChooserButton().addActionListener(al -> {

            JFileChooser jfc = new JFileChooser();
            int user_choice = jfc.showDialog(newGroupFrame, "Send");

            if (user_choice == JFileChooser.APPROVE_OPTION) {
                File chosenFile = jfc.getSelectedFile();
                try {
                    byte[] fileContent = Files.readAllBytes(chosenFile.toPath());
                    Message newFileMessage = createMessage(Message.MessageType.GROUP_CHAT_FILE, newGroupFrame.group_name, chosenFile.getName());
                    newFileMessage.setFileContent(fileContent);
                    client.sendMessage(newFileMessage);
                    newGroupFrame.getChatBoxField().append("\n[Ben]: " + chosenFile.getName());

                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(newGroupFrame, "Dosya gönderimi başarısız.");
                }

            }

        });

        groupchats.put(newGroupFrame.group_name, newGroupFrame);
    }

    private void connectToServer() { //connect to server with given ip

        this.client = new Client(address, 5000);
        client.setUsername(clientFrame.getUsernameTextField().getText());
        System.out.println("client username: " + client.getUsername());

        this.clientFrame.getConnectButton().removeActionListener(this.clientFrame.getConnectButton().getActionListeners()[0]);
        lisThread = createListenThread(client);
        lisThread.start();
        sendMessage(Message.MessageType.NEW_USERNAME, null, client.getUsername());
        System.out.println("Username sent.");
    }

    private void update_connected_list() {//update the connected clients list in main page
        clientFrame.getDlm().clear();

        String usernames[] = connectedClients.split("\n");

        for (String un : usernames) {
            if (un.equals(client.getUsername())) {
                clientFrame.getDlm().addElement(un + " (ben)");
            } else {
                clientFrame.getDlm().addElement(un);
                connecteds.add(un);
            }

        }

        clientFrame.getConnectedClientList().setModel(clientFrame.getDlm());

    }

    private void update_group_members(GroupChattingFrame gcf, String members) { //update the members of the given gc 

        gcf.dlm.clear();

        String memberList[] = members.split("\n");
        for (String un : memberList) {
            gcf.dlm.addElement(un);
        }
        gcf.getMembersList().setModel(gcf.dlm);
    }

    private Message createMessage(Message.MessageType msg_type, String receiver, String content) {
        //create Message type with given parameters
        Message newMessage = new Message(msg_type);
        newMessage.setSender(client.getUsername());
        newMessage.setReceiver(receiver);
        newMessage.setMsg_content(content);

        return newMessage;

    }

    private void sendMessage(Message.MessageType msg_type, String receiver, String msg) {
        //send the message to server from the current client
        client.sendMessage(createMessage(msg_type, receiver, msg));
    }

}
