package fr.kev36rno.iutgon.bridged.objects;

import fr.kev36rno.iutgon.bridged.WheeledVehicle;
import fr.kev36rno.iutgon.bridged.enums.Way;

public class Truck extends WheeledVehicle {
    public Truck(Way way) {
        super(way);
    }

    @Override
    public int getSize() {
        return 2;
    }
}
