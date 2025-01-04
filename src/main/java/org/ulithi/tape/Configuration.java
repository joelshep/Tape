package org.ulithi.tape;

/**
 * "Struct" that specifies a potential "move" of the machine. If the current state and scanned value
 * of the machine matches {@code inState} and {@code scanned} symbol, then the specified operations
 * are performed, potentially writing the corresponding symbols to the tape, and finally the
 * machine transitions to {@code outState}.
 */
public class Configuration {
    /** Expected machine state at start of configuration. */
    public String inState;

    /** Expected symbol in the current scanned square. */
    public Character scanned;

    /** List of operations to perform if this configuration is selected. */
    public char[] operations;

    /** Symbols for P operations at corresponding index positions in the operations array. */
    public Character[] symbols;

    /** Machine state at the end of configuration. */
    public String outState;
}
