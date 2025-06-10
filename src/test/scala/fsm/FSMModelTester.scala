package fsm

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.flatspec.AnyFlatSpec
import chisel3.experimental.BundleLiterals._
// Old tester for simple FSM software model
// See FSMCompilerTester and TestEquivalence for robust template verification/testing
class FSMModelTester extends AnyFlatSpec with ChiselScalatestTester {
    it should "step through each transition of the simple FSM" in {
        val graph = new fsm.FSMGraph("src/test/scala/fsm/test-dotfiles/sample.dot")
        val model = new FSMModel(graph)
        model.take_transition(0)
        assert(model.current_state.label == "Intermediate")
        model.take_transition(1)
        assert(model.current_state.label == "Final")
        model.take_transition(1)
        assert(model.current_state.label == "Final")
    }
    it should "disallow illegal transitions" in {
        val graph = new fsm.FSMGraph("src/test/scala/fsm/test-dotfiles/sample.dot")
        val model = new FSMModel(graph)
        model.take_transition(0)
        assert(model.current_state.label == "Intermediate")
        model.take_transition(1)
        assert(model.current_state.label == "Final")
        model.take_transition(0)
        assert(model.current_state.label == "Final")
    }
}