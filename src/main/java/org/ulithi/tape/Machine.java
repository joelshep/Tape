package org.ulithi.tape;

import java.io.IOException;

/**
 * A somewhat bare-bones implementation of a Turing machine.
 */
public class Machine {

    private static final int DEFAULT_TAPE_LENGTH = 8*1024;  // Bytes, effectively.

    private final Character[] tape;
    private int head;
    private boolean halt = false;
    private boolean pause = false;

    // For display purposes: not needed for the "machine" itself.
    private int windowMin;
    private int windowMax;

    public Machine() {
        this(DEFAULT_TAPE_LENGTH);
    }

    public Machine(final int length) {
        tape = new Character[length];
        head = tape.length / 2;
        windowMin = Math.max(0, head - 2);
        windowMax = Math.min(tape.length, head + 4);
    }

    public void setPause(final boolean pause) {
        this.pause = pause;
    }

    public void run(final Program program) {

        initTape(program.initialTape, tape);
        String state = program.initialState;

        if (program.initialHead >= 0) {
            head = program.initialHead;
        }

        Character scanned = tape[head];

        while (!halt) {
            Configuration config = findNextInstruction(state, scanned, program.configurations);

            if (config == null) {
                throw new RuntimeException(
                        "No configuration matching state: " + state + " scanned: " + scanned);
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
                        halt = true;
                        break;
                    case Program.NOOP:
                        break;
                    default:
                        throw new RuntimeException(
                                "Unknown operation " + config.operations[i]);
                }
                scanned = tape[head];
                printState(config.inState + " " + config.operations[i] + ":" + (config.symbols[i] == null ? "" : config.symbols[i]));
            }
            state = config.outState;

            if (pause) {
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

    private void initTape(final Character[] initialTape, final Character[] programTape) {
        if (initialTape == null || initialTape.length == 0) {
            return;
        }

        final int offset = initialTape.length / 2;
        final int midpoint = programTape.length / 2;

        System.arraycopy(initialTape, 0, programTape, midpoint - offset, initialTape.length);

        windowMin = midpoint - offset - 1;
        windowMax = midpoint + offset + 1;
    }

    private static Configuration findNextInstruction(final String state, final Character scanned, Configuration[] instructions) {
        // First try to find exact match for state and value
        Configuration nullScannedInstruction = null;

        for (Configuration inst : instructions) {
            if (inst.inState.equals(state)) {
                if ((scanned == null && inst.scanned == null) ||
                        (scanned != null && scanned == inst.scanned)) {
                    return inst;
                } else if (inst.scanned == null) {
                    nullScannedInstruction = inst;
                }
            }
        }

        return nullScannedInstruction;
    }

    private void printState(final String instruction) {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-14s ", instruction));

        for (int i = windowMin; i <= windowMax; i++) {
            sb.append("| ").append(tape[i] == null ? " " : tape[i]).append(" ");
        }

        sb.append("|\n");
        sb.append("               ");

        for (int i = windowMin; i <= windowMax; i++) {
            sb.append(head == i ? "  ^ " : "    ");
        }

        sb.append(" \n");

        System.out.print(sb);
    }
}
