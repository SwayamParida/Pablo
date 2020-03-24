import java.util.Objects;

public class Player {
    private String playerName;
    private Card[] cards;
    private Card cardInHand;
    private int numCards;

    public static final int NUM_INITIAL_CARDS = 4;

    public Player(String name, Card[] dealtCards) {
        playerName = name;
        cards = dealtCards;
        numCards = dealtCards.length;
    }

    public Card[] initialPeek() {
        return new Card[]{cards[0], cards[1]};
    }

    public Card draw(Deck deck) {
        cardInHand = deck.drawCard();
        return cardInHand;
    }

    public Card discard(Pile pile) {
        pile.addCard(cardInHand);
        cardInHand = null;
        return pile.peek();
    }

    public void swapWithHand(int cardIndex) {
        Card temp = cards[cardIndex];
        cards[cardIndex] = cardInHand;
        cardInHand = temp;
    }

    public void swapWithPlayer(int selfIndex, Player player, int playerIndex) {
        Card temp = cards[selfIndex];
        cards[selfIndex] = player.cards[playerIndex];
        player.cards[playerIndex] = temp;
    }

    public Card peek(int cardIndex) {
        return cards[cardIndex];
    }

    public int getTotal() {
        int sum = 0;
        for (Card card : cards)
            sum += card.getValue();
        return sum;
    }

    public String cardReveal() {
        StringBuilder stringBuilder = new StringBuilder("{");
        for (int i = 0; i < numCards - 1; ++i)
            stringBuilder.append(String.format("%s, ", cards[i]));
        stringBuilder.append(String.format("%s}\t(%d)", cards[numCards-1], getTotal()));
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return playerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(playerName, player.playerName);
    }
}
