import java.util.Stack;

public class Pile extends Deck {
    public Pile() {
        numCards = 0;
        cards = new Stack<>();
    }

    public void addCard(Card card) {
        cards.push(card);
        ++numCards;
    }
}
