/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import javax.swing.JFrame;

/**
 *
 * @author beyza
 */
public class ClientMain {

    /**
     * 
     * Driver class
     */
    public static void main(String[] args) {
        ClientFrame cliFrame = new ClientFrame();
        Controller controller = new Controller(cliFrame, null);
        controller.init_controller();
    }

}
