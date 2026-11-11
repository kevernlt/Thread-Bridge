package fr.kevernlt.java;

public class Car implements Vehicle {

    private final Bridge bridge;
    private static final int size = 1;
    private final Direction direction;
    private boolean exit;
    private Integer position;

    public Car(Bridge b,Direction direction){
        if(direction==Direction.SOUTH||direction==Direction.NORTH)throw new WrongDirectionException("La direction est impossible");
        bridge=b;
        this.direction=direction;
        exit=false;
        position=null;
    }

    @Override
    public int getSize(){
        return size;
    }

    @Override
    public void run() {
        entry();
        try {
            cross();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public void entry() {
        bridge.LandEntry(this,direction);
    }

    @Override
    public void cross() throws InterruptedException {
        while(!exit){
            exit=bridge.landForward(this);
        }
    }

    @Override
    public Integer getPosition() {
        return position;
    }

    @Override
    public void setPosition(Integer position) {
        this.position=position;
    }

    @Override
    public void sleep(int i) throws InterruptedException {
        Thread.sleep(i);
    }
}
