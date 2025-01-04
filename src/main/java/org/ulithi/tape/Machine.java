package org.ulithi.tape;

import java.io.IOException;

/**
 * An implementation of a Turing machine. Turing machines are really very simple. Most of the
 * code here is to handle pragmatic real-world concerns, like the fact that memory is finite --
 * unlike a true Turing machine where the tape is of infinite length -- it's not helpful to
 * display thousands of empty squares when printing the tape, and debugging. The main complexity
 * in the machine itself is selecting the next m-configuration to process.
 */
public class Machine {

    /**
     * The default length of the tape, in squares.
     */
    public static final int DEFAULT_TAPE_LENGTH = 8*1024;  // Bytes, effectively.

    /**
     * The tape itself.
     */
    private final Character[] tape;

    /**
     * The position of the "head"; or, the zero-based index of the currently scanned square.
     */
    private int head;

    /** Indicates if the machine has been halted. */
    private boolean halted = false;

    /** Indicates if the machine is running in debug mode. */
    private boolean debug = false;

    /**
     * windowMin and windowMax are for display purposes only. They are the minimum and maximum
     * indices of the squares to be displayed when the tape is displayed.
     */
    private int windowMin;
    private int windowMax;

    /**
     * Initializes an instance of this machine with the default tape length.
     */
    public Machine() {
        this(DEFAULT_TAPE_LENGTH);
    }

    /**
     * Initializes an instance of this machine with a tape of the specified length.
     * @param length Length of the machine tape, in squares.
     */
    public Machine(final int length) {
        tape = new Character[length];
        head = tape.length / 4;
        windowMin = Math.max(0, head - 2);
        windowMax = Math.min(tape.length, head + 4);
    }

    /**
     * Enables or disables debug mode.
     * @param debug If true, enables debug mode; if false, disables it.
     */
    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    /**
     * Runs the given {@link Program}, by iteratively finding the current m-configuration,
     * processing the related instructions and transitioning to a new state and m-configuration.
     *
     * @param program The {@code Program} to run.
     */
    public void run(final Program program) {

        initTape(program.initialSeq, tape);
        String state = program.initialState;

        if (program.initialHead >= 0) {
            head = program.initialHead;
        }

        Character scanned = tape[head];

        while (!halted) {
            Configuration config = findMConfiguration(state, scanned, program.configurations);

            if (config == null) {
                throw new RuntimeException(
                        "No configuration matching state: " + state + " and scanned: " + scanned);
            }

            for (int i = 0; i < config.operations.length; i++) {
                switch (config.operations[i]) {
                    case Program.RIGHT:
                        head++;
                        windowMax = Math.max(windowMax, head);
                        break;
                    case Program.LEFT:
                        head--;
                        windowMin = Math.min(windowMin, head);
                        break;
                    case Program.PRINT:
                        tape[head] = config.symbols[i];
                        break;
                    case Program.ERASE:
                        tape[head] = null;
                        break;
                    case Program.HALT:
                        halted = true;
                        break;
                    case Program.NOOP:
                        break;
                    default:
                        throw new RuntimeException(
                                "Unknown operation " + config.operations[i]);
                }

                scanned = tape[head];
                printState(config, i);
            }

            state = config.outState;

            if (debug) {
                System.out.println("Press Enter to continue ...");
                try {
                    System.in.read();
                } catch (IOException e) {
                    // Swallow it
                }
            }
        }

        System.out.println("Halted ...");
    }

    /**
     * Initializes the machine's tape by copying the given initial tape sequence on the machine
     * tape, beginning at the default head position (1/4 in from left end of tape).
     * @param initialSeq The symbol sequence used to initialize the machine tape.
     * @param machineTape Reference to this machine's tape.
     */
    private void initTape(final Character[] initialSeq, final Character[] machineTape) {
        if (initialSeq == null || initialSeq.length == 0) {
            return;
        }

        System.arraycopy(initialSeq, 0, machineTape, head, initialSeq.length);

        windowMax = head + initialSeq.length + 1;
    }

    /**
     * Locates the mConfiguration and behavior the machine should process next.
     * @param state The current state of the machine.
     * @param scanned The symbol in the current scanned square.
     * @param configurations A reference to the full configuration for this machine.
     * @return The selected configuration, or null if no configuration is selected (always an
     *         error). Configuration is selected by matching the given state to the configuration's
     *         state, and scanned symbol to the configuration's symbol, or {@code null} if there
     *         is no exact match for the scanned symbol.
     */
    private static Configuration findMConfiguration(final String state, final Character scanned, Configuration[] configurations) {
        Configuration nullScannedConfiguration = null;

        for (Configuration config : configurations) {
            if (config.inState.equals(state)) {
                if ((scanned == null && config.scanned == null) ||
                        (scanned != null && scanned == config.scanned)) {
                    return config;
                } else if (config.scanned == null) {
                    nullScannedConfiguration = config;
                }
            }
        }

        return nullScannedConfiguration;
    }

    /**
     * Writes the current state of the machine to STDOUT, including the m-configuration (state and
     * symbol) and head position indicator.
     * @param config The machine configuration for the move just completed.
     * @param step The zero-based index of the just-executed instruction in the move.
     */
    private void printState(final Configuration config, final int step) {
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format("%-10s", config.inState))
                .append(' ')
                .append(config.operations[step])
                .append(':')
                .append(config.symbols[step] == null ? ' ' : config.symbols[step])
                .append(' ');

        for (int i = windowMin; i <= windowMax; i++) {
            sb.append("| ").append(tape[i] == null ? ' ' : tape[i]).append(' ');
        }

        sb.append("|\n");
        sb.append(" ".repeat(15));

        for (int i = windowMin; i <= windowMax; i++) {
            sb.append(head == i ? "  ^ " : "    ");
        }

        sb.append('\n');

        System.out.print(sb);
    }
}
