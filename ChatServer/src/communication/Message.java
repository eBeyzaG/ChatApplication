/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

import java.io.Serializable;

/**
 *
 * @author beyza
 */
public class Message implements Serializable {

    public static enum MessageType {
        INFO,
        DIRECT_CHAT,
        DIRECT_CHAT_REQUEST,
        GROUP_CHAT,
        GROUP_CHAT_START,
        GROUP_CHAT_REQUEST,
        DIRECT_FILE,
        GROUP_CHAT_MEMBERS,
        GROUP_CHAT_FILE,
        GROUP_CHAT_DELETE,
        CLIENT_MESSAGE,
        NEW_USERNAME,
        CONNECTED_CLIENTS,
        BROADCAST_MESSAGE,
        BROADCAST_FILE,
        
    }

    private MessageType msg_type;
    private String msg_content;
    private String sender;
    private String receiver;
    private byte[] fileContent;

    public Message(MessageType msg_type) {
        this.msg_type = msg_type;
        this.sender = "yollayan";
        this.receiver = "alan";
    }
 

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

   
    public void setMsg_content(String msg_content) {
        this.msg_content = msg_content;
    }

    public void setMsg_type(MessageType msg_type) {
        this.msg_type = msg_type;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMsg_content() {
        return msg_content;
    }

    public MessageType getMsg_type() {
        return msg_type;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    @Override
    public String toString() {
        return "Sender: " + this.sender + " Receiver: " + this.receiver + " Message: " + this.msg_content.toString();
    }

}
