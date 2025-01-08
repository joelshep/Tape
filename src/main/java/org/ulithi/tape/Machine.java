package org.ulithi.tape;

import org.ulithi.tape.notifier.Notifier;

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

    /**
     * Notifier, for debugging and displaying machine state at intermediate points during
     * processing.
     */
    private Notifier notifier = new Notifier() { };

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
    }

    /**
     * Sets a {@link Notifier} to support debugging, etc., for this machine
     * @param notifier The {@code Notifier} for this machine to publish notifications to
     *                 during processing.
     */
    public void setNotifier(final Notifier notifier) {
        this.notifier = notifier;
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
        Configuration config = null;

        while (!halted) {
            config = findMConfiguration(state, scanned, program.configurations);
            notifier.beforeMove(config, head, tape.clone());

            if (config == null) {
                throw new RuntimeException(
                        "No configuration matching state: " + state + " and scanned: " + scanned);
            }

            for (int i = 0; i < config.operations.length; i++) {
                switch (config.operations[i]) {
                    case Program.RIGHT:
                        head++;
                        break;
                    case Program.LEFT:
                        head--;
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
                notifier.afterMove(config, i, head, tape.clone());
            }

            state = config.outState;
        }

        notifier.onHalt(config, head, tape.clone());
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
}
