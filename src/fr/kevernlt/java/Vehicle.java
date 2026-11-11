package fr.kevernlt.java;

public interface Vehicle extends Runnable {

    @Override
    void run();

    Direction getDirection();
    int getSize();
    void entry();
    void cross() throws InterruptedException;
    Integer getPosition();
    void setPosition(Integer position);

    void sleep(int i) throws InterruptedException;
}
