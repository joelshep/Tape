package org.ulithi.tape;

import org.ulithi.tape.notifier.DebugNotifier;
import org.ulithi.tape.notifier.StepNotifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * Implements a simple REPL (read-evaluate-print-loop) for loading, running and debugging Turing
 * machine "programs".
 */
public class Main {

    /** Constants for readability: true->debug mode, false->run mode. */
    private static final boolean RUN_MODE = false;
    private static final boolean DEBUG_MODE = true;

    /**
     * Reads and parses the specified program, and passes it to the machine for execution.
     * @param args Commandline arguments (none supported currently).
     */
    public static void main(String[] args) {
        final Scanner scanner = new Scanner(System.in);
        String source = null;

        System.out.println("Tape v0.1: A Turing Machine");
        System.out.println("Enter a command, or 'h' for help ...");
        System.out.println("Commands: (l)oad (r)un (d)ebug (s)ource (q)uit (h)elp");

        while (true) {
            System.out.print(": ");
            final String input = scanner.nextLine().trim();

            if (input.isBlank()) {
                continue;
            }

            switch (input.toLowerCase().charAt(0)) {
                case 'l':
                    source = loadProgram(scanner);
                    break;
                case 's':
                    displaySource(source);
                    break;
                case 'r':
                    parseAndRunProgram(scanner, source, RUN_MODE);
                    break;
                case 'd':
                    parseAndRunProgram(scanner, source, DEBUG_MODE);
                    break;
                case 'q':
                    scanner.close();
                    System.out.println("Exiting ...");
                    return;
                case 'h':
                    help();
                    break;
                default:
                    System.err.println("Unknown REPL command: type 'h' for help.");
            }
        }
    }

    /**
     * Prompts the user for a path to the "program"/configuration file for the machine, reads
     * it and returns it as a String.
     * @param scanner Scanner used to handle user response to file pathname prompt.
     * @return The user's selected file contents as a String, or null if the file can't be read.
     */
    private static String loadProgram(final Scanner scanner) {
        final String cwd = System.getProperty("user.dir");
        System.out.println("  Enter file path (" + cwd + "): ");
        System.out.print("  ");
        final String filePath = scanner.nextLine().trim();

        try {
            File file = new File(filePath);

            if (!file.exists() && !file.isAbsolute()) {
                file = new File(cwd, filePath);
            }

            if (!file.exists() || !file.canRead()) {
                System.err.println("File " + filePath + " doesn't exist or is unreadable");
                return null;
            }

            final StringBuilder source = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    source.append(line.trim()).append('\n');
                }
            }

            System.out.println(file + " loaded ...");
            return source.toString();

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return null;
    }

    /**
     * Writes the loaded machine "program"/configuration to STDOUT.
     * @param source The currently loaded machine program/configuration.
     */
    private static void displaySource(final String source) {
        System.out.println("Currently loaded source:");
        System.out.println(source);
    }

    /**
     * Parses the loaded program and initiates machine-processing of it.
     * @param scanner Scanner for reading user input from STDIN: needed if running the
     *                program in the debugger.
     * @param source The currently loaded machine program/configuration.
     * @param mode Indicates if the machine will run in debug mode (true) or normal execution
     *             mode (false).
     */
    private static void parseAndRunProgram(
            final Scanner scanner, final String source, final boolean mode) {
        if (source == null || source.isBlank()) {
            System.err.println("No source loaded: use 'l' option to load a program");
            return;
        }

        Program program = Parser.parseSource(source);

        Machine machine = new Machine();

        if (mode == DEBUG_MODE) {
            machine.setNotifier(new DebugNotifier(scanner));
        } else {
            machine.setNotifier(new StepNotifier());
        }

        machine.run(program);
    }

    /**
     * Prints a short help message describing the REPL commands.
     */
    private static void help() {
        final String message =
                """
                  Tape v0.1: A Turing Machine
                  Commands:
                    'l' - Load a program. You'll be prompted for the path and file name of
                          the program you wish to load.
                    's' - Display the program source code.
                    'r' - Run the program.
                    'd' - Start the debugger.
                    'q' - Quit.
                    'h' - Print this message.
                """;

        System.out.println(message);
    }
}
