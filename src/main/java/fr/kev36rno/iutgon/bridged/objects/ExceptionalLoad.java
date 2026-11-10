package fr.kev36rno.iutgon.bridged.objects;

import fr.kev36rno.iutgon.bridged.WheeledVehicle;
import fr.kev36rno.iutgon.bridged.enums.Way;

public class ExceptionalLoad extends WheeledVehicle {
    public ExceptionalLoad(Way way) {
        super(way);
    }

    @Override
    public int getSize() {
        return 4;
    }

    @Override
    public boolean isBlockingOtherWay() {
        return true;
    }
}
