/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package groupchatserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alois
 */
// This class represents a client handler for the group chat server. Each instance of this class handles
// communication with a specific client in a separate thread.
public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> ClientHandlerArrayList = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private PublicKey myPublicKey;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    public ClientHandler(Socket socket) {
        try {
            
            this.socket = socket;
            this.inputStream = new ObjectInputStream(socket.getInputStream());
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = bufferedReader.readLine();
            this.ClientHandlerArrayList.add(this);
            broadcastMessage("server:"+username + " is online!");
            /*try {
                
                this.myPublicKey = (PublicKey) inStream.readObject();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

            
            broadcastPublicKey(myPublicKey);*/

        } catch (IOException e) {
            //closeEverything(socket, bufferedReader, bufferedWriter);

        }
    }

    @Override
    public void run() {
        String messageFromUser;
        try {
            //inputStream = new ObjectInputStream(socket.getInputStream());
            PublicKey receivedPublicKey = (PublicKey) inputStream.readObject();
            broadcastPublicKey(receivedPublicKey);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (socket.isConnected()) {
            try {
                byte[] receivedMessageCipherByte = (byte[])inputStream.readObject();
                broadcastMessageCipherByte(receivedMessageCipherByte);
                
                //messageFromUser = bufferedReader.readLine();
                //broadcastMessage(messageFromUser);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void broadcastPublicKey(PublicKey publicKey) {
        for (ClientHandler clientHandler : ClientHandlerArrayList) {
            try {
                if (!clientHandler.username.equals(username)) {
                    //ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                    outputStream.writeObject(publicKey);

                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }
  public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : ClientHandlerArrayList) {
            try {
                if (!clientHandler.username.equals(username)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();

                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
  }
    public void broadcastMessageCipherByte(byte[] messageToSend) {
        for (ClientHandler clientHandler : ClientHandlerArrayList) {
            if (!clientHandler.username.equals(username)) {
                try {
                    clientHandler.outputStream.writeObject(messageToSend);
                } catch (IOException ex) {
                    Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void removeClientHandler() {
        ClientHandlerArrayList.remove(this);
        broadcastMessage("Server: " + username + " has left the chat");

    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
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
            e.printStackTrace();

        }
    }
}
