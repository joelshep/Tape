package org.ulithi.tape;

/**
 * "Struct" representing a parsed program for the {@code Machine} to process. Specifies the
 * initial sequence of symbols on the tape, if any, the initial program state, and the full
 * program configuration and behavior.
 */
public class Program {

    // Instruction constants
    public static final char LEFT = 'L';
    public static final char RIGHT = 'R';
    public static final char PRINT = 'P';
    public static final char ERASE = 'E';
    public static final char HALT = 'H';
    public static final char NOOP = 'N';

    /**
     * Any initial sequence of symbols to be present on the tape before the machine begins
     * processing the configurations.
     */
    public Character[] initialSeq;

    /**
     * The initial state of the machine.
     */
    public String initialState;

    /**
     * The initial position of the head (-1 means use the default position of 2048).
     */
    public int initialHead = -1;

    /**
     * Table of states and values to configuration, parsed from the program source.
     */
    public Configuration[] configurations;
}
