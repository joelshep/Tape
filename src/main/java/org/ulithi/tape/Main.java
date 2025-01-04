package org.ulithi.tape;

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
                    parseAndRunProgram(source, false);
                    break;
                case 'd':
                    parseAndRunProgram(source, true);
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

    private static void displaySource(final String source) {
        System.out.println("Currently loaded source:");
        System.out.println(source);
    }

    private static void parseAndRunProgram(final String source, final boolean debug) {
        if (source == null || source.isBlank()) {
            System.err.println("No source loaded: use 'l' option to load a program");
            return;
        }

        Program program = Reader.parseSource(source);

        Machine machine = new Machine();
        machine.setDebug(debug);
        machine.run(program);
    }

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
