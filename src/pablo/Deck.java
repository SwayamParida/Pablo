package pablo;

import java.util.Collections;
import java.util.Stack;

public class Deck {
    protected Stack<Card> cards;
    protected int numCards;

    public Deck() {
        numCards = 52;
        cards = new Stack<>();
        initializeCards();
        Collections.shuffle(cards);
    }

    private void initializeCards() {
        Card.Suit[] suits = { Card.Suit.HEARTS, Card.Suit.DIAMONDS, Card.Suit.SPADES, Card.Suit.CLUBS};
        int[] ranks = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 };

        for (Card.Suit suit : suits) {
            for (int rank : ranks)
                cards.push(new Card(suit, rank));
        }
    }

    public Card drawCard() {
        --numCards;
        return cards.pop();
    }

    public Card peek() {
        return cards.peek();
    }

    public String toString() {
        String str = String.format("Number of cards: %d\n", numCards);
        if (!cards.empty())
            str += String.format("Top card: %s\n", cards.peek());
        return str;
    }
}
