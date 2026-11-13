package fr.kevernlt.iutgon.bridged.exceptions;

/**
 * Exception thrown when a vehicle attempts to enter a way incompatible with its type.
 */
public class IllegalVehicleWay extends RuntimeException {
    /**
     * Creates the exception with an error message.
     *
     * @param message description of the illegal way assignment
     */
    public IllegalVehicleWay(String message) {
        super(message);
    }
}
