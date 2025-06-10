Chisel State Machine Generator
==============================
Generates Chisel hardware templates of Moore state machines given a Graphviz style DOT file. 

Running instructions
====================
- Create a dotfile (sample given in tests directory)
- Instantiate an FSMGraph object with the file path to the dotfile
- Build the adjacency list representation with `.build_adj_list()`, then use `.reachability_bfs()` to determine if there are unreachable states.
- Instantiate an FSMCompiler object (pass a flag for enabling/disabling the unreachable state pruning, and the FSMGraph object)
  - Call `FSMCompiler.build()` to get the tree representation of the Chisel template
  - (Subequently) call `FSMCompiler.generation`, with a `Path` object representing the desired output file. 
- As an example (adapted from `default_test` in `FSMCompilerTester`):

```
  val graph = new fsm.FSMGraph("test.dot")
  val adj_list = graph.build_adj_list()
  val unreachable_states = graph.reachability_bfs() // returns a Set of unreachable State (no in-edges)
  val dead_states = graph.dead_state_detection() // returns a Set of dead State (no out-edges)
  
  // Instantiate the Compiler
  val model = new FSMCompiler(optimization, "FSMGen") // produces a template with the Chisel class named FSMGen
  // Build the AST
  model.build(graph)
  // Generate the template
  model.generation(os.Path("src/test/scala/fsm/outputs/test.scala", os.pwd))
```

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
    - Equivalence between optimized/unoptimized FSM implementation proven for complex example
  - Dead/trap state detection
  - Parameterizable equivalence testing

In progress:
  - ~~Formal LTL models?~~
  - ~~Mealy machine support (output state dependent on transition asserted + values)~~