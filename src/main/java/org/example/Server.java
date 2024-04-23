package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Server {

    private ServerSocket serverSocket;
    ArrayList<ClientHandler> clients = new ArrayList<>();
    private ArrayList<Lobby> lobbies = new ArrayList<>();

    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        // Wątek dla odczytu poleceń od użytkownika
        Thread consoleThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                try {
                    String command = scanner.nextLine();
                    String[] arrayCommand = command.split(" ", 2);
                    if (arrayCommand[0].equalsIgnoreCase("skip")) {
                        skipHandInLobby(arrayCommand[1]);
                    }
                    // Tutaj możesz dodać kod do obsługi poleceń od użytkownika
                    System.out.println("Command from console: " + command);
                } catch(Exception e){
                    System.err.println("Command is not full");
                }
            }
        });
        consoleThread.start();

        // Wątek dla obsługi nowych połączeń
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection: " + clientSocket);
                ClientHandler clientHandler = new ClientHandler(this, clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setClientName(String name, ClientHandler client){
        client.setClientName(name);
        client.hasName = true;
    }
    public void skipHandInLobby(String lobbyName){
        for(Lobby lobby : lobbies){
            if(lobby.getName().equals(lobbyName)){
                lobby.getGame().skipCurrentHand();
                return;
            }
        }
    }

    public void createLobby(String lobbyName, ClientHandler creator) {
        Lobby lobby = new Lobby(lobbyName);
        lobby.addPlayer(creator);
        lobby.setCreator(creator);
        lobbies.add(lobby);
        creator.setLobbyName(lobbyName);
        ArrayList<ClientHandler> lobbyPlayers = lobby.getPlayers();
        System.out.println(lobbyPlayers);
        creator.sendMessage("Created lobby: " + lobbyName);
        creator.sendMessage("Joined lobby " + lobbyName);
        creator.inLobby = true;
    }

    public void joinLobby(String lobbyName, ClientHandler player) {
        for (Lobby lobby : lobbies) {
            if (lobby.getName().equals(lobbyName)) {
                if (lobby.getPlayers().size() >= 4){
                    player.sendMessage("Lobby is full!");
                    return;
                }
                lobby.addPlayer(player);
                player.setLobbyName(lobbyName);
                ArrayList<ClientHandler> lobbyPlayers = lobby.getPlayers();
                System.out.println(lobbyPlayers);
                System.out.println(player + " joined lobby " + lobbyName);
                player.sendMessage("Joined lobby " + player.getLobbyName());
                player.inLobby = true;
                return;
            }
        }
        player.sendMessage("Lobby " + lobbyName + " does not exists.");
    }
    public void leaveLobby(ClientHandler player){
        for(Lobby lobby : lobbies){
            if(lobby.getName().equals(player.getLobbyName())){
                lobby.removePlayer(player);
                player.clearLobbyName();
                player.sendMessage("You have left the lobby");
                if(lobby.getPlayers().size() == 0){
                    this.lobbies.remove(lobby);
                }
                player.inLobby = false;
                return;
            }
        }
    }

    public void startGame(String lobbyName, ClientHandler player){
        for(Lobby lobby : lobbies){
            if(lobby.getName().equals(lobbyName)){
                ArrayList<ClientHandler> lobbyPlayers = lobby.getPlayers();
                if(lobbyPlayers.size() < 4 || !lobby.getCreator().equals(player)){
                    player.sendMessage("Can't start game with less than 4 players or you don't have permission to start the game");
                    return;
                }else {
                    for(ClientHandler user : lobby.getPlayers()){
                        user.inGame = true;
                        user.inLobby = false;
                        user.sendMessage("Game Started!");
                    }
                    Game game = new Game(lobby);
                    lobby.setGame(game);
                    game.beginHand();
                }
            }
        }
    }

    public void listLobbies(ClientHandler player){
        for (Lobby lobby : lobbies){
            player.sendMessage(lobby.getName()+":");
            ArrayList<ClientHandler> lobbyPlayers = lobby.getPlayers();
            for(ClientHandler gamer : lobbyPlayers){
                player.sendMessage("\t"+gamer.getClientName());
            }
        }
    }

    public void printHand(ClientHandler player){
        ArrayList<String> cardNames = new ArrayList<>();
        for(Card card : player.hand.getCards()){
            cardNames.add(card.getName());
        }
        player.sendMessage(cardNames.toString());
    }

    public void playCard(String cardName, ClientHandler player) {
        if (player.canMove) {
            for (Lobby lobby : lobbies) {
                if (lobby.getName().equals(player.getLobbyName())) {
                    handleCardPlay(lobby, player, cardName);
                }
            }
        } else {
            player.sendMessage("Wait for your turn");
        }
    }

    private void handleCardPlay(Lobby lobby, ClientHandler player, String cardName) {
        Card card = player.hand.getCard(cardName);
        if (card == null) {
            player.sendMessage("You don't own such card");
        } else {
            ArrayList<Card> gamePile = lobby.getGame().getPile();
            if (gamePile.size() == 0) {
                handleEmptyPilePlay(lobby, player, card);
            } else {
                handleNonEmptyPilePlay(lobby, player, card);
            }
        }
    }

    private void handleEmptyPilePlay(Lobby lobby, ClientHandler player, Card card) {
        int currentHand = lobby.getGame().getHand();
        if (currentHand == 2 || currentHand == 5 || currentHand == 7) {
            handleSpecialRoundPlay(lobby, player, card);
        } else {
            handleRegularPlay(lobby, player, card);
        }
    }

    private void handleNonEmptyPilePlay(Lobby lobby, ClientHandler player, Card card) {
        Card firstOnPile = lobby.getGame().getPile().get(0);
        if (player.hand.checkIfHaveSuit(firstOnPile)) {
            handleSuitPlay(lobby, player, card, firstOnPile);
        } else {
            handleRegularPlay(lobby, player, card);
        }
    }

    private void handleSpecialRoundPlay(Lobby lobby, ClientHandler player, Card card) {
        if (player.hand.checkIfHaveSuit("Hearts") && card.getSuit().equals("Hearts")) {
            player.sendMessage("You can't play Hearts in this round if you have another suit");
        } else {
            processCardPlay(lobby, player, card);
        }
    }

    private void handleSuitPlay(Lobby lobby, ClientHandler player, Card card, Card firstOnPile) {
        if (card.getSuit().equals(firstOnPile.getSuit())) {
            processCardPlay(lobby, player, card);
        } else {
            player.sendMessage("You have to play a card in the given suit");
        }
    }

    private void handleRegularPlay(Lobby lobby, ClientHandler player, Card card) {
        processCardPlay(lobby, player, card);
    }

    private void processCardPlay(Lobby lobby, ClientHandler player, Card card) {
        lobby.getGame().getPile().add(card);
        lobby.getGame().playedCards.put(player, card);
        player.hand.removeCard(card);
        player.canMove = false;
        lobby.getGame().nextStep();
    }


    public void sendChatMessage(String user, String message, ClientHandler sender) {
        if(user.equalsIgnoreCase("all")){
            for (ClientHandler client : clients) {
                client.sendMessage("Message from " + sender.getClientName() + ": " + message);
            }
        }
        else {
            for(ClientHandler client : clients){
                if(client.getClientName().equals(user)){
                    client.sendMessage("Message from " + sender.getClientName() + ": " + message);
                    return;
                }
            }
        }
    }
    public void invitePlayer(String reciever, ClientHandler sender){
        for(ClientHandler client : clients){
            if(client.getClientName().equals(reciever)){
                client.sendMessage("You have been invited to lobby: " + sender.getLobbyName() + " by " + sender.getClientName());
                return;
            }
        }
    }

    public static void main(String[] args) {
        int port = 12345; // dowolny numer portu
        Server server = new Server(port);
        server.start();
    }
}

