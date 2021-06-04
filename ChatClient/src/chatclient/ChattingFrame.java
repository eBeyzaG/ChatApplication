/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;
import static javax.swing.text.DefaultCaret.ALWAYS_UPDATE;

/**
 *
 * @author beyza
 * direct chatting frame
 */
public class ChattingFrame extends javax.swing.JFrame {

    /**
     * Creates new form ChattingFrame
     */
    public String friend;
    
    public ChattingFrame(String friend_username) {
        initComponents();
        
        try{
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }catch(Exception e){
          System.out.println(e.toString());
      }
        
        this.getContentPane().setBackground(new java.awt.Color(239,239,239));
        friend = friend_username;
        chatNameLabel.setText(friend_username);
       
        DefaultCaret caret = (DefaultCaret)(chatBoxField.getCaret());
        caret.setUpdatePolicy(ALWAYS_UPDATE);
        
        chatBoxField.setLineWrap(true);
        chatBoxField.setWrapStyleWord(true);
        
        this.setTitle(friend_username);
    
        this.setVisible(true);
        

    }

    public JButton getSendButton() {
        return sendButton;
    }

    public JTextField getWriteMessageField() {
        return writeMessageField;
    }

    public JTextArea getChatBoxField() {
        return chatBoxField;
    }

    public JButton getFileChooserButton() {
        return fileChooserButton;
    }
    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        chatNameLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        chatBoxField = new javax.swing.JTextArea();
        sendButton = new javax.swing.JButton();
        writeMessageField = new javax.swing.JTextField();
        fileChooserButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(179, 214, 228));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        chatNameLabel.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        chatNameLabel.setText("Beyza");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 14, 0, 3);
        getContentPane().add(chatNameLabel, gridBagConstraints);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(400, 400));

        chatBoxField.setEditable(false);
        chatBoxField.setColumns(20);
        chatBoxField.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        chatBoxField.setRows(5);
        jScrollPane2.setViewportView(chatBoxField);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(32, 14, 9, 14);
        getContentPane().add(jScrollPane2, gridBagConstraints);

        sendButton.setText("Gönder");
        sendButton.setPreferredSize(new java.awt.Dimension(75, 45));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(9, 27, 9, 16);
        getContentPane().add(sendButton, gridBagConstraints);

        writeMessageField.setPreferredSize(new java.awt.Dimension(280, 40));
        writeMessageField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                writeMessageFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 43, 0, 86);
        getContentPane().add(writeMessageField, gridBagConstraints);

        fileChooserButton.setText("...");
        fileChooserButton.setPreferredSize(new java.awt.Dimension(45, 45));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        getContentPane().add(fileChooserButton, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void writeMessageFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeMessageFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_writeMessageFieldActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ChattingFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ChattingFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ChattingFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ChattingFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ChattingFrame("hi").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea chatBoxField;
    private javax.swing.JLabel chatNameLabel;
    private javax.swing.JButton fileChooserButton;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton sendButton;
    private javax.swing.JTextField writeMessageField;
    // End of variables declaration//GEN-END:variables
}
