package org.example;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private ArrayList<Card> cards;
    private int deckCount;

    public Deck(){
        this.cards = new ArrayList<>();
        fillDeck();
        this.deckCount = cards.size();
    }

    public void fillDeck(){
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        int maxCardValue = 14;
        for (String suit : suits) {
            for (int value = 2; value <= maxCardValue; value++) {
                Card card = new Card(suit, value);
                cards.add(card);
            }
        }
    }
    public void shuffle() {
        Collections.shuffle(cards);
    }
    public Card getNextCard(){
        Card card = cards.get(0);
        cards.remove(0);
        updateCount();
        return card;
    }
    public void clearDeck(){
        cards.clear();
    }
    public int getDeckCount(){
        return this.deckCount;
    }
    public void updateCount(){
        this.deckCount = cards.size();
    }
    public void dealHands(ArrayList<ClientHandler> players){
        int count = 0;
        while(getDeckCount() > 0){
            players.get(count%4).hand.getCards().add(getNextCard());
            count++;
        }
    }
}
