package fr.kevernlt.iutgon.bridged.enums;

/**
 * Represents the 4 mechanical states of the movable bridge:
 * CLOSED (road traffic allowed, duration 60s),
 * OPENING (raising the deck, duration 3s),
 * OPEN (waterway traffic allowed until empty),
 * CLOSING (lowering the deck, duration 3s).
 */
public enum BridgeState {
    CLOSED(60000),
    OPENING(3000),
    OPEN(0),
    CLOSING(3000);

    private final int delay;

    /**
     * Enum constructor with state duration in milliseconds.
     *
     * @param delay duration in ms
     */
    BridgeState(int delay) {
        this.delay = delay;
    }

    /**
     * Returns the base duration in milliseconds for this state.
     *
     * @return delay in ms
     */
    public int getDelay() {
        return delay;
    }

    /**
     * Returns the next state in the cyclic sequence:
     * CLOSED -> OPENING -> OPEN -> CLOSING -> CLOSED.
     *
     * @return next state
     */
    public BridgeState getNextState() {
        return switch (this) {
            case CLOSED -> OPENING;
            case OPENING -> OPEN;
            case OPEN -> CLOSING;
            case CLOSING -> CLOSED;
        };
    }
}
