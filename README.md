Chisel State Machine Generator
==============================
Generates (for now) Moore state machines given a Graphviz style DOT file. 

Running instructions
====================
- Create a dotfile (sample given in tests directory)
- Instantiate an FSMGraph object with the file path to the dotfile
- Pass the object into either the Scala model (FSMModel) or Chisel hardware generator (FSM)
  - For the Scala model: Call `takeTransition()` with the index of the transition you wish to take. 
    - e.g. if you have two transitions that correspond to states 1 -> 2, and 2 -> 3, a sample call would be `takeTransition(0)` from state 1. 
    - If the transition is invalid, nothing happens - if it is valid, the `current_state` value in the model is updated immediately. 
  - For the Chisel generator: Assert the input wire that corresponds to the index of the transition you wish to take
    - e.g. repeating the three state FSM from before: assert `io.in(0)` to take the transition from state 1 to 2. 
    - If the transition is invalid, nothing happens - if it is valid, the `out` value will be updated with the index of the destination state on the next clock cycle. 

See FSMModelTester and FSMTester respectively for a demonstration on how to run the Scala model and Chisel generator. 

Progress
========
Completed:
  - Parsing the dotfile
  - Building a simple Moore state machine (either in software or in hardware via Chisel)
  - Tests
    - Ensures transitions are taken and the states are updated as we expect
    - Rejects illegal transitions for a simple state machine

In progress:
  - Refactoring FSMGraph (used to transform the text representation of the graph into Scala objects)
    - Currently quite messy and not super readable or well-documented
  - Carry state information into hardware?
    - Remove the need for integer labels (at least, at the user-level) - this should be using the transition and state labels that are assigned by the user in the dotfile
  - More extensive testing/verification
    - Formal LTL models?
    - State reachability?
    - More unit tests in general
  - Mealy machine support (output state dependent on transition asserted + values)
  - Optimization
    - State pruning
    - Equivalence with unoptimized version
      - May apply state reduction only in hardware to keep software model as a sanity check. 
