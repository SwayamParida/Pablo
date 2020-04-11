package pablo;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static pablo.BoardController.CARD_BACK;

public class Protocol implements Serializable, Constants {
    private State state;
    private Object data;

    public Protocol(State state, Object data) {
        this.state = state;
        this.data = data;
    }

    public State getState() {
        return state;
    }
    public Object getData() {
        return data;
    }
    public void setFields(Protocol p) {
        state = p.state;
        data = p.data;
    }

    // Server to client communication methods
    public static Protocol sendExistingPlayers(List<Player> players) {
        return new Protocol(State.INIT_EXISTING_PLAYERS, players);
    }
    public static Protocol sendNewPlayer(Player newPlayer) {
        return new Protocol(State.ADD_NEW_PLAYER, newPlayer);
    }
    public static Protocol initialPeek(Card[] initialCards) {
        return new Protocol(State.INITIAL_PEEK, initialCards);
    }
    public static Protocol startTurn(Player curPlayer, Card topOfPile) {
        return new Protocol(State.TURN_START, Arrays.asList(curPlayer, topOfPile));
    }
    public static Protocol drawnPile(Player curPlayer, Card newTopOfPile, Card drawnCard) {
        return new Protocol(State.DRAWN_PILE, Arrays.asList(curPlayer, newTopOfPile, drawnCard));
    }
    public static Protocol drawnDeck(Player curPlayer, Card drawnCard) {
        return new Protocol(State.DRAWN_DECK, Arrays.asList(curPlayer, drawnCard));
    }
    public static Protocol keptCard(Player curPlayer, int replacedCardIndex, Card discardedCard) {
        return new Protocol(State.KEPT, Arrays.asList(curPlayer, replacedCardIndex, discardedCard));
    }
    public static Protocol discardedCard(Card card) {
        return new Protocol(State.DISCARDED, card);
    }
    public static Protocol tellPeekSelf(Player curPlayer) {
        return new Protocol(State.TELL_PEEK_SELF, curPlayer);
    }
    public static Protocol peekedSelf(Player curPlayer, int cardIndex, Card card) {
        return new Protocol(State.PEEKED_SELF, Arrays.asList(curPlayer, cardIndex, card));
    }
    public static Protocol tellPeekOther(Player curPlayer) {
        return new Protocol(State.TELL_PEEK_OTHER, curPlayer);
    }
    public static Protocol peekedOther(Player curPlayer, Player peekedPlayer, int peekedCardIndex, Card peekedCard) {
        return new Protocol(State.PEEKED_OTHER, Arrays.asList(curPlayer, peekedPlayer, peekedCardIndex, peekedCard));
    }
    public static Protocol tellSwap(Player curPlayer) {
        return new Protocol(State.TELL_SWAP, curPlayer);
    }
    public static Protocol swapped(Player curPlayer, Player swappedPlayer, int givenCardIndex, int takenCardIndex) {
        return new Protocol(State.SWAPPED, Arrays.asList(curPlayer, swappedPlayer, givenCardIndex, takenCardIndex));
    }
    public static Protocol calledPablo(List<Player> players) {
        return new Protocol(State.CALLED_PABLO, players);
    }
    // Client to server communication methods
    public static Protocol drawDeck() {
        return new Protocol(State.DRAW_DECK, null);
    }
    public static Protocol drawPile() {
        return new Protocol(State.DRAW_PILE, null);
    }
    public static Protocol keepCard(int cardIndex) {
        return new Protocol(State.KEEP, cardIndex);
    }
    public static Protocol discardCard() {
        return new Protocol(State.DISCARD, null);
    }
    public static Protocol peekSelf(int cardIndex) {
        return new Protocol(State.PEEK_SELF, cardIndex);
    }
    public static Protocol peekOther(String playerName, int cardIndex) {
        return new Protocol(State.PEEK_OTHER, Arrays.asList(playerName, cardIndex));
    }
    public static Protocol swap(String swappedPlayerName, int givenCardIndex, int takenCardIndex) {
        return new Protocol(State.SWAP, Arrays.asList(swappedPlayerName, givenCardIndex, takenCardIndex));
    }
    public static Protocol callPablo() {
        return new Protocol(State.CALL_PABLO, null);
    }

