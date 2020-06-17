package pablo.client;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import pablo.Card;
import pablo.Constants;
import pablo.Player;
import pablo.Protocol;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BoardController implements Constants {
    public static Image NO_CARD;
    public static Image CARD_BACK;

    private String playerName;
    private PlayerView[] playerViews;
    private int numPlayersInitialized;
    private Map<String, PlayerView> playerViewMap;
    private Card drawnCard;
    private DrawSource drawSource;
    private boolean selfPeekMode, otherPeekMode, swapMode;

    public final Protocol clientOutput;

    @FXML public PlayerView bottomPlayerView, topPlayerView, leftPlayerView, rightPlayerView;
    @FXML public ImageView discardPileImageView, drawnCardImageView;

    public BoardController() {
        numPlayersInitialized = 0;
        playerViewMap = new HashMap<>();
        clientOutput = new Protocol(null, null);
        selfPeekMode = false;
        otherPeekMode = false;
        swapMode = false;
        initImages();
    }

    public static void sleepThenPerformTask(long sleepTime, Runnable toRun) {
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(event -> toRun.run());
        new Thread(sleeper).start();
    }

    @FXML
    public void initialize() {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Pablo");
        inputDialog.setHeaderText("New player");
        inputDialog.setContentText("Player name");
        Optional<String> result = inputDialog.showAndWait();

        result.ifPresent(name -> playerName = name);
        initClientPlayer();
        playerViews = new PlayerView[]{bottomPlayerView, topPlayerView, leftPlayerView, rightPlayerView};
    }

    public void initClientPlayer() {
        bottomPlayerView.init(playerName);
        ++numPlayersInitialized;
        playerViewMap.put(playerName, bottomPlayerView);
    }

    public void initRemainingPlayers(List<Player> players) {
        for (Player player : players) {
            PlayerView playerView = playerViews[numPlayersInitialized++];
            playerView.init(player.toString());
            playerViewMap.put(player.toString(), playerView);
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public PlayerView getPlayerViewByName(String playerName) {
        return playerViewMap.get(playerName);
    }

    public PlayerView[] getPlayerViews() {
        return playerViews;
    }

    public void setSelfPeekMode(boolean selfPeekMode) {
        this.selfPeekMode = selfPeekMode;
    }

    public void setOtherPeekMode(boolean otherPeekMode) {
        this.otherPeekMode = otherPeekMode;
    }

    public void setSwapMode(boolean swapMode) {
        this.swapMode = swapMode;
    }

    public void initialPeek(Card[] initialCards) {
        bottomPlayerView.initialPeek(initialCards);
    }

    public void updateDiscardPile(Card topOfPile) {
        if (topOfPile == null)
            discardPileImageView.setImage(NO_CARD);
        else
            discardPileImageView.setImage(topOfPile.getImage());
    }

    public void setCurPlayer(Player player, Color color) {
        for (PlayerView playerView : playerViews) {
            playerView.highlightPlayerName(Color.BLACK);
            playerView.removeAllBorders();
            playerView.setSelectable(false);
        }
        PlayerView playerView = playerViewMap.get(player.toString());
        playerView.highlightPlayerName(color);
        playerView.setSelectable(true);
    }

    public void viewDrawnCard(Card card, DrawSource source) {
        drawnCard = card;
        drawSource = source;
        drawnCardImageView.setImage(card.getImage());
    }

    public void swapWithHand() {
        bottomPlayerView.getSelectedCard().setImage(drawnCard.getImage());
        sleepThenPerformTask(VIEW_TURN_TIME, () -> {
            bottomPlayerView.getSelectedCard().setImage(CARD_BACK);
            bottomPlayerView.deselectCard();
        });
    }

    public void discard(Card discardedCard) {
        drawnCard = null;
        drawSource = null;
        discardPileImageView.setImage(discardedCard.getImage());
        drawnCardImageView.setImage(NO_CARD);
    }

    public void flashPlayerCard(Player curPlayer, int replacedCardIndex) {
        PlayerView curPlayerView = playerViewMap.get(curPlayer.toString());
        curPlayerView.flashCard(replacedCardIndex);
    }

    @FXML
    public synchronized void drawFromPile() {
        System.out.println("Pile clicked");
        clientOutput.setFields(Protocol.drawPile());
    }

    @FXML
    public synchronized void drawFromDeck() {
        System.out.println("Deck clicked");
        clientOutput.setFields(Protocol.drawDeck());
    }

    @FXML
    public synchronized void actOnDrawnCard() {
        System.out.println("Drawn card clicked");
        int cardIndex;
        if ((cardIndex = bottomPlayerView.getSelectedCardIndex()) != -1) {
            System.out.println("Keep card");
            clientOutput.setFields(Protocol.keepCard(cardIndex));
        } else if (drawSource == DrawSource.DECK) {
            System.out.println("Discard card");
            clientOutput.setFields(Protocol.discardCard());
        }
    }

    // Persistent storage of variables across two clicks
    private static String swappedPlayerName = null;
    private static int givenCardIndex = -1, takenCardIndex = -1;
    @FXML
    public synchronized void playerViewClicked(MouseEvent event) {
        if (event.getSource().equals(bottomPlayerView) && selfPeekMode) {
            int peekedCardIndex = bottomPlayerView.getSelectedCardIndex();
            clientOutput.setFields(Protocol.peekSelf(peekedCardIndex));
        }
        if (!event.getSource().equals(bottomPlayerView) && otherPeekMode) {
            String peekedPlayerName = ((PlayerView) event.getSource()).getPlayerName();
            int peekedCardIndex = ((PlayerView) event.getSource()).getSelectedCardIndex();
            clientOutput.setFields(Protocol.peekOther(peekedPlayerName, peekedCardIndex));
        }
        if (swapMode) {
            if (!event.getSource().equals(bottomPlayerView)) {
                swappedPlayerName = ((PlayerView) event.getSource()).getPlayerName();
                takenCardIndex = ((PlayerView) event.getSource()).getSelectedCardIndex();
            } else {
                givenCardIndex = bottomPlayerView.getSelectedCardIndex();
            }
            if (swappedPlayerName != null && givenCardIndex != -1 && takenCardIndex != -1) {
                clientOutput.setFields(Protocol.swap(swappedPlayerName, givenCardIndex, takenCardIndex));
                swappedPlayerName = null;
                givenCardIndex = -1;
                takenCardIndex = -1;
            }
        }
    }

    @FXML
    public synchronized void callPablo() {
        if (drawnCard == null && !(selfPeekMode || otherPeekMode || swapMode)) {
            clientOutput.setFields(Protocol.callPablo());
        }
    }

    private void initImages() {
        try {
            NO_CARD = new Image(getClass().getResource(NO_CARD_FILEPATH).toURI().toString(), 100, 100, true, true);
            CARD_BACK = new Image(getClass().getResource(CARD_BACK_FILEPATH).toURI().toString(), 100, 100, true, true);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
