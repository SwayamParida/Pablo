import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Game {
    private Deck deck;
    private Pile pile;
    private List<Player> players;
    private Player curPlayer;
    private int turn;

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

    public Card drawDeck() {
        return curPlayer.draw(deck);
    }

    public Card drawPile() {
        return curPlayer.draw(pile);
    }

    public Card discard() {
        return curPlayer.discard(pile);
    }

    public Card keep(int index) {
        curPlayer.swapWithHand(index);
        return curPlayer.discard(pile);
    }

    public Card peekSelf(int index) {
        return curPlayer.peek(index);
    }

    public Card peekOther(Player target, int index) {
        return target.peek(index);
    }

    public void playerSwap(Player target, int sourceIndex, int targetIndex) {
        curPlayer.swapWithPlayer(sourceIndex, target, targetIndex);
    }

    public void endTurn() {
        curPlayer = players.get((++turn - 1) % getNumPlayers());
    }

    public Card topOfPile() {
        return pile.peek();
    }

    public Player findPlayerByName(String playerName) {
        for (Player p : players) {
            if (playerName.equals(p.toString()))
                return p;
        }
        return null;
    }

    public Player getWinner() {
        return Collections.min(players, Comparator.comparingInt(Player::getTotal));
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
