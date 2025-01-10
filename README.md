# Tape

A simple commandline application for running and debugging Turing machine programs.

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
Tape emulates a Turing machine. Turing machines are a formal, symbolic computing system, first
described by British mathematician Alan Turing in his 1936 paper "On Computable Numbers, With an
Application to the Entscheidungsproblem". Turing machines are very simple, yet are theoretically
capable of any symbolic computation: i.e., capable of anything that any digital computer is capable
of and more. In essence, a Turing machine consists of a simple paper tape, divided into squares,
and a device with a head that can read, or "scan", the symbol on the square currently underneath it
(the "current square"), print a symbol on the current square, erase the current square, or move the
head on square left or right. That's it. Tape provides an additional "no-op" action which can
make writing programs a little easier, but I may remove it at some point.

These links have more information on Turing machines:
* https://samwho.dev/turing-machines/ - A great interactive intro to Turing machines.
* https://www.historyofinformation.com/detail.php?id=619
* https://en.wikipedia.org/wiki/Turing%27s_proof
* https://en.wikipedia.org/wiki/Alan_Turing
* https://archive.org/details/Turing1936OnCumputableNumbers/mode/2up

The Turing Machine implementation in Tape itself is small, as you might expect. Most of the
functionality in Tape is around loading and debugging programs written for the machine. Because
the language used to specify the programs is so minimal and because so many "moves" (as Turing
referred to them) are needed to carry out even simple tasks like adding 4-bit numbers, debugging
can be challenging. So Tape provides basic functionality for stepping through programs, setting
breakpoints, limiting execution times (yet to be implemented) and so on.

## Basics of The Machine
The Turing Machine implemented in Tape has a tape with a default length of 8192 "squares". A
square can hold a single 16-bit character, so the default capacity of the machine is 8192
16-bit words, or 16 KB.

The "head" of the reader -- i.e., the initial scanned square -- defaults to the square with
index 2048: about 1/4 of the length of the tape from the leftmost (zero-index) square.

The machine understands six instructions:
* R - Move the head one square to the right.
* L - Move the head one square to the left.
* P:c - Print the character represented by _c_ in the current scanned square.
* E - Erase the character in the current scanned square (set it to null).
* N - No-op - Do nothing. (May be removed in the future.)

The first five instructions are the canonical Turing machine instructions. The "N" instructions is
simply a convenience for "moves" that just change the state of the machine.

# Writing Programs
Turing described the behavior of his machines using simple tables of states, symbols, and
instructions. It wasn't really a programming language like we'd think of programming languages
today ... but then, this was 1936 and programming languages simply did not exist at that time.

Tape closely follows Turing's original table-based format, with a few additions for full-line
comments (which you will want to use to make sense of your "code"!) and setting the initial state
of the machine.

For this implementation, a program has three main sections: an initial tape configuration (required,
but can be empty), and initial state (required), and one or more "configurations", which map a
potential state of the machine (what Turing referred to as its m-configuration) to its
corresponding behavior, which is one or more of the fundamental instructions, separated by
commas, followed by the state the machine should enter after the last instruction is carried out.
The combination of instructions and ending state are referred to as a "move". Full-line comments
have a ";;" prefix, and can appear anywhere after the lines specifying the initial tape
configuration and state.

Here is an example program which prints alternating 1's and 0's on every other square:
```

b
;; A single-state program that prints alternating 1's and 0's on every other square.
;; Lifted directly from "On Computable Numbers", chapter 3.
b null P:0     b
b 0    R,R,P:1 b
b 1    R,R,P:0 b
```
The first line is empty and specifies the initial tape configuration (empty: the default). The
second line specifies the initial state is 'b'. The next two lines are comments and are ignored
by the machine (more correctly: are removed before the machine begins processing the following
configurations). The remaining lines specify the possible m-configurations and corresponding moves.
For instance, the first configuration will be selected and processed if the current state is 'b',
and the current value read from the tape is null (empty square) or any character other than the
'0' and '1' specified by the other two configurations. If that configuration is selected, the
machine will print a '0' in the current square and then enter state 'b' (a configuration can begin
and end in the same state).

On the next iteration, the machine will see that it is in state 'b' and the current value under
the head is '0', so it will select the m-configuration which will instruct it to move right two
squares and print '1', and then remain in state 'b'. The next m-configuration will be matched
causing the machine to move right two squares, print '0', and loop back to the previous
m-configuration.

See the Sample Programs package in 'resources' for some examples of working Turing Machine
programs.

## Running and Debugging Programs
When Tape starts, you'll see a bit of introductory info and then the prompt for input:
```
:
```
Enter 'h' for a short help document. The usual flow is that you enter 'l' to load the program
to run -- specify an absolute path to the file or a path relative to the current working
directory -- and then either 'r' to run the program or 'd' to run it in the debugger. In the
debugger you can step through the code move by move, set breakpoints for one or more m-configurations,
list and delete breakpoints, run code to a breakpoint or until the program halts (if it ever does).
You can also trace code execution, which simply prints the machine state instruction-by-instruction.

Enter 'q' to quit Tape, or 's' to list the program source.

The debugger offers several basic commands:
* s - Step through the program, configuration by configuration.
* b <breakpoint> - Set a breakpoint by specifying the state and current value to stop
at: e.g. "b findone:1".
* d <breakpoint> - Deletes a breakpoint.
* l - List breakpoints.
* x - Delete all breakpoints.
* t - Trace program execution.
* c - Continue to the next breakpoint or end of the program.
* g - Continue until the end of the program (exits the debugger).
* q - Quit tape.
* h - Get help (similar to this documentation).

## Notes About The Machine
Here are a few informal observations about programming a Turing machine.
* Memory in a Turing machine is strictly sequential access. This is one of the biggest
differences between a Turing machine and any practical computer: Turing machines support
sequential memory access only, practical computers support sequential and random access.
* Turing machine programs are essentially a graph of while-loops. Generally, the machine
enters a state, prints or erases a symbol, and then enters a while loop to scan left or
right, either printing a sequence, or seeking a "marker square" which will cause it to
change state.
* In Turing's "On Computable Numbers", he describes E- and F-squares. E- and F-squares alternate.
F-squares contain data being computed by the machine, while E squares hold state (as different
types of symbols) needed for bookkeeping during the computation. Eg, I could (and should!)
rewrite the "count-ones.tur" example to use F-squares to hold the sequence being processed and
the total number of 1's in the sequence, and E-squares to indicate which 1's have been counted
so far (instead of overwriting each 1 with x as the program does today). This would make the
program non-destructive: the original input sequence would be preserved.
* Forget performance. Pretty much any operation of interest is O(N^2) or worse. If you're not
familiar with Big-O notation, this basically means that doubling the amount of data you're
working with will result in a 4x (2-squared) increase in the number of operations. A program
that takes 8 operations to print two squares, will take roughly 32 operations to print 4
squares, or 72 operations to print 6 squares (3x increase in squares => 3-squared increase
in operations). Again, practical computers use random access memory which is essentially
means that finding any memory location is O(1), not O(n) as it is in a Turing machine.
