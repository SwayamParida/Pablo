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
        return String.format("%s of %s", suits[suit.getIndex()], ranks[rank]);
    }

    public boolean equals(Card card) {
        return this.suit == card.suit
                && this.rank == card.rank
                && this.value == card.value;
    }
}
