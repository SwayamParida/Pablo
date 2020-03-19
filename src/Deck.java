import java.util.ArrayList;
import java.util.List;

public class Deck {
    private List<Card> cards;
    private int numCards;

    public Deck() {
        numCards = 52;
        cards = new ArrayList<>(numCards);
        initializeCards();
    }

    
    private void initializeCards() {
        Card.Suit[] suits = { Card.Suit.HEARTS, Card.Suit.DIAMONDS, Card.Suit.SPADES, Card.Suit.CLUBS};
        int[] ranks = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 };

        for (Card.Suit suit : suits) {
            for (int rank : ranks)
                cards.add(new Card(suit, rank));
        }
    }
}
