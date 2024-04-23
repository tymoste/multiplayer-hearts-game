package org.example;

import java.util.ArrayList;

public class Hand {
    private ArrayList<Card> cards = new ArrayList<>();

    public ArrayList<Card> getCards() {
        return cards;
    }

    public Card getCard(String cardName){
        for (Card card : cards){
            if(card.getName().equalsIgnoreCase(cardName)){
                return card;
            }
        }
        return null;
    }
    public void clearHand(){
        this.cards.clear();
    }
    public void removeCard(Card card){
        cards.remove(card);
    }
    public boolean checkIfHaveSuit(Card cardOnPile){
        String searchFor = cardOnPile.getSuit();
        for (Card card : cards){
            if(card.getSuit().equals(searchFor)) {
                return true;
            }
        }
        return false;
    }
    public boolean checkIfHaveSuit(String suit){
        for (Card card : cards){
            if(card.getSuit().equals(suit)) {
                return true;
            }
        }
        return false;
    }
}
