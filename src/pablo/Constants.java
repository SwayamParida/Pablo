package pablo;

public interface Constants {
    enum DrawSource { PILE, DECK }
    enum State {
        INIT_EXISTING_PLAYERS, ADD_NEW_PLAYER, INITIAL_PEEK, TURN_START, DRAWN_PILE, DRAWN_DECK, KEPT, DISCARDED,
        TELL_PEEK_SELF, PEEKED_SELF, TELL_PEEK_OTHER, PEEKED_OTHER, TELL_SWAP, SWAPPED, CALLED_PABLO,
        DRAW_PILE, DRAW_DECK, KEEP, DISCARD, PEEK_SELF, PEEK_OTHER, SWAP, CALL_PABLO
    }

    int portNumber = 4444;
    String hostName = "138.68.254.97";
    int MAX_CLIENTS = 4;

    String NO_CARD_FILEPATH = "../res/no_card.png";
    String CARD_BACK_FILEPATH = "../res/red_back.png";

    long VIEW_TURN_TIME = 1500;
    long PEEK_TIME = 4000;
}
