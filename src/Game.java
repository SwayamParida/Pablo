import java.util.ArrayList;
import java.util.List;

public class Game {
    private Deck deck;
    private Pile pile;
    private List<Player> players;
    private Player curPlayer;
    private int turn;

    public enum Move { DRAW_DECK, DRAW_PILE }

    public Game() {
        deck = new Deck();
        pile = new Pile();
        players = new ArrayList<>();
    }

    public synchronized Player createNewPlayer(String playerName) {
        Player newPlayer = new Player(playerName, dealCardsToPlayer());
        players.add(newPlayer);
        return newPlayer;
    }

    public synchronized List<Player> getPlayers() {
        return players;
    }

    public synchronized int getNumPlayers() {
        return players.size();
    }

    public Player getCurPlayer() {
        return curPlayer;
    }

    public void initGame() {
        curPlayer = players.get(0);
        turn = 1;
        pile.addCard(deck.drawCard());
    }

    public Card makeMove(Move move) {
        if (move == Move.DRAW_DECK) {
            return curPlayer.draw(deck);
        }
        if (move == Move.DRAW_PILE) {
            return curPlayer.draw(pile);
        }
        return null;
    }

    public void endTurn() {
        curPlayer = players.get((++turn - 1) % getNumPlayers());
    }

    public Card topOfPile() {
        return pile.peek();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Number of players: %d\n", players.size()));
        for (int i = 0; i < players.size(); ++i) {
            stringBuilder.append(String.format("Player #%d:\n%s", i + 1, players.get(i)));
        }
        stringBuilder.append("Deck:\n").append(deck);
        stringBuilder.append("Pile:\n").append(pile);
        return stringBuilder.toString();
    }

    private Card[] dealCardsToPlayer() {
        Card[] cards = new Card[Player.NUM_INITIAL_CARDS];
        for (int i = 0; i < cards.length; ++i) {
            cards[i] = deck.drawCard();
        }
        return cards;
    }
}
