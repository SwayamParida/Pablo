public class Card {
    public enum Suit {
        HEARTS (0), DIAMONDS (1), SPADES (2), CLUBS (3);
        private int index;
        Suit(int index) { this.index = index; }
        public int getIndex() { return index; }
    }
    public final static String[] suits = { "Hearts", "Diamonds", "Spades", "Clubs" };
    public final static String[] ranks = {
            "Ace", "Two", "Three", "Four", "Five", "Six", "Seven",
            "Eight", "Nine", "Ten", "Jack", "Queen", "King"
    };
    public Suit suit;
    public int rank;
    public int value;

    public Card(Suit suit, int rank) {
        this.suit = suit;
        this.rank = rank;
        this.value = Math.min(rank, 10); // Assign face cards value of 10
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
}
