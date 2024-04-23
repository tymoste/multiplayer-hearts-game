package org.example;

public class Card {
    private String suit;
    private int value;
    private String name;

    public Card(String suit, int value){
        this.suit = suit;
        this.value = value;
        this.name = generateName();
    }

    public String generateName(){
        String newVal = switch (value) {
                            case 11 -> "J";
                            case 12 -> "Q";
                            case 13 -> "K";
                            case 14 -> "A";
                            default -> Integer.toString(value);
        };
        return suit.charAt(0)+newVal;
    }

    public String getSuit() {
        return suit;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
