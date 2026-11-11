package fr.kevernlt.java;

import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class Bridge {

    private State state;

    private final int sizeBridge;

    private final List<Vehicle> vehicleEtoW;

    private final List<Vehicle> vehicleWtoE;

    private final AtomicReferenceArray<Vehicle> segmentEtoW;

    private final AtomicReferenceArray<Vehicle> segmentWtoE;

    private List<Boat> boatStoN;

    private List<Boat> boatNtoS;

    public Bridge(int n) {
        sizeBridge=n;
        state = State.CLOSE;
        this.vehicleEtoW = new LinkedList<>();
        this.vehicleWtoE = new LinkedList<>();
        this.segmentEtoW = new AtomicReferenceArray<>(n);
        this.segmentWtoE = new AtomicReferenceArray<>(n);
        this.boatNtoS = new LinkedList<>();
        this.boatStoN = new LinkedList<>();
    }

    public synchronized void LandEntry(Vehicle vehicle,Direction direction){
        if(direction == Direction.EAST)vehicleWtoE.add(vehicle);
        else if(direction == Direction.WEST)vehicleEtoW.add(vehicle);
    }

    public boolean landForward(Vehicle vehicle) throws InterruptedException {
        if(vehicle.getDirection()==Direction.EAST){
            if(vehicle.getPosition()==null){
                if(vehicleWtoE.getFirst()==vehicle){
                    if(segmentWtoE.compareAndSet(0,null,vehicle)){
                        vehicle.sleep(500);
                        vehicle.setPosition(0);
                    }
                }
            }else{
                if(vehicle.getPosition()+1==sizeBridge){
                    vehicle.sleep(500);
                    segmentWtoE.set(vehicle.getPosition(), null);
                    return true;
                }
                if(segmentWtoE.compareAndSet(vehicle.getPosition()+1,null,vehicle )){
                    vehicle.sleep(500);
                    segmentWtoE.set(vehicle.getPosition(), null);
                    vehicle.setPosition(vehicle.getPosition()+1);
                }
            }
        }else {
            if(vehicle.getPosition()==null){
                if(vehicleEtoW.getFirst()==vehicle){
                    if(segmentEtoW.compareAndSet(0,null,vehicle)){
                        vehicle.sleep(500);
                        vehicle.setPosition(0);
                    }
                }
            }else{
                if(vehicle.getPosition()+1==sizeBridge){
                    vehicle.sleep(500);
                    segmentEtoW.set(vehicle.getPosition(), null);
                    return true;
                }
                if(segmentEtoW.compareAndSet(vehicle.getPosition()+1,null,vehicle )){
                    vehicle.sleep(500);
                    segmentEtoW.set(vehicle.getPosition(), null);
                    vehicle.setPosition(vehicle.getPosition()+1);
                }
            }
        }
        return false;
    }

    public void boatForward(Boat boat){

    }
}
