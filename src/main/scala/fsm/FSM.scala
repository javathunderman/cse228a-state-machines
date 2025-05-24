package fsm

import chisel3._
import chisel3.util
import scala.io.Source
import scala.util.matching.Regex

class FSM(val fsm_graph: FSMGraph) extends Module {
  val transition_indices : Seq[(Int, Int, String)] = fsm_graph.statesTransitions._1.map (transition => {
    val srcIndex = fsm_graph.statesTransitions._2.indexOf(transition.source)
    val destIndex = fsm_graph.statesTransitions._2.indexOf(transition.dest)
    (srcIndex, destIndex, transition.label)
  })
  val io = IO(new Bundle {
      val in  = Input(Vec(fsm_graph.statesTransitions._1.length, Bool()))
      val out = Output(UInt(util.log2Ceil(fsm_graph.statesTransitions._2.length).W))
  })
  val intState = RegInit(0.U(util.log2Ceil(fsm_graph.statesTransitions._2.length).W))
  for (i <- 0 until fsm_graph.statesTransitions._1.length) {
    when (io.in(i) && intState === transition_indices(i)._1.U) {
      intState := transition_indices(i)._2.U
      printf(f"Took the transition ${transition_indices(i)._3}\n")
    }
  }
  io.out := intState
}