package fsm.outputs

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.BeforeAndAfterAll
import java.io.File

class FSMGenEquiv extends Module {
    def stringToEnumOpt(s: String): Option[FSMGenOptTransition.Type] =
        FSMGenOptTransition.all.find(_.toString == s)
    def stringToEnumUnOpt(s: String): Option[FSMGenUnoptTransition.Type] =
        FSMGenUnoptTransition.all.find(_.toString == s)
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

class HostGenEquiv extends Module {
    def stringToEnumOpt(s: String): Option[HostGenOptTransition.Type] =
        HostGenOptTransition.all.find(_.toString == s)
    def stringToEnumUnOpt(s: String): Option[HostGenUnoptTransition.Type] =
        HostGenUnoptTransition.all.find(_.toString == s)
    val io = IO(new Bundle{
        val transition_unopt = Input(HostGenUnoptTransition())
        val transition_opt = Input(HostGenOptTransition())
        val state_opt = Output(HostGenOptState())
        val state_unopt = Output(HostGenUnoptState())
    })
    val unopt = Module(new HostGenUnopt())
    val opt = Module(new HostGenOpt())
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
    it should "prove equivalence over paths" in {
        test(new FSMGenEquiv()) { dut => 
            val graph = new fsm.FSMGraph("src/test/scala/fsm/test-dotfiles/host_extra_states.dot")
            val adj_list = graph.build_adj_list()    
            val paths = graph.path_bfs(adj_list)
            for (path <- paths.keySet) { // entry and end state pair
                for (i <- 0 until paths(path).length) {
                    dut.io.transition_opt.poke(dut.stringToEnumOpt(paths(path)(i)._1).get)
                    dut.io.transition_unopt.poke(dut.stringToEnumUnOpt(paths(path)(i)._1).get)
                    dut.clock.step()
                    assert(dut.io.state_opt.peek() == dut.io.state_unopt.peek())
                    assert(dut.io.state_opt.peek() == paths(path)(i)._2)
                    // todo: Path is likely backwards, need to reverse and run through each of the paths
                }
            }
        }
    }
}