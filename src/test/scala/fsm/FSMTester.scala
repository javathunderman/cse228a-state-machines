package fsm

import chisel3._
import chiseltest._
import chisel3.experimental.BundleLiterals._
import org.scalatest.flatspec.AnyFlatSpec
// Old tester for simple FSM hardware generator
// See FSMCompilerTester and TestEquivalence for robust template verification/testing
class FSMTester extends AnyFlatSpec with ChiselScalatestTester {
  it should "step through states in simple state machine" in {
    val fsm_graph = FSMGraph("src/test/scala/fsm/test-dotfiles/sample.dot")
    test(new FSM(fsm_graph)) { dut =>
      dut.io.in(0).poke(true.B)
      dut.io.in(1).poke(false.B)
      dut.io.in(2).poke(false.B)
      dut.clock.step()
      dut.io.out.expect(1)

      dut.io.in(0).poke(false.B)
      dut.io.in(1).poke(true.B)
      dut.io.in(2).poke(false.B)
      dut.clock.step()
      dut.io.out.expect(2)
      
      dut.io.in(0).poke(false.B)
      dut.io.in(1).poke(false.B)
      dut.io.in(2).poke(true.B)
      dut.clock.step()
      dut.io.out.expect(2)
    }
  }
  it should "prevent illegal transitions in a simple state machine" in {
    val fsm_graph = FSMGraph("src/test/scala/fsm/test-dotfiles/sample.dot")
    test(new FSM(fsm_graph)) { dut =>
      dut.io.in(0).poke(true.B)
      dut.io.in(1).poke(false.B)
      dut.io.in(2).poke(false.B)
      dut.clock.step()
      dut.io.out.expect(1)

      dut.io.in(0).poke(false.B)
      dut.io.in(1).poke(true.B)
      dut.io.in(2).poke(false.B)
      dut.clock.step()
      dut.io.out.expect(2)
      
      dut.io.in(0).poke(true.B)
      dut.io.in(1).poke(false.B)
      dut.io.in(2).poke(false.B)
      dut.clock.step()
      dut.io.out.expect(2)
    }
  }
  it should "handle multiple possible transitions" in {
    val fsm_graph = FSMGraph("src/test/scala/fsm/test-dotfiles/sample_2.dot")
    test(new FSM(fsm_graph)) { dut =>
      dut.io.in(0).poke(true.B)
      dut.io.in(1).poke(false.B)
      dut.io.in(2).poke(false.B)
      dut.io.in(3).poke(false.B)
      dut.io.in(4).poke(false.B)
      dut.clock.step()
      dut.io.out.expect(1)

      dut.io.in(0).poke(false.B)
      dut.io.in(1).poke(false.B)
      dut.io.in(2).poke(false.B)
      dut.io.in(3).poke(true.B)
      dut.io.in(4).poke(false.B)
      dut.clock.step()
      dut.io.out.expect(3)

      dut.io.in(0).poke(false.B)
      dut.io.in(1).poke(false.B)
      dut.io.in(2).poke(false.B)
      dut.io.in(3).poke(false.B)
      dut.io.in(4).poke(true.B)
      dut.clock.step()
      dut.io.out.expect(1)

      dut.io.in(0).poke(false.B)
      dut.io.in(1).poke(true.B)
      dut.io.in(2).poke(false.B)
      dut.io.in(3).poke(false.B)
      dut.io.in(4).poke(false.B)
      dut.clock.step()
      dut.io.out.expect(2)
      
      dut.io.in(0).poke(false.B)
      dut.io.in(1).poke(false.B)
      dut.io.in(2).poke(true.B)
      dut.io.in(3).poke(false.B)
      dut.io.in(4).poke(false.B)
      dut.clock.step()
      dut.io.out.expect(2)
    }
  }
}
