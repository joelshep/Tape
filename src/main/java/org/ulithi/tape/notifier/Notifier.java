package org.ulithi.tape.notifier;

import org.ulithi.tape.Configuration;
import org.ulithi.tape.Machine;

/**
 * Interface for receiving notifications from a {@link Machine}: to support debugging and other
 * activities that aren't part of the core Turing machine itself.
 */
public interface Notifier {

    /**
     * Invoked before the {@code Machine} begins to process the instructions related to a newly
     * selected m-configuration.
     *
     * @param config The machine configuration just selected.
     * @param head The zero-based index of the currently scanned square on the tape.
     * @param tape A copy of the machine's current tape.
     */
    default void beforeMove(Configuration config, int head, Character[] tape) {}

    /**
     * Invoked after the {@code Machine} has processed the instructions related to the current
     * m-configuration.
     *
     * @param config The machine configuration for the move just completed.
     * @param step The zero-based index of the just-executed instruction in the move.
     * @param head The zero-based index of the currently scanned square on the tape.
     * @param tape A copy of the machine's current tape.
     */
    default void afterMove(Configuration config, int step, int head, Character[] tape) {}

    /**
     * Invoked when the {@code Machine} reaches a "halt" instruction or its processing is
     * otherwise terminated.
     * @param config The machine configuration for the move just completed.
     * @param head The zero-based index of the currently scanned square on the tape.
     * @param tape A copy of the machine's current tape.
     */
    default void onHalt(Configuration config, int head, Character[] tape) {}
}
