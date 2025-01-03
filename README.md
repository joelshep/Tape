# Tape

A simple commandline application for running and debugging Turing Machine programs.

**NOTE**: This project is decidedly prototype quality as of January 2025. No warranty expressed
or implied!

## Quick Start

Tape is a standard Maven-based project, with minimal dependencies. As supplied, it compiles
with JDK-17 but will probably compile with older versions as well.

A simple REPL is included with the project, in the ```Main``` class, so you can run and
interact with tool on the commandline.

To build the jar and run the unit test suite:

```mvn clean package```

To build an executable jar:

```mvn clean package shade:shade```

To run the Tape REPL from the executable jar:

```java -jar target/org-ulithi-tape-0.1-SNAPSHOT.jar```

## Introduction
Tape emulates a Turing Machine. The Turing Machine implementation itself is small, as you
might expect. Most of the functionality in Tape is around loading and debugging "programs"
written for the machine. Because the "language" is so minimal and because so many "moves" are
needed to carry out even simple tasks like adding 4-bit numbers, debugging a more sophisticated
program can be challenging.

## Basics of The Machine
The Turing Machine implemented in Tape has a tape with a default length of 8192 "squares". A
square can hold a single 16-bit character, so the default capacity of the machine is 8192
16-bit words, or 16 KB.

The "head" of the reader -- i.e., the initial scanned square -- defaults to the middle of the
tape: the square with index 4096.

The machine understands six instructions:
* R - Move the head one square to the right.
* L - Move the head one square to the left.
* P:c - Print the character represented by _c_ in the current scanned square.
* E - Erase the character in the current scanned square (set it to null).
* N - No-op - Do nothing.

The first five commands are the canonical Turing machine instructions. The "N" command is simply
a convenience for "moves" that just change the state of the machine.

# Writing Programs
For this implementation, a program has three sections: an initial tape configuration (required,
but can be empty), and initial state (required), and one or more "configurations", which map a
potential state of the machine (what Turing referred to as its m-configuration) to its
corresponding behavior, which is one or more of the fundamental instructions, followed by
the state the machine should enter after the last instruction is carried out. The combination
of instructions and ending state are referred to as a "move". Here is an example program
which prints alternating 1's and 0's on every other square:
```

b
b null P:0     b
b 0    R R P:1 b
b 1    R R P:0 b
```
The initial empty line specifies the initial tape configuration (empty: the default). The
second line specifies the initial state is 'b'. The remaining lines specify the possible
m-configurations and corresponding moves. For instance, the first configuration will be
selected and processed if the current state is 'b', and the current value read from the tape is
null (empty square) or any character other than the '0' and '1' specified by the other two
configurations. If that configuration is selected, the machine will print a '0' in the current
square and then enter state 'b' (a configuration can begin and end in the same state).

On the next iteration, the machine will see that it is in state 'b' and the current value
under the head is '0', so it will select the second instruction which will instruct it to
move right two squares and print '1', and then remain in state 'b'. The final instruction
will be matched caused the machine to move right two squares and print '0', causing it to
loop back to the previous instruction.

See the Sample Programs package in 'resources' for some examples of working Turing Machine
programs.

## Running and Debugging Programs
When Tape starts, it'll prompt you for a program to run. Once you load the program, you have
several options, selected by entering the corresponding character and pressing ENTER:
* r - Run the program.
* d - Run the program in the debugger.
* l - Load a different program.
* q - Quit tape.
* h - Get help (similar to this documentation).

The debugger offers several basic commands:
* s - Step through the program, configuration by configuration.
* b <breakpoint> - Set a breakpoint by specifying the state and current value to stop
at: e.g. "findone 1".
* c - Continue to the next breakpoint or end of the program.
* g - Continue until the end of the program (exits the debugger).
* q - Quit tape.
* h - Get help (similar to this documentation).

## Notes About The Machine
Here are a few informal observations about programming a Turing Machine.
* Memory in a Turing Machine is strictly sequential access. This is one of the biggest
differences between a Turing Machine and any practical computer: Turing Machines support
sequential memory access only, practical computers support sequential and random access.
* Turing Machine programs are essentially a graph of while-loops. Generally, the machine
enters a state, prints or erases a symbol, and then enters a while loop to scan left or
right, either printing a sequence, or seeking a "marker square" which will cause it to
change state.
* Forget performance. Pretty much everything of interest is O(N^2) or worse. If you're not
familiar with Big-O notation, this basically means that doubling the amount of data you're
working with will result in a 4x (2-squared) increase in the number of operations. A program
that takes 8 operations to print two squares, will take roughly 32 operations to print 4
squares, or 72 operations to print 6 squares (3x increase in squares => 3-squared increase
in operations).
