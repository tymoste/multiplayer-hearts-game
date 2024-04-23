package org.example;

import java.util.*;

public class Game {
    private Lobby lobby;
    private Deck deck;
    private int hand;
    private int round;
    private ArrayList<ClientHandler> players;
    private ClientHandler currentPlayer;
    private ClientHandler loser;
    private Card highestCard;
    private ArrayList<Card> pile;
    public HashMap<ClientHandler, Card> playedCards = new HashMap<>();
    private Deck deck1;

    public Game(Lobby lobby) {
        this.lobby = lobby;
        this.players = lobby.getPlayers();
        this.pile = new ArrayList<>();
        this.deck = new Deck();
        this.hand = 1;
        this.round = 1;
        this.loser = players.get(new Random().nextInt(players.size()));
    }

    public void sendMessage(ClientHandler player, String mess) {
        player.sendMessage(mess);
    }
    public int getHand(){
        return this.hand;
    }

    public void sendAll(String mess) {
        for (ClientHandler player : players) {
            sendMessage(player, mess);
        }
    }

    public ArrayList<Card> getPile() {
        return this.pile;
    }

    public void sendPileAll(){
        for(Card card : pile){
            sendAll(card.getName());
        }
    }

    public void skipCurrentHand(){
        this.pile.clear();
        for(ClientHandler player : players){
            player.hand.clearHand();
            player.canMove = false;
            player.gamePoints += (new Random().nextInt(101));
        }
        loser = players.get(new Random().nextInt(players.size()));
        round = 1;
        hand++;
        beginHand();
    }

    public void beginHand(){
        if(hand == 8){
            endGame();
            return;
        }
        deck.clearDeck();
        deck.fillDeck();
        deck.updateCount();
        deck.shuffle();
        System.out.println("Deck Shuffled");
        deck.dealHands(players);
        System.out.println("Hands dealt");
        sendAll("Current points");
        for (ClientHandler player : players){
            sendAll(player.getClientName() + ": " + player.gamePoints);
        }
        sendAll("\n");
        sendAll("Hand " + hand + " started");
        nextStep();
    }
    public void endGame(){
        ClientHandler loser = determineLoser();
        for (ClientHandler player : players){
            sendAll(player.getClientName() + ": " + player.gamePoints);
        }
        sendAll("PLayer "+loser.getClientName()+ " lost");
        for (ClientHandler player : players){
            player.canMove = false;
            player.gamePoints = 0;
            player.inGame = false;
            player.inLobby = true;
        }
    }

    public ClientHandler determineLoser(){
        ClientHandler loser = players.get(0);
        for(ClientHandler player : players){
            if(player.gamePoints > loser.gamePoints){
                loser = player;
            }
        }
        return loser;
    }

    public void nextStep(){
        sendPileAll();
        sendAll("\n");
        if(this.pile.size() < 4){
            if(this.pile.size() == 0){
                currentPlayer = loser;
            }else{
                currentPlayer = players.get((players.indexOf(currentPlayer) + 1) % players.size());
            }
            currentPlayer.sendMessage("Your turn!");
            currentPlayer.canMove = true;
        }
        if(this.pile.size() == 4){
            checkHighestCard();
            evalPoints(hand);
            for(ClientHandler player : players){
                sendAll(player.getClientName() + " " + player.gamePoints);
            }
            sendAll("\n");
            this.pile.clear();
            round++;
            if(round == 14) {
                round = 1;
                hand++;
                beginHand();
            } else {
                nextStep();
            }
        }
    }
    public void checkHighestCard(){
        Card highest = pile.get(0);
        for(Map.Entry<ClientHandler, Card> entry : playedCards.entrySet()){
            ClientHandler toCheck = entry.getKey();
            Card hisCard = entry.getValue();
            System.out.println(hisCard.getName());
            if(highest.getSuit().equals(hisCard.getSuit())){
                System.out.println(highest.getName() + " " + hisCard.getName());
                if(hisCard.getValue() > highest.getValue()){
                    loser = toCheck;
                    highest = hisCard;
                    System.out.println(loser.getClientName());
                }
            }
        }
        sendAll("Loser: " + loser.getClientName());
    }
    public void evalPoints(int hand){

        switch (hand) {
            case 1 -> evalPointsHand1();
            case 2 -> evalPointsHand2();
            case 3 -> evalPointsHand3();
            case 4 -> evalPointsHand4();
            case 5 -> evalPointsHand5();
            case 6 -> evalPointsHand6();
            case 7 -> evalPointsHand7();
        }
    }

    public void evalPointsHand1(){
        int points = 0;
        for(Card card : pile){
            points += 5;
        }
        loser.gamePoints += points;
    }

    public void evalPointsHand2(){
        int points = 0;
        for(Card card : pile){
            if(card.getSuit().equals("Hearts")){
               points += 20;
            }
        }
        loser.gamePoints += points;
    }

    public void evalPointsHand3(){
        int points = 0;
        for(Card card : pile){
           if(card.getValue() == 12){
               points += 60;
           }
        }
        loser.gamePoints += points;
    }

    public void evalPointsHand4(){
        int points = 0;
        for(Card card : pile){
            if(card.getValue() == 11 || card.getValue() == 13){
                points += 30;
            }
        }
        loser.gamePoints += points;
    }

    public void evalPointsHand5(){
        int points = 0;
        for(Card card : pile){
            if(card.getSuit().equals("Hearts") && card.getValue() == 13){
                points += 150;
            }
        }
        loser.gamePoints += points;
    }

    public void evalPointsHand6(){
        int points = 0;
        if(round == 7 || round == 13){
            points += 75;
        }
        loser.gamePoints += points;
    }

    public void evalPointsHand7(){
        int points = 20;
        if(round == 7 || round == 13){
            points += 75;
        }
        for(Card card : pile){
            if(card.getSuit().equals("Heart")){
                points += 20;
                if(card.getValue() == 13){
                    points += 150;
                }
            }
            if(card.getValue() == 12){
                points += 60;
            }
            if(card.getValue() == 11 || card.getValue() == 13){
                points += 30;
            }
        }
        loser.gamePoints += points;
    }



}





