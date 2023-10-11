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
import java.util.ArrayList;

/**
 *
 * @author Alois
 */
// This class represents a client handler for the group chat server. Each instance of this class handles
// communication with a specific client in a separate thread.
public class ClientRunnable implements Runnable {

    public static ArrayList<ClientRunnable> clientRun = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public ClientRunnable(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = bufferedReader.readLine();
            clientRun.add(this);
            broadcastMessage("Server: " + username + " has entered the chat!");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);

        }
    }

    @Override
    public void run() {
        String messageFromUser;

        while (socket.isConnected()) {
            try {
                messageFromUser = bufferedReader.readLine();
                broadcastMessage(messageFromUser);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        for (ClientRunnable clientRun : clientRun) {
            try {
                if (!clientRun.username.equals(username)) {
                    clientRun.bufferedWriter.write(messageToSend);
                    clientRun.bufferedWriter.newLine();
                    clientRun.bufferedWriter.flush();

                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientRun() {
        clientRun.remove(this);
        broadcastMessage("Server: " + username + " has left the chat");

    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientRun();
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
