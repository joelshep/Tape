package org.ulithi.tape;

/**
 * Utility class to read "programs" from a file or input stream, and transform them to a format
 * that can be processed by the {@link Machine}.
 * <p>
 * A program has three parts. The first line specifies the initial state of the tape as space-
 * separated symbols. If the tape is completely empty, the first line should be empty as well.
 * <p>
 * The second line specifies the initial state of the machine and, optionally, the initial
 * "head position" -- the zero-based index of the initial scanned sqaure -- as a comma-separated
 * string. E.g.<ul>
 *     <li>init,4192 - Specifies an initial state of 'init' and initial scanned square index
 *     of 4192.</li>
 *     <li>init, - Specifies an initial state of 'init' and the default scanned square index (4096).</li>
 *     <li>init - Same as "init,".</li>
 * </ul>
 * The initial state must be specified, and must be a valid state in the configuration.
 * <p>
 * The remaining lines are the list of machine configurations. Each configuration consists of a
 * state and a symbol (or {@code null} to match the empty scanned symbol and also to serve as
 * the default symbol. Each symbols is followed by one or more* instructions and a new state to
 * transition to. The {@code Machine} implements five instructions: move left, move right, print,
 * erase and halt.
 */
public class Reader {

    /** Zero-based index of the first line of configuration expected in the source text. */
    private static final int CONFIGURATION_INDEX = 2;

    public static Program parseSource(final String source) {

        final Program program = new Program();

        final String[] lines = source.split("\n");

        final String initialTape = lines[0].trim();

        if (!initialTape.isBlank()) {
            program.initialSeq = new Character[(initialTape.length() + 1) / 2];

            for (int i = 0; i < initialTape.length(); i++) {
                char chr = initialTape.charAt(i++);
                program.initialSeq[i / 2] = chr == ' ' ? null : chr;
            }
        }

        String[] initialState = lines[1].trim().split(",");

        if (initialState.length < 1 || initialState.length > 2) {
            throw new RuntimeException("Illegal initial state specification: " + initialState);
        }

        program.initialState = initialState[0].trim();

        if (!(initialState.length == 1 || initialState[1].isBlank())) {
            program.initialHead = Integer.parseInt(initialState[1]);
        }

        program.configurations = new Configuration[lines.length - CONFIGURATION_INDEX];

        int configCount = 0;

        for (int i = CONFIGURATION_INDEX; i < lines.length; i++) {
            final String[] columns = lines[i].trim().split("\\s+");

            // If the line is blank or a full line comment, skip it.
            if (columns.length == 0 || columns[0].startsWith(";;")) {
                continue;
            }

            Configuration inst = new Configuration();

            inst.inState = columns[0];
            inst.scanned = "null".equals(columns[1]) ? null : columns[1].charAt(0);

            // Parse operations and related symbols.
            inst.operations = new char[columns.length - 3];
            inst.symbols = new Character[columns.length - 3];

            for (int j = 2; j < columns.length - 1; j++) {
                String operation = columns[j];
                if (operation.contains(":")) {
                    String[] opParts = operation.split(":");
                    inst.operations[j - 2] = opParts[0].charAt(0);
                    inst.symbols[j - 2] = opParts[1].charAt(0);
                } else {
                    inst.operations[j-2] = operation.charAt(0);
                    inst.symbols[j-2] = null;
                }
            }

            inst.outState = columns[columns.length - 1];

            program.configurations[configCount++] = inst; //i-CONFIGURATION_INDEX] = inst;
        }

        return program;
    }

    /**
     * Opens, reads and interprets the contents of the file specified by <code>fileName</code>.
     *
     * @param fileName The path and file name of the file to be opened and read.
     * @return 1 if successful, 0 otherwise.
     */
    /*
    private int loadFile (final String fileName) {
        // See if the specified file exists
        final File f = new File(fileName);

        if ( !f.exists() ) {
            System.err.println("File '" + fileName + "' not found");
            return 0;
        }

        try (final BufferedReader file = new BufferedReader((new FileReader(fileName)))) {
            // Read first line of the text
            String text = file.readLine();

            // Interpret each line of the text
            while ( text != null ) {
                if ( !offer(text) ) return 0;
                text = file.readLine();
            }

            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
*/
}
