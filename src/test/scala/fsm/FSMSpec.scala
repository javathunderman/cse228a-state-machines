// See README.md for license details.

package fsm

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.flatspec.AnyFlatSpec
import chisel3.experimental.BundleLiterals._

class FSMSpec extends AnyFlatSpec with ChiselScalatestTester {
  it should "read the dotfile in, and process the states correctly" in {
    val fsm_test = new fsm.FSM("/home/arjun/coursecode/state-machine-228/src/test/scala/fsm/sample.dot")
    assert(fsm_test.states.length == 3)
  }
}

