package org.example;

import java.util.ArrayList;

public class Lobby {

    private String name;

    private ClientHandler creator;
    private ArrayList<ClientHandler> players = new ArrayList<>();
    private Game game;

    public Lobby(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addPlayer(ClientHandler player) {
        players.add(player);
    }
    public void setCreator(ClientHandler creator){
        this.creator = creator;
    }
    public ClientHandler getCreator(){
        return this.creator;
    }
    public void setGame(Game game){
        this.game = game;
    }
    public Game getGame(){
        return this.game;
    }

    public ArrayList<ClientHandler> getPlayers() {
        return players;
    }

    public void removePlayer(ClientHandler player) {
        players.remove(player);
    }

    public void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler player : players) {
            if (player != sender) {
                player.sendMessage(message);
            }
        }
    }
}

