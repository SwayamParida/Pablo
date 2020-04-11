package pablo;

import javafx.scene.image.Image;

import java.io.Serializable;
import java.net.URISyntaxException;

public class Card implements Serializable {
    public enum Suit {
        HEARTS (0), DIAMONDS (1), SPADES (2), CLUBS (3);
        private int index;
        Suit(int index) { this.index = index; }
        public int getIndex() { return index; }
    }
    private final static String[] suits = { "Hearts", "Diamonds", "Spades", "Clubs" };
    private final static String[] ranks = {
            "Ace", "Two", "Three", "Four", "Five", "Six", "Seven",
            "Eight", "Nine", "Ten", "Jack", "Queen", "King"
    };
    private Suit suit;
    private int rank;
    private int value;

    public Card(Suit suit, int rank) {
        this.suit = suit;
        this.rank = rank;
        this.value = Math.min(rank, 10); // Assign face cards value of 10
    }

    public int getValue() {
        return value;
    }

    public Image getImage() {
        return new Image(getImageFilename(), 100, 100, true, true);
    }

    public String toString() {
        return String.format("%s of %s", ranks[rank-1], suits[suit.getIndex()]);
    }

    public boolean equals(Object card) {
        return card.getClass().equals(this.getClass())
                && this.suit == ((Card) card).suit
                && this.rank == ((Card) card).rank
                && this.value == ((Card) card).value;
    }

    private String getImageFilename() {
        String prefix;
        switch (rank) {
            case 1:  prefix = "A"; break;
            case 11: prefix = "J"; break;
            case 12: prefix = "Q"; break;
            case 13: prefix = "K"; break;
            default: prefix = String.valueOf(rank);
        }
        String filename = String.format("res/%s%s.png", prefix, suits[suit.getIndex()].charAt(0));
        try {
            return getClass().getResource(filename).toURI().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}
