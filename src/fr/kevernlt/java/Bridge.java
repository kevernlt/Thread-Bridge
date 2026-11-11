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
    public void printCurrentState() {
        clearOutput();
        System.out.println("Current bridge state: " + state);

        System.out.println("Cars segments:");

        for (int i = 0; i < sizeBridge; i++) {
            int oppositeIndex = sizeBridge - 1 - i;

            // Lecture thread-safe des cases du tableau
            Vehicle vWtoE = segmentWtoE.get(i);
            Vehicle vEtoW = segmentEtoW.get(oppositeIndex);

            String firstColor = resetColor();
            String secondColor = resetColor();

            String firstStatus = (vWtoE != null) ? "Car " + vWtoE.hashCode() : "Empty";
            String secondStatus = (vEtoW != null) ? "Car " + vEtoW.hashCode() : "Empty";

            if (vWtoE != null) {
                firstColor = getRandomAnsiColorCodeFromStrings(String.valueOf(vWtoE.hashCode()));
            }
            if (vEtoW != null) {
                secondColor = getRandomAnsiColorCodeFromStrings(String.valueOf(vEtoW.hashCode()));
            }

            System.out.println("  -> " +
                    i +
                    firstColor +
                    " [" + firstStatus + "]        " +
                    resetColor() +
                    oppositeIndex +
                    secondColor +
                    "  [" + secondStatus + "]" +
                    resetColor()
            );
        }

        System.out.println("Vehicles at bridge entry:");
        System.out.println("  -> WEST to EAST entry: " + vehicleWtoE.size() + " vehicle(s) waiting");
        System.out.println("  -> EAST to WEST entry: " + vehicleEtoW.size() + " vehicle(s) waiting");
        System.out.println("-----------------------------------");
    }


    private void clearOutput() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private String resetColor() {
        return "\u001B[0m";
    }

    private String getRandomAnsiColorCodeFromStrings(String seed) {
        int hash = Math.abs(seed.hashCode());
        int colorCode = 31 + (hash % 6);
        return "\u001B[" + colorCode + "m";
    }

}
