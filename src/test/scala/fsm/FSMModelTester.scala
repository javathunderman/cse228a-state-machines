package fsm

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.flatspec.AnyFlatSpec
import chisel3.experimental.BundleLiterals._
import java.io.File

class FSMModelTester extends AnyFlatSpec with ChiselScalatestTester {
    it should "step through each transition of the simple FSM" in {
        val graph = new fsm.FSMGraph("src/test/scala/fsm/sample.dot")
        val model = new FSMModel(graph)
        model.take_transition(0)
        assert(model.current_state.label == "Intermediate")
        model.take_transition(1)
        assert(model.current_state.label == "Final")
        model.take_transition(1)
        assert(model.current_state.label == "Final")
    }
    it should "disallow illegal transitions" in {
        val graph = new fsm.FSMGraph("src/test/scala/fsm/sample.dot")
        val model = new FSMModel(graph)
        model.take_transition(0)
        assert(model.current_state.label == "Intermediate")
        model.take_transition(1)
        assert(model.current_state.label == "Final")
        model.take_transition(0)
        assert(model.current_state.label == "Final")
    }
    it should "generate a correct chisel source file" in {
        val graph = new fsm.FSMGraph("src/test/scala/fsm/sample_2.dot")
        val model = new FSMCompiler()
        model.build(graph)
        val file = new File("src/test/scala/fsm/test.scala")
        if (file.exists && file.isFile) {
            file.delete()
        }
        model.generation(os.Path("src/test/scala/fsm/test.scala", os.pwd))
        assert(true)
    }
}