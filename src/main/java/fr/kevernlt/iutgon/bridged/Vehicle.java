package fr.kevernlt.iutgon.bridged;

import fr.kevernlt.iutgon.bridged.enums.Way;

public class Vehicle extends Thread {

    private Way way;

    public Vehicle(Way way) {
        this.way = way;
    }

    public Way getWay() {
        return this.way;
    }
}
