package fsm

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.flatspec.AnyFlatSpec
import chisel3.experimental.BundleLiterals._
// Basic parsing tests
class FSMSpec extends AnyFlatSpec with ChiselScalatestTester {
  it should "read the dotfile in, and process the states/transitions correctly" in {
    val fsm_test = new fsm.FSMGraph("src/test/scala/fsm/test-dotfiles/sample.dot")
    assert(fsm_test.statesTransitions._2(0).label == "ENTRY")
    assert(fsm_test.statesTransitions._2(1).label == "Intermediate")
    assert(fsm_test.statesTransitions._2(2).label == "Final")
    assert(fsm_test.statesTransitions._1(0).label == "moveToIntermediate")
    assert(fsm_test.statesTransitions._1(1).label == "moveToFinal")
    assert(fsm_test.statesTransitions._1(2).label == "repeatFinal")
  }
}

