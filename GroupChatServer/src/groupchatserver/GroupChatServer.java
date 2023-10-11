/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package groupchatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alois
 */
// This class represents the server for a group chat application. It listens for incoming client connections 
// and handles each client connection in a separate thread using the ClientRunnable class.

public class GroupChatServer {

    private ServerSocket serverSocket;

    public GroupChatServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {

        try {

            while (!serverSocket.isClosed()) {
                
            
            Socket socket = serverSocket.accept();
             System.out.println(" a new client has connected");
            ClientRunnable clientHandler = new ClientRunnable(socket);

            Thread thread = new Thread(clientHandler);
            thread.start();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closedServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(4444);
            GroupChatServer server = new GroupChatServer(serverSocket);
            server.startServer();
        } catch (IOException ex) {
           ex.printStackTrace();
        }
    }

}
