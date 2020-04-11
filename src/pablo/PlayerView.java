package pablo;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static pablo.BoardController.CARD_BACK;

public class PlayerView extends GridPane implements Constants {
    @FXML Label playerName;
    @FXML ImageView topLeftImageView, topRightImageView, bottomLeftImageView, bottomRightImageView;

    private ImageView[] imageViews;
    private boolean isSelectable;
    private Map<BorderPane, SimpleBooleanProperty> clickedPropertyMap, hoveredPropertyMap;
    private ImageView selectedCard;

    public PlayerView() {
        load();
        isSelectable = false;
        imageViews = new ImageView[]{ bottomLeftImageView, bottomRightImageView, topLeftImageView, topRightImageView };
        clickedPropertyMap = new HashMap<>();
        hoveredPropertyMap = new HashMap<>();
        initImageViewEventHandlers();
    }

    private void load() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PlayerView.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        getStylesheets().add(getClass().getResource("ImageViewStyles.css").toExternalForm());
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void initImageViewEventHandlers() {
        PseudoClass imageViewBorderClicked = PseudoClass.getPseudoClass("border_clicked");
        PseudoClass imageViewBorderHovered = PseudoClass.getPseudoClass("border_hovered");
        for (ImageView imageView : imageViews) {
            BorderPane imageViewWrapper = (BorderPane) imageView.getParent();
            SimpleBooleanProperty imageViewClicked = new SimpleBooleanProperty() {
                @Override
                protected void invalidated() {
                    imageViewWrapper.pseudoClassStateChanged(imageViewBorderClicked, get());
                }
            };
            SimpleBooleanProperty imageViewHovered = new SimpleBooleanProperty(){
              @Override
              protected void invalidated() {
                  imageViewWrapper.pseudoClassStateChanged(imageViewBorderHovered, get());
              }
            };
            imageView.setOnMouseClicked(this::cardClicked);
            imageView.setOnMouseEntered(event -> {
                if (isSelectable)
                    imageViewHovered.set(true);
            });
            imageView.setOnMouseExited(event -> {
                if (isSelectable)
                    imageViewHovered.set(false);
            });
            clickedPropertyMap.put(imageViewWrapper, imageViewClicked);
            hoveredPropertyMap.put(imageViewWrapper, imageViewHovered);
        }
    }
    private synchronized void cardClicked(MouseEvent mouseEvent) {
        if (!isSelectable)
            return;

        ImageView clickedCard = (ImageView) mouseEvent.getSource();

        if (selectedCard != null && selectedCard.equals(clickedCard)) {
            deselectCard();
            return;
        }

        if (selectedCard != null)
            deselectCard();
        selectCard(clickedCard);
    }
    private BorderPane getImageViewWrapper(ImageView imageView) {
        return (BorderPane) imageView.getParent();
    }

    public void init(String name) {
        playerName.setText(name);
        for (ImageView imageView : imageViews)
            imageView.setImage(CARD_BACK);
    }

    public void initialPeek(Card[] initialCards) {
        bottomLeftImageView.setImage(initialCards[0].getImage());
        bottomRightImageView.setImage(initialCards[1].getImage());
        BoardController.sleepThenPerformTask(PEEK_TIME, () -> {
            bottomLeftImageView.setImage(CARD_BACK);
            bottomRightImageView.setImage(CARD_BACK);
        });
    }

    public void deselectCard() {
        clickedPropertyMap.get(getImageViewWrapper(selectedCard)).set(false);
        selectedCard = null;
    }

    public void selectCard(ImageView card) {
        clickedPropertyMap.get(getImageViewWrapper(card)).set(true);
        selectedCard = card;
    }

    public void highlightPlayerName(Color highlightColor) {
        playerName.setTextFill(highlightColor);
    }

    public void flashCard(int cardIndex) {
        ImageView card = imageViews[cardIndex];
        clickedPropertyMap.get(getImageViewWrapper(card)).set(true);
        BoardController.sleepThenPerformTask(VIEW_TURN_TIME, () ->
            clickedPropertyMap.get(getImageViewWrapper(card)).set(false)
        );
    }

    public void removeAllBorders() {
        for (ImageView imageView : imageViews) {
            clickedPropertyMap.get(getImageViewWrapper(imageView)).set(false);
            hoveredPropertyMap.get(getImageViewWrapper(imageView)).set(false);
        }
    }

    public void setSelectable(boolean selectable) {
        isSelectable = selectable;
    }

    public ImageView getSelectedCard() {
        return selectedCard;
    }

    public String getPlayerName() {
        return playerName.getText();
    }

    public ImageView[] getImageViews() {
        return imageViews;
    }

    public int getSelectedCardIndex() {
        for (int i = 0; i < imageViews.length; ++i) {
            if (imageViews[i].equals(selectedCard))
                return i;
        }
        return -1;
    }
}
