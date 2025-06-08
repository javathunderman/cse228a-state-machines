Chisel State Machine Generator
==============================
Generates Chisel hardware templates of Moore state machines given a Graphviz style DOT file. 

Running instructions
====================
- Create a dotfile (sample given in tests directory)
- Instantiate an FSMGraph object with the file path to the dotfile
- (Optionally) Build the adjacency list representation with `.build_adj_list()`, then use `.reachability_bfs()` to determine if there are unreachable states.
- Instantiate an FSMCompiler object (pass a flag for enabling/disabling the unreachable state pruning, and the FSMGraph object)
  - Call `FSMCompiler.build()` to get the tree representation of the Chisel template
  - (Subequently) call `FSMCompiler.generation`, with a `Path` object representing the desired output file. 
- For an example, see the `default_test` function in `FSMCompilerTester.scala` for an example of how to use the Chisel template generator. 

Progress
========
Completed:
  - Parsing the dotfile
  - Building a simple Moore state machine (either in software or in hardware via Chisel)
  - Tests
    - Ensures transitions are taken and the states are updated as we expect
    - Rejects illegal transitions for a simple state machine
  - Generation of Chisel templates
  - State reachbility analysis (via BFS) and pruning
    - Equivalence between optimized/unoptimized FSM implementation proven for simple example

In progress:
  - ~~Formal LTL models?~~
  - ~~Mealy machine support (output state dependent on transition asserted + values)~~

Future work: 
  - Dead state analysis
  - More robust equivalence checking between unoptimized/optimized versions