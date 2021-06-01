/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import communication.Message;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author beyza
 */
public class Chatroom  {

    public List<ServerClient> memberClilents;
    private String name; //must be unique
    private int memberCount;

    public Chatroom(String groupName, ServerClient startedMember) {

        this.name = groupName;
        this.memberCount = 1;
        this.memberClilents = new ArrayList<>();
        memberClilents.add(startedMember);
        

    }

    public void sendToGroupChat(Message m, ServerClient sender) {

        for (ServerClient sc : memberClilents) {
            if(sc.equals(sender)) continue;
            sc.sendMessage(m);
        }

    }

    public void addMember(ServerClient newMember) {
        newMember.sendMessage(newMember.createMessage(Message.MessageType.GROUP_CHAT_REQUEST, this.name, null));
        memberClilents.add(newMember);
        String members = convert_members_to_string();
        Message newMsg = new Message(Message.MessageType.GROUP_CHAT_MEMBERS);
        newMsg.setMsg_content(members);
        newMsg.setReceiver(this.name);
        newMsg.setSender("Server");
        sendToGroupChat(newMsg, null);
        memberCount++;
    }
    
    private String convert_members_to_string(){
        String str = "";
        for(ServerClient sc: memberClilents){
            str+= sc.getUsername() + "\n";
        }
        
        return str;
    }

    public synchronized void removeMember(ServerClient sc){
        this.memberClilents.remove(sc);
        this.memberCount--;
    }
    
    public String getGroupName() {
        return name;
    }

    public int getMemberCount() {
        return memberCount;
    }
    
    

    
}
