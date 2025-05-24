package fsm

class FSMModel(fsm_graph: FSMGraph) {
  var current_state = fsm_graph.statesTransitions._2(0)
  def take_transition(transition_index : Int) = {
    if (current_state == fsm_graph.statesTransitions._1(transition_index).source) {
      current_state = fsm_graph.statesTransitions._1(transition_index).dest
      println(s"Took the transition ${fsm_graph.statesTransitions._1(transition_index).label}")
    }
  }
}
