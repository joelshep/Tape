package org.ulithi.tape;

/**
 * Specifies a potential "move" of the machine. If the current state and scanned value of the
 * machine matches {@code inState} and {@code scanned}, then the specified operations are
 * performed, potentially writing the corresponding symbols to the tape, and finally the
 * machine transitions to {@code outState}.
 */
public class Configuration {
    public String inState;      // Expected machine state at start of configuration.
    public Character scanned;   // Expected symbol in the current scanned square.
    public char[] operations;   // List of operations to perform if this configuration is selected
    public Character[] symbols; // Symbols to print for P operations.
    public String outState;     // Machine state at the end of configuration.
}
