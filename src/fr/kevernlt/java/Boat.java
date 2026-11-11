package fr.kevernlt.java;

public class Boat implements Runnable {

    private Bridge bridge;

    public Boat(Bridge b){
        bridge=b;
    }

    @Override
    public void run() {
        bridge.boatForward(this);
    }
}
