package org.ulithi.tape;

/**
 * Utility class to parse source code specifying machine configurations (a.k.a., a "program") so
 * that they can be processed by a {@link Machine}.
 * <p>
 * A program has three parts. The first line specifies the initial state of the tape as space-
 * separated symbols. If the tape is initially completely empty, the first line should be empty
 * as well.
 * <p>
 * The second line specifies the initial state of the machine and, optionally, the initial
 * "head position" -- the zero-based index of the initial scanned sqaure -- as a comma-separated
 * string. E.g.<ul>
 *     <li>init,2020 - Specifies an initial state of 'init' and initial scanned square index
 *     of 2020.</li>
 *     <li>init, - Specifies an initial state of 'init' and the default scanned square index (2048).</li>
 *     <li>init - Same as "init,".</li>
 * </ul>
 * The initial state must be specified, and must be a valid state in the configuration.
 * <p>
 * The remaining lines are the list of machine configurations. Each configuration consists of a
 * state and a symbol (or {@code null} to match the empty scanned symbol and also to serve as
 * the default symbol. Each symbol is followed by one or more comma-separated instructions and a
 * new state to transition to. The {@code Machine} implements five instructions: move left, move
 * right, print, erase and halt. It also currently implements a "no-op" instruction: this isn't
 * part of the formal Turing machine description, but it can be useful for allowing simple
 * state transitions. It may be removed in future versions.
 */
public class Parser {

    /** Config row indices, for readability. */
    private static final int INITIAL_STATE = 0;
    private static final int SCANNED = 1;
    private static final int OPERATIONS = 2;
    private static final int FINAL_STATE = 3;

    /** Zero-based index of the first line of configuration expected in the source text. */
    private static final int CONFIGURATION_INDEX = 2;

    /**
     * Parses the given source and returns a {@link Program} specifying the initial state and
     * configurations for a Turing machine.
     * @param source Source code for the machine initial state and configurations.
     * @return A {@link Program} representing the parsed source, to be used as input to a
     *         {@link Machine}.
     */
    public static Program parseSource(final String source) {
        final Program program = new Program();

        final String[] lines = source.split("\n");

        // Parse the initial sequence of symbols for the tape, if specified.
        final String initialSeq = lines[0].trim();

        if (!initialSeq.isBlank()) {
            program.initialSeq = new Character[(initialSeq.length() + 1) / 2];

            for (int i = 0; i < initialSeq.length(); i++) {
                char chr = initialSeq.charAt(i++);
                program.initialSeq[i / 2] = chr == ' ' ? null : chr;
            }
        }

        // Parse the initial state (required) and head position (optional).
        String[] initialState = lines[1].trim().split(",");

        if (initialState.length < 1 || initialState.length > 2) {
            throw new RuntimeException("Illegal initial state specification: " + initialState);
        }

        program.initialState = initialState[0].trim();

        if (!(initialState.length == 1 || initialState[1].isBlank())) {
            program.initialHead = Integer.parseInt(initialState[1]);
        }

        // Prepare to parse the machine configurations.
        program.configurations = new Configuration[lines.length - CONFIGURATION_INDEX];

        int configCount = 0;

        for (int i = CONFIGURATION_INDEX; i < lines.length; i++) {
            final String[] columns = lines[i].trim().split("\\s+");

            // If the line is blank or a full line comment, skip it.
            if (columns.length == 0 || columns[0].startsWith(";;")) {
                continue;
            }

            Configuration config = new Configuration();

            config.inState = columns[INITIAL_STATE];
            config.scanned = "null".equals(columns[SCANNED]) ? null : columns[SCANNED].charAt(0);

            // Parse operations and related symbols.
            final String[] operations = columns[OPERATIONS].split(",");

            config.operations = new char[operations.length];
            config.symbols = new Character[operations.length];

            for (int j = 0; j < operations.length; j++) {
                String operation = operations[j];
                if (operation.contains(":")) {
                    String[] opParts = operation.split(":");
                    config.operations[j] = opParts[0].charAt(0);
                    config.symbols[j] = opParts[1].charAt(0);
                } else {
                    config.operations[j] = operation.charAt(0);
                    config.symbols[j] = null;
                }
            }

            config.outState = columns[FINAL_STATE];

            program.configurations[configCount++] = config;
        }

        return program;
    }
}
