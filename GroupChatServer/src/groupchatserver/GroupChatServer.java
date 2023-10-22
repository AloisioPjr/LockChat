/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package groupchatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Alois
 */
// This class represents the server for a group chat application. It listens for incoming client connections 
// and handles each client connection in a separate thread using the ClientRunnable class.
public class GroupChatServer {

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private static final int MAX_USERS =3 ;
    private int userCount = 0;

    public GroupChatServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        //this.executorService = Executors.newFixedThreadPool(MAX_USERS);
    }

    public void startServer() {

        try {
            while (!serverSocket.isClosed()/*&userCount < MAX_USERS*/) {
                
                Socket socket = serverSocket.accept();
               
                    userCount++;
                    System.out.println(" a new client has connected" + userCount);
                    ClientHandler clientRun = new ClientHandler(socket);
                    // Create a new thread for the clientHandler and start it
                    Thread thread = new Thread(clientRun);
                    thread.start();
                    //executorService.execute(clientRun);
                
                
            }

        } catch (IOException e) {
            closeServerSocket();
        } finally {
            //closeExecutorService();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public void closeExecutorService() {
        if (!executorService.isShutdown() || executorService != null) {
            executorService.shutdown();
        }
    }*/

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