    public static boolean processClientInput(BoardController controller, Protocol input) {
        List<Object> dataFields = null;
        if (input.data instanceof List)
            dataFields = (List<Object>) input.data;

        switch (input.state) {
            case INIT_EXISTING_PLAYERS:
                Platform.runLater(() ->
                    controller.initRemainingPlayers((List<Player>) input.data)
                );
                return false;
            case ADD_NEW_PLAYER:
                Platform.runLater(() ->
                    controller.initRemainingPlayers(Collections.singletonList((Player) input.data))
                );
                return false;
            case INITIAL_PEEK:
                Platform.runLater(() ->
                    controller.initialPeek((Card[]) input.data)
                );
                return false;
            case TURN_START: {
                Player curPlayer = (Player) dataFields.get(0);
                Object topOfPile = dataFields.get(1);
                Platform.runLater(() -> {
                    controller.updateDiscardPile(topOfPile == null ? null : (Card) topOfPile);
                    controller.setCurPlayer(curPlayer, Color.RED);
                });
                return controller.getPlayerName().equals(curPlayer.toString());
            }
            case DRAWN_PILE: {
                Player curPlayer = (Player) dataFields.get(0);
                Object topOfPile = dataFields.get(1);
                Platform.runLater(() ->
                        controller.updateDiscardPile(topOfPile == null ? null : (Card) topOfPile)
                );
                if (controller.getPlayerName().equals(curPlayer.toString())) {
                    Card drawnCard = (Card) dataFields.get(2);
                    Platform.runLater(() ->
                            controller.viewDrawnCard(drawnCard, DrawSource.PILE)
                    );
                    return true;
                }
                return false;
            }
            case DRAWN_DECK: {
                Player curPlayer = (Player) dataFields.get(0);
                if (controller.getPlayerName().equals(curPlayer.toString())) {
                    Card drawnCard = (Card) dataFields.get(1);
                    Platform.runLater(() ->
                            controller.viewDrawnCard(drawnCard, DrawSource.DECK)
                    );
                    return true;
                }
                return false;
            }
            case KEPT: {
                Player curPlayer = (Player) dataFields.get(0);
                int replacedCardIndex = (int) dataFields.get(1);
                Card discardedCard = (Card) dataFields.get(2);
                if (controller.getPlayerName().equals(curPlayer.toString())) {
                    Platform.runLater(() -> {
                        controller.swapWithHand();
                        controller.discard(discardedCard);
                    });
                } else {
                    Platform.runLater(() -> {
                        controller.flashPlayerCard(curPlayer, replacedCardIndex);
                        controller.discard(discardedCard);
                    });
                }
                return false;
            }
            case DISCARDED: {
                Card discardedCard = (Card) input.data;
                controller.discard(discardedCard);
                return false;
            }
            case TELL_PEEK_SELF: {
                Player curPlayer = (Player) input.data;
                if (controller.getPlayerName().equals(curPlayer.toString())) {
                    Platform.runLater(() -> {
                        controller.setSelfPeekMode(true);
                    });
                    return true;
                }
                return false;
            }
            case PEEKED_SELF: {
                Player curPlayer = (Player) dataFields.get(0);
                int peekedCardIndex = (int) dataFields.get(1);
                Card peekedCard = (Card) dataFields.get(2);
                if (controller.getPlayerName().equals(curPlayer.toString())) {
                    Platform.runLater(() -> {
                        controller.bottomPlayerView.getSelectedCard().setImage(peekedCard.getImage());
                        BoardController.sleepThenPerformTask(VIEW_TURN_TIME, () -> {
                            controller.bottomPlayerView.getSelectedCard().setImage(CARD_BACK);
                            controller.bottomPlayerView.deselectCard();
                            controller.setSelfPeekMode(false);
                        });
                    });
                } else {
                    Platform.runLater(() ->
                        controller.flashPlayerCard(curPlayer, peekedCardIndex)
                    );
                }
                return false;
            }
            case TELL_PEEK_OTHER: {
                Player curPlayer = (Player) input.data;
                if (controller.getPlayerName().equals(curPlayer.toString())) {
                    Platform.runLater(() -> {
                        controller.setOtherPeekMode(true);
                        for (PlayerView playerView : controller.getPlayerViews()) {
                            if (!curPlayer.toString().equals(playerView.getPlayerName())) {
                                playerView.setSelectable(true);
                                playerView.highlightPlayerName(Color.GOLD);
                            }
                        }
                    });
                    return true;
                }
                return false;
            }
            case PEEKED_OTHER: {
                Player curPlayer = (Player) dataFields.get(0);
                Player peekedPlayer = (Player) dataFields.get(1);
                int peekedCardIndex = (int) dataFields.get(2);
                Card peekedCard = (Card) dataFields.get(3);
                if (controller.getPlayerName().equals(curPlayer.toString())) {
                    Platform.runLater(() -> {
                        PlayerView peekedPlayerView = controller.getPlayerViewByName(peekedPlayer.toString());
                        peekedPlayerView.getSelectedCard().setImage(peekedCard.getImage());
                        BoardController.sleepThenPerformTask(VIEW_TURN_TIME, () -> {
                            peekedPlayerView.getSelectedCard().setImage(CARD_BACK);
                            peekedPlayerView.deselectCard();
                            controller.setOtherPeekMode(false);
                        });
                    });
                } else {
                    Platform.runLater(() -> {
                        controller.flashPlayerCard(peekedPlayer, peekedCardIndex);
                    });
                }
                return false;
            }
            case TELL_SWAP: {
                Player curPlayer = (Player) input.data;
                if (controller.getPlayerName().equals(curPlayer.toString())) {
                    Platform.runLater(() -> {
                        controller.setSwapMode(true);
                        for (PlayerView playerView : controller.getPlayerViews()) {
                            playerView.setSelectable(true);
                            playerView.highlightPlayerName(Color.GOLD);
                        }
                    });
                    return true;
                }
                return false;
            }
            case SWAPPED: {
                Player curPlayer = (Player) dataFields.get(0);
                Player swappedPlayer = (Player) dataFields.get(1);
                int givenCardIndex = (int) dataFields.get(2);
                int takenCardIndex = (int) dataFields.get(3);
                if (controller.getPlayerName().equals(curPlayer.toString())) {
                    Platform.runLater(() -> {
                        PlayerView swappedPlayerView = controller.getPlayerViewByName(swappedPlayer.toString());
                        BoardController.sleepThenPerformTask(VIEW_TURN_TIME, () -> {
                            swappedPlayerView.deselectCard();
                            controller.bottomPlayerView.deselectCard();
                            controller.setSwapMode(false);
                        });
                    });
                } else {
                    Platform.runLater(() -> {
                        controller.flashPlayerCard(swappedPlayer, takenCardIndex);
                        controller.flashPlayerCard(curPlayer, givenCardIndex);
                    });
                }
                return false;
            }
            case CALLED_PABLO: {
                List<Player> players = (List<Player>) input.data;
                Platform.runLater(() -> {
                    for (Player player : players) {
                        PlayerView playerView = controller.getPlayerViewByName(player.toString());
                        ImageView[] cardImageViews = playerView.getImageViews();
                        Card[] cards = player.getCards();
                        for (int i = 0; i < cardImageViews.length; ++i) {
                            cardImageViews[i].setImage(cards[i].getImage());
                        }
                    }
                });
                return false;
            }
        }
        return false;
    }
    public static boolean isValidResponse(Protocol input, Protocol output) {
        if (input.getState() == null)
            return false;
        switch (input.getState()) {
            case TURN_START:
                return output.getState() == State.DRAW_PILE || output.getState() == State.DRAW_DECK || output.getState() == State.CALL_PABLO;
            case DRAWN_PILE:
                return output.getState() == State.KEEP;
            case DRAWN_DECK:
                return output.getState() == State.KEEP || output.getState() == State.DISCARD;
            case TELL_PEEK_SELF:
                return output.getState() == State.PEEK_SELF;
            case TELL_PEEK_OTHER:
                return output.getState() == State.PEEK_OTHER;
            case TELL_SWAP:
                return output.getState() == State.SWAP;
        }
        return false;
    }
}
