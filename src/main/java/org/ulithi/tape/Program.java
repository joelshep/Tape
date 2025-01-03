package org.ulithi.tape;

/**
 * Represents a parsed program for the {@code Machine} to process. Specifies the initial state
 * of the tape, if any, the initial program state, and the full program configuration and
 * behavior.
 */
public class Program {

    // Instruction constants
    public static final char LEFT = 'L';
    public static final char RIGHT = 'R';
    public static final char PRINT = 'P';
    public static final char ERASE = 'E';
    public static final char HALT = 'H';
    public static final char NOOP = 'N';


    public Character[] initialTape;

    public String initialState;

    public int initialHead = -1;

    /** Table of states and values to configuration. */
    public Configuration[] configurations;
}
