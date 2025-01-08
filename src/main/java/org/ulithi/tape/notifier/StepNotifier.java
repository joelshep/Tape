package org.ulithi.tape.notifier;

import org.ulithi.tape.Configuration;

/**
 * {@link Notifier} implementation that simply prints the state of the machine and resulting
 * tape contents to STDOUT as each instruction is executed. This {@code Notifier} defaults to
 * outputting just a small "window" of squares of the tape: the section of the tape with data
 * (squares with symbols) plus a couple squares on either end. It attempts to expand the window
 * as needed to accommodate growing amounts of data.
 */
public class StepNotifier implements Notifier {

    /**
     * Once windowMin and windowMax are initialized, this determines how large of an increase
     * in print window size we can adjust for with each invocation of afterMove().
     */
    private static final int WINDOW_EXPANSION_STEP = 20;

    /**
     * Number of additional, empty squares printed on each end of the working section of the
     * tape (section containing at least some non-null symbols).
     */
    private static final int WINDOW_MARGIN = 1;

    /** The index of the leftmost tape square that will be printed. */
    private int windowMin = -1;

    /** The index of the rightmost tape square that will be printed. */
    private int windowMax = Integer.MAX_VALUE - WINDOW_EXPANSION_STEP;

    /**
     * {@inheritDoc}
     * <p>
     * Writes the current state of the machine to STDOUT, including the m-configuration (state and
     * symbol) and head position indicator.
     */
    @Override
    public void afterMove(final Configuration config, final int step, final int head, final Character[] tape) {
        initializePrintWindow(head, tape);

        final StringBuilder sb = new StringBuilder();

        if (step >= 0) {
            sb.append(String.format("%-10s", config.inState))
                    .append(' ')
                    .append(config.operations[step])
                    .append(':')
                    .append(config.symbols[step] == null ? ' ' : config.symbols[step])
                    .append(' ');
        } else {
            sb.append(String.format("%-15s", config.inState));
        }

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void onHalt(Configuration config, int head, Character[] tape) {
        System.out.println("Halted ...");
    }

    /**
     * Determines the leftmost and rightmost tape indices for the squares to be printed. The
     * length of the printed section of the tape (number of printed squares) may increase over
     * time. We assume that the indices, once initialized, will never change by more than a small
     * amount between prints, up to WINDOW_EXPANSION_STEP squares.
     *
     * @param head Index of the currently scanned square on the tape.
     * @param tape The "tape" of squares and symbols being processed by the machine.
     */
    private void initializePrintWindow(final int head, final Character[] tape) {
        windowMin = Math.max(0, windowMin - WINDOW_EXPANSION_STEP);
        windowMax = Math.min(tape.length - 1, windowMax + WINDOW_EXPANSION_STEP);

        for (int i = 0; i < tape.length; i++) {
            if (i == head || tape[i] != null) {
                windowMin = Math.max(0, i - WINDOW_MARGIN);
                break;
            }
        }

        for (int i = tape.length - 1; i > 0; i--) {
            if (i == head || tape[i] != null) {
                windowMax = Math.min(i + WINDOW_MARGIN, tape.length);
                break;
            }
        }
    }
}
