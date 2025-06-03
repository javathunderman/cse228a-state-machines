package fsm.outputs

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.BeforeAndAfterAll
import java.io.File

class FSMGenEquiv extends Module {
    val io = IO(new Bundle{
        val transition_unopt = Input(FSMGenUnoptTransition())
        val transition_opt = Input(FSMGenOptTransition())
        val state_opt = Output(FSMGenOptState())
        val state_unopt = Output(FSMGenUnoptState())
    })
    val unopt = Module(new FSMGenUnopt())
    val opt = Module(new FSMGenOpt())
    unopt.io.transition := io.transition_unopt
    opt.io.transition := io.transition_opt
    io.state_unopt := unopt.io.state
    io.state_opt := opt.io.state
}

class TestEquivalence extends AnyFlatSpec with ChiselScalatestTester {
    it should "step through the instantiated chisel design (unoptimized)" in {
        test(new FSMGenEquiv()) { dut =>
            dut.io.transition_unopt.poke(FSMGenUnoptTransition.moveToIntermediate)
            dut.io.transition_opt.poke(FSMGenOptTransition.moveToIntermediate)
            dut.clock.step()
            dut.io.state_unopt.expect(FSMGenUnoptState.Intermediate)
            dut.io.state_opt.expect(FSMGenOptState.Intermediate)
        }
    }
}