package org.ulithi.tape;

import sun.misc.Signal;

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

    private static final String ALTERNATE_1 =
            """

                    b
                    b null P:0 R c
                    c null R d
                    d null P:1 R e
                    e null R b
                    """;

    private static final String ALTERNATE_2 =
            """

                    b
                    b null P:0 b
                    b 0 R R P:1 b
                    b 1 R R P:0 b
                    """;

    private static final String SEQUENCE_1 =
            """

                    init
                    init    null P:e R P:e R P:0 R R P:0 L L mark
                    mark    1    R P:x L L L                 mark
                    mark    0    N                           scan-p1
                    scan-p1 null P:1 L                       return
                    scan-p1 0    R R                         scan-p1
                    scan-p1 1    R R                         scan-p1
                    scan-p1 e    R R                         scan-p1
                    scan-p1 x    R R                         scan-p1
                    return  x    E R                         scan-p1
                    return  e    R                           scan-p0
                    return  null L L                         return
                    scan-p0 null P:0 L L                     mark
                    scan-p0 0    R R                         scan-p0
                    scan-p0 1    R R                         scan-p0
                    scan-p0 e    R R                         scan-p0
                    scan-p0 x    R R                         scan-p0
                    """;

    /**
     * Starting from 1, repeatedly multiplies by 2. Note that the position of the lower
     * order (1's) digit never changes. What is 2^4096? Big. This is a rare example of an
     * O(N) program: the runtime increases linearly with the number of digits printed.
     */
    private static final String DOUBLER =
                """

                    a
                    a null P:1 b
                    b 1 P:0 L P:1 b
                """;

    /**
     * Reads and parses the specified program, and passes it to the machine for execution.
     * @param args
     */
    public static void main(String[] args) {
        final Main main = new Main();
        final Scanner scanner = new Scanner(System.in);
        String source = null;

        System.out.println("Tape v0.1: A Turing Machine");
        System.out.println("Enter a command, or 'h' for help ...");

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
                    main.displaySource(source);
                    break;
                case 'r':
                    main.runProgram(source);
                    break;
                case 'd':
                    main.debugProgram(source);
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
                    source.append(line.trim()).append("\n");
                }
            }

            System.out.println(file + " loaded ...");
            return source.toString();

        } catch (IOException e) {
            System.err.println("Error loading file: " + e.getMessage());
        }

        return null;
    }

    private void displaySource(final String source) {
        System.out.println("Currently loaded source:");
        System.out.println(source);
    }

    private void runProgram(final String source) {
        if (source == null || source.isBlank()) {
            System.err.println("No source loaded: use 'l' option to load a program");
            return;
        }

        //Program program = reader.parseProgram(ADD);
        //Program program = reader.parseProgram(ALTERNATE_2);
        //Program program = reader.parseProgram(SEQUENCE_1);
        //Program program = Reader.parseProgram(COUNT);
        //Program program = Reader.parseProgram(DOUBLER);
        //Program program = Reader.parseProgram(COUNT_ONES);
        Program program = Reader.parseSource(source);
        runProgram(program, false);
    }

    private void debugProgram(final String source) {
        if (source == null || source.isBlank()) {
            System.err.println("No source loaded: use 'l' option to load a program");
            return;
        }

        Program program = Reader.parseSource(source);
        runProgram(program, true);
    }

    private void runProgram(final Program program, final boolean debug) {
        Machine machine = new Machine();
        machine.setPause(debug);
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
