package fsm.outputs

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.BeforeAndAfterAll
import java.io.File
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