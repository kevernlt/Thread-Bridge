package fr.kev36rno.iutgon.bridged.objects;

import fr.kev36rno.iutgon.bridged.WheeledVehicle;
import fr.kev36rno.iutgon.bridged.enums.Way;

public class Car extends WheeledVehicle {
    public Car(Way way) {
        super(way);
    }

    @Override
    public int getSize() {
        return 1;
    }
}
