package fr.kevernlt.java;

public enum State {
    OPEN(false,true),OPENING(false,false),CLOSE(true,false),CLOSING(false,false);

    private final boolean landCrossing;
    private final boolean boatCrossing;

    State(boolean landCrossing, boolean boatCrossing) {
        this.landCrossing = landCrossing;
        this.boatCrossing = boatCrossing;
    }

    public boolean isLandCrossing() {
        return landCrossing;
    }

    public boolean isBoatCrossing() {
        return boatCrossing;
    }

    public State nextState(State state){
        return switch (state){
            case OPEN -> CLOSING;
            case CLOSE -> OPENING;
            case CLOSING -> CLOSE;
            case OPENING -> OPEN;
        };
    }
}
