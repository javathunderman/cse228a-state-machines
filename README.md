Chisel State Machine Generator
==============================
Generates Chisel hardware templates of Moore state machines given a Graphviz style DOT file. 

### Files:
- `FSM.scala`: Initial hardware generator (used more for proof of concept)
  - `FSMTester`: Instantiates and steps through a simple FSM (in hardware)
- `FSMModel`: Initial software model (used more for proof of concept)
  - `FSMModelTester`: Instantiates and steps through a simple FSM (in software)
- `FSMGraph`: Parsing framework and graph analysis
  - Initially returns a set of States/Transitions (as defined within the file)
  - Also has functions for:
    - Building an adjacency list representation
    - Performing BFS
    - Reachability analysis
    - Extracting all paths between entry and final states
    - Dead state analysis
  - `FSMGraphTester`: Just tests the set of transitions/states after parsing.  
- `FSMCompiler`: Generates Chisel templates after parsing
  - Build function: Takes a set of states/transitions, calls the adjacency list building function, and generates an AST of the Chisel template
  - Contains small subclasses for each of the elements within the tree
    - Opening line, set of child nodes in the AST, and closing line (allows for recursive code generation)
  - Generate function: Writes the opening line, the children nodes, and closing line
  - `FSMCompilerTester`
    - `default_test` - checks that a Chisel file is generated without errors, and verifies correct # of unreachable/dead states
    - `unopt_opt_test` - generates two Chisel files, one with optimization (unreachable state elimination) and one without. Subsequently diffs the generated files against a known safe version
- `outputs/`
  - `_default` files - used by `FSMCompilerTester` to diff against newly generated Chisel templates
  - `TestEquivalence`
    - Instantiates hardware harnesses with optimized/unoptimized versions of simple FSM example, and the host/global ops FSM example
    - `enumToString` and `stringToEnum`: convert between String value and enum type of transition/state for each instantiated hardware component (relies on Scala type argument, `stringToEnum` is roughly analogous to Scala's built in `.getName` method on enums)
    - One manual example (simple FSM), one complex example (host/global ops FSM example)
      - Finds all paths and proves state equivalence between the two versions after each transition

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

Potential future work:
  - Formal LTL models?
  - Mealy machine support (output state dependent on transition asserted + values)
  - Boolean/logical operations within transition labels (e.g. raccoon example: `noise` and `noNoise` transitions could be `noise` and `!noise`)