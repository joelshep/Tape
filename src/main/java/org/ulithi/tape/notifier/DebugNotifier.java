package org.ulithi.tape.notifier;

import org.ulithi.tape.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * Implementation of {@link Notifier} that provides basic debugging capabilities, such as setting
 * breakpoints, stepping through "program execution", and so on.
 */
public class DebugNotifier extends StepNotifier {

    /** Scanner for processing user input from the prompt. Shared with the Main REPL. */
    private final Scanner scanner;

    /** Indicates if this is first user prompt in the debugger REPL. */
    private boolean entryPrompt = true;

    /**
     * Enumerates the different execution modes of the debugger: stepping through code,
     * running/executing until a breakpoint is reached or the program halts, or running
     * until the program halts (or in all cases, until terminated).
     */
    private enum Mode {
        NONE,              // Initial mode
        STEP,              // Stepping through code
        RUN_TO_BREAKPOINT, // Execute until hitting breakpoint, halted or termination
        RUN_TO_HALT,       // Execute until halted or termination.
        TRACE              // Execute until halted or terminated, outputting state at each move.
    }

    /** Current execution mode of the debugger. */
    private Mode mode = Mode.NONE;

    /**
     * Maps machine states to a list of symbols identifying breakpoints set for that state.
     */
    private final Map<String, List<Character>> breakpoints = new HashMap<>();

    /**
     * Utility class for extracting breakpoint state and symbol specifications from user input.
     * The expected input format is "b|d state[:symbol]" .
     */
    private static class Breakpoint {
        public Breakpoint(final String input) {
            final String[] command = input.trim().toLowerCase().split("\\s+");

            if (command.length < 2) {
                System.out.println("Error: syntax for a breakpoint is \"b|d <state>[:<symbol>]\"");
                return;
            }

            final String[] args = command[1].split(":");

            state = args[0].trim();
            symbol = args.length > 1 ? args[1].trim().charAt(0) : null;
        }

        public String state;
        public Character symbol;
    }

    /**
     * Constructs a new {@link DebugNotifier} which accepts input through the given {@link Scanner}.
     * @param scanner An initialized {@code Scanner} to provide user input.
     */
    public DebugNotifier(final Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This is the main REPL loop for the debugger. The user can set breakpoints, step through
     * the execution of the program, let it run until it hits a breakpoint, or let it run until
     * it halts (or is terminated).
     */
    @Override
    public void beforeMove(Configuration config, int head, Character[] tape) {
        if (mode == Mode.RUN_TO_HALT) { return; }

        if (mode == Mode.RUN_TO_BREAKPOINT || mode == Mode.TRACE) {
            if (!atBreakpoint(config)) {
                return;
            }

            super.afterMove(config, 0, head, tape);
        }

        if (entryPrompt) {
            System.out.println("Entering debugger ...");
            System.out.println("Debug commands: (s)tep (b)reakpoint (c)ontinue (t)race (q)uit (h)elp");
            entryPrompt = false;
        }

        while (true) {
            System.out.print(":>> ");
            final String input = scanner.nextLine().trim();

            if (input.isBlank() && mode == Mode.STEP) {
                return;
            } else if (input.isBlank()) {
                continue;
            }

            switch (input.toLowerCase().charAt(0)) {
                case 's':
                    mode = Mode.STEP;
                    return;
                case 'b':
                    setBreakpoint(input);
                    mode = Mode.RUN_TO_BREAKPOINT;
                    continue;
                case 'd':
                    deleteBreakpoint(input);
                    continue;
                case 'l':
                    listBreakpoints();
                    continue;
                case 'x':
                    breakpoints.clear();
                    continue;
                case 'c':
                    mode = Mode.RUN_TO_BREAKPOINT;
                    return;
                case 'g':
                    mode = Mode.RUN_TO_HALT;
                    return;
                case 't':
                    mode = Mode.TRACE;
                    return;
                case 'q':
                    // Quit debugger?
                    System.exit(0); // TODO
                    return;
                case 'h':
                    help();
                    break;
                default:
                    System.err.println("Unknown debugger command: type 'h' for help.");
            }
        }
    }

    /**
     * Parses the given input -- a complete breakpoint command -- and sets up the breakpoint
     * for the debugger.
     *
     * @param input A breakpoint command. The expected string format is "b state[:symbol]" .
     */
    private void setBreakpoint(final String input) {
        final Breakpoint bp = new Breakpoint(input);
        final List<Character> symbols = breakpoints.computeIfAbsent(bp.state, k->new ArrayList<>());

        if (!symbols.contains(bp.symbol)) {
            symbols.add(bp.symbol);
        }
    }

    /**
     * Deletes the breakpoint specified by the command input.
     *
     * @param input A breakpoint delete command ('d').
     */
    private void deleteBreakpoint(final String input) {
        final Breakpoint bp = new Breakpoint(input);
        final List<Character> symbols = breakpoints.get(bp.state);

        if (symbols != null) {
            symbols.remove(bp.symbol);
        }
    }

    private void listBreakpoints() {
        final StringBuilder sb = new StringBuilder();

        for (Entry<String, List<Character>> bpList: breakpoints.entrySet()) {
            final String state = bpList.getKey();
            List<Character> symbols = bpList.getValue();

            symbols.stream().sorted().forEach(s -> sb.append(String.format("  %s:%s\n",state, s)));
        }

        System.out.print(sb.isEmpty() ? "\n" : sb);
    }

    /**
     * Indicates if program execution has reached a configured breakpoint.
     *
     * @param config The m-configuration about to be processed by the machine.
     * @return True if a breakpoint has been configured for the given m-configuration,
     *         false otherwise.
     */
    private boolean atBreakpoint(final Configuration config) {
        if (!breakpoints.containsKey(config.inState)) { return false; }

        final List<Character> bps = breakpoints.get(config.inState);

        for (Character bp: bps) {
            if (bp == config.scanned) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the user is stepping through code, pretty-print the current state and tape window of
     * interest.
     */
    @Override
    public void afterMove(Configuration config, int step, int head, Character[] tape) {
        if (mode == Mode.STEP || mode == Mode.TRACE ) {
            super.afterMove(config, step, head, tape);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * If running until halted, pretty-print the final state of the machine.
     */
    @Override
    public void onHalt(Configuration config, int head, Character[] tape) {
        if (mode != Mode.STEP) {
            super.afterMove(config, config.operations.length - 1, head, tape);
        }
        super.onHalt(config, head, tape);
    }

    /**
     * Prints a short help message describing debugger commands.
     */
    private static void help() {
        final String message =
                """
                  Tape v0.1 Debugger
                  Commands:
                    's' - Step: process the next m-configuration.
                    'b state[:symbol]' - Set a breakpoint.
                    'd state[:symbol' - Delete a breakpoint.
                    'l' - List breakpoints.
                    'x' - Delete all breakpoints.
                    'c' - Continue to the next breakpoint or until the machine halts.
                    't' - Like 'c' but outputs machine state with each move.
                    'g' - Stop debugging and continue until the machine halts.
                    'q' - Quit the debugger and terminate the machine.
                    'h' - Print this message.
                """;

        System.out.println(message);
    }
}
