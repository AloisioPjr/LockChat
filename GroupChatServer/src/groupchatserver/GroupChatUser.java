/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package groupchatserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alois
 */
public class GroupChatUser {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public GroupChatUser(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader =new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        } catch (IOException ex) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

   

    public void sendMessage() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            
            Scanner scanner = new Scanner(System.in);

            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(username + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);

        }
    }

    public void listenForMessage() {
        new Thread(new Runnable(){
           @Override
           public void run(){
               String msgFromGroupChat;
               while(socket.isConnected()){
                   try{
                       msgFromGroupChat = bufferedReader.readLine();
                       System.out.println(msgFromGroupChat);
                   }catch(IOException e ){
                       closeEverything(socket, bufferedReader, bufferedWriter);
                       
                   }
               }
           }
        }).start();
            
          
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
    try {
            // Close resources (socket, reader, writer)
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace(); // Print the stack trace if an IO exception occurs while closing resources
        }
    }

    //a thrread constructor take a runnable object or an object of a class that implements the runnable interface     
    public static void main(String[] args) {
        
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter your username for the group chat: ");
            String username = scanner.nextLine();
            Socket socket = new Socket("localhost", 4444);
            GroupChatUser groupChatUser;
            groupChatUser = new GroupChatUser(socket, username );
            
            groupChatUser.listenForMessage();
            groupChatUser.sendMessage();
        } catch (IOException ex) {
            Logger.getLogger(GroupChatUser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
    }

}