package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    private volatile Server server;
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String clientName;
    public boolean hasName = false;
    private String lobbyName;
    public Boolean inGame = false;
    public Boolean inLobby = false;
    public Hand hand = new Hand();
    public Boolean canMove = false;
    public int gamePoints = 0;

    public ClientHandler(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            writer.println("Hello! Tell us your name!");
            String input;
            while ((input = reader.readLine()) != null) {
                System.out.println("|"+input+"|");
                String[] inputArray = input.split(" ", 3);

                if(this.hasName){
                    if(inLobby && !inGame){
                        handleLobbyCommands(inputArray);
                    } else if (inGame && !inLobby) {
                        handleGameCommand(inputArray);
                    } else {
                        handleAppCommands(inputArray);
                    }
                } else {
                    nameSetter(inputArray);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public void setLobbyName(String lobbyName){
        this.lobbyName = lobbyName;
    }
    public String getLobbyName(){
        return this.lobbyName;
    }
    public void clearLobbyName(){
        this.lobbyName = null;
    }
    public void setClientName(String name){
        this.clientName = name;
    }
    public String getClientName(){
        return this.clientName;
    }

    public void handleAppCommands(String[] inputArray){
        String command = inputArray[0].toLowerCase();
        switch (command) {
            case "create" -> server.createLobby(inputArray[1], this);
            case "join" -> server.joinLobby(inputArray[1], this);
            case "list" -> server.listLobbies(this);
            case "send" -> server.sendChatMessage(inputArray[1], inputArray[2], this);
            default -> writer.println("Unknown command.");
        }
    }
    public void nameSetter(String[] inputArray){
        server.setClientName(inputArray[0], this);
    }
    public void handleLobbyCommands(String[] inputArray){
        String command = inputArray[0].toLowerCase();
        switch (command) {
            case "start" -> server.startGame(this.lobbyName, this);
            case "invite" -> server.invitePlayer(inputArray[1], this);
            case "send" -> server.sendChatMessage(inputArray[1], inputArray[2], this);
            case "leave" -> server.leaveLobby(this);
            default -> writer.println("Unknown command.");
        }
    }
    public void handleGameCommand(String[] inputArray){
        String command = inputArray[0].toLowerCase();
        switch (command) {
            case "list" -> server.printHand(this);
            case "play" -> server.playCard(inputArray[1], this);
            case "send" -> server.sendChatMessage(inputArray[1], inputArray[2], this);

            default -> writer.println("Unknown command.");
        }
    }

    private void close() {
        try {
            reader.close();
            writer.close();
            clientSocket.close();
            server.clients.remove(this);
            System.out.println("Zakończono połączenie: " + clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

