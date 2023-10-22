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
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 *
 * @author Alois
 */
public class GroupChatUser {

    private Socket socket;// Socket for communication with the server
    private BufferedReader bufferedReader; // Reader to receive messages from the server
    private BufferedWriter bufferedWriter;// Writer to send messages to the server
    private String username;// Username of the user
    private KeyPairGenerator keyPairGenerator;
    private KeyPair keyPair;
    private PublicKey receivedPublicKey;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public GroupChatUser(Socket socket, String username) { // Constructor to initialize the user with a socket and username

        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            this.keyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {

        }
        try {
            this.socket = socket; //set the socket instance
            // Initialize input and output streams for the user socket
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.inputStream = new ObjectInputStream(socket.getInputStream());
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.username = username;// set the username
            sendPublicKey();
        } catch (IOException ex) {// in case of exception the program calls the method closeEverything() to terminate the program
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

// Method to send messages to the server
    public void sendPublicKey() {
        try {
            PublicKey publicKey = keyPair.getPublic();
            outputStream.writeObject(publicKey);
            outputStream.close();

        } catch (IOException ex) {
            Logger.getLogger(GroupChatUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendMessage() throws Exception {
        //TODO: fix Exceptions
        try {
            //
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, receivedPublicKey);

            bufferedWriter.write(username);//send the user's username to the server
            bufferedWriter.newLine();// sends a new line 
            bufferedWriter.flush();// sends watever was in the bufferedWriter to the server staight away

            Scanner scanner = new Scanner(System.in);

            while (socket.isConnected()) {// Continuously write messages from the user and send them to the server

                String messageToSend = scanner.nextLine();
                byte[] encryptedMessage = messageToSend.getBytes();
                byte[] cipherText = cipher.doFinal(encryptedMessage);
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                // Send the private key to the client
                outputStream.writeObject(cipherText);
                bufferedWriter.write(username + ": ");//TODO: concatnate the username and decrypted message on the other side
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);// calls the method close

        }
    }

    public void listenForMessage() {    // Method to listen for messages from the server in a separate thread
        // Start a new thread to continuously read messages from the server
        new Thread(() -> {
            try {
                receivedPublicKey = (PublicKey) inputStream.readObject();

                String msgFromGroupChat;
                Cipher cipher = Cipher.getInstance("RSA");

                while (socket.isConnected()) {
                    try {
                       
                        byte[] receivedEncryptedCipherBytes = (byte[])inputStream.readObject();

                        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
                        byte[] decryptedBytes = cipher.doFinal(receivedEncryptedCipherBytes);
                        String decryptedMessage = new String(decryptedBytes);
                        System.out.println(username +": "+ decryptedMessage);
                        // print message to the users screen
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);

                    } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
                        Logger.getLogger(GroupChatUser.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | IOException | ClassNotFoundException ex) {
                Logger.getLogger(GroupChatUser.class.getName()).log(Level.SEVERE, null, ex);
            }
        } //a thrread constructor take a runnable object or an object of a class that implements the runnable interface
        ).start();

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

    public static void main(String[] args) {

        try {
            Scanner scanner = new Scanner(System.in);// create a scanner object
            System.out.println("Enter your username for the group chat: ");
            String username = scanner.nextLine();// takes an input from the user and stores it in a variable called username
            Socket socket = new Socket("localhost", 4444);// 
            GroupChatUser groupChatUser = new GroupChatUser(socket, username);// 
            groupChatUser.listenForMessage();
            try {
                groupChatUser.sendMessage();

            } catch (Exception ex) {
                Logger.getLogger(GroupChatUser.class
                        .getName()).log(Level.SEVERE, null, ex);

            }
        } catch (IOException ex) {
            Logger.getLogger(GroupChatUser.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

}
