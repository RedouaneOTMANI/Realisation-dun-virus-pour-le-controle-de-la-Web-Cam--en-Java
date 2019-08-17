## Chess

A desktop appllication to play chess against one another or against the computer.
The application feautues a time constrained modues in PvP, seven computer
difficulty settings and two different modes: aggressive and non-aggressive.

Implementation-wise  a recursive minimax algorithm with alpha-beta-pruning is used. The
algorithm should make use of all available cores via multi-threading.

Use ```ant build``` to compile.

The GUI is in German and the source code in a horrible German-Englisch mixture
("Denglisch").