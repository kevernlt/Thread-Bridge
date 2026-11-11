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
     * @return the next state of the bridge
     */
    public BridgeState getNextState() {
        // "Closed" -> "Opening" -> "Open" -> "Closing" -> "Closed"
        return switch (this) {
            case CLOSED -> OPENING;
            case OPENING -> OPEN;
            case OPEN -> CLOSING;
            case CLOSING -> CLOSED;
        };
    }
}
