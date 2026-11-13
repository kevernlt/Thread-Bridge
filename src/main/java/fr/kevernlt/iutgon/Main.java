package fr.kevernlt.iutgon;

import java.util.Scanner;

/**
 * Main application selector.
 * Prompts the user in the console to choose between CLI console mode and JavaFX GUI mode.
 */
public class Main {

    /**
     * Entry point for running the application.
     * Asks the user to choose mode and launches the corresponding launcher.
     *
     * @param args command line arguments
     * @throws Exception if an error occurs during launch
     */
    public static void main(String[] args) throws Exception {
        if (args != null && args.length > 0) {
            String arg = args[0].trim().toLowerCase();
            if (arg.equals("1") || arg.contains("cli")) {
                CliLauncher.main(args);
                return;
            } else if (arg.equals("2") || arg.contains("jfx") || arg.contains("gui") || arg.contains("javafx")) {
                JfxLauncher.main(args);
                return;
            }
        }

        System.out.println("=========================================");
        System.out.println("        Thread Bridge Simulator          ");
        System.out.println("=========================================");
        System.out.println("Select execution mode:");
        System.out.println("[1] CLI (Console Top-Down View)");
        System.out.println("[2] JavaFX (Graphical Interface)");
        System.out.print("Your choice (1 or 2): ");

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String choice = scanner.nextLine().trim();
            if (choice.equals("1") || choice.equalsIgnoreCase("cli")) {
                System.out.println("Starting CLI mode...\n");
                CliLauncher.main(args);
                return;
            } else if (choice.equals("2") || choice.equalsIgnoreCase("javafx") || choice.equalsIgnoreCase("jfx")) {
                System.out.println("Starting JavaFX mode...\n");
                JfxLauncher.main(args);
                return;
            } else {
                System.out.print("Invalid choice. Please enter 1 (CLI) or 2 (JavaFX): ");
            }
        }
    }
}
