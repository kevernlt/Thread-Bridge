package fr.kevernlt.iutgon.bridged.enums;

// To "simplify" : this is the pov of a boat, if closed, cars can drive but boats cant
// and for states closing, opening no one can move (or they will die???)
public enum BridgeState {
    CLOSED,
    CLOSING,
    OPENING,
    OPEN;

    /**
     * Returns the next state of the bridge based on its current state.
     * @param currentState the current state of the bridge
     * @return the next state of the bridge
     */
    public static BridgeState getNextState(BridgeState currentState) {
        // "Closed" -> "Opening" -> "Open" -> "Closing" -> "Closed"
        return switch (currentState) {
            case CLOSED -> OPENING;
            case OPENING -> OPEN;
            case OPEN -> CLOSING;
            case CLOSING -> CLOSED;
        };
    }
}
