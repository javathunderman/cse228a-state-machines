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

class HostGenEquiv extends Module {
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
    val enumStrRegex = raw"\=(.*)\)".r.unanchored
    def stringToEnum(s: String, enumType: ChiselEnum) : Option[enumType.Type] =
        enumType.all.map(x => x.toString()).zipWithIndex.foldLeft(Option.empty[enumType.Type]){case (acc, x) => x._1 match {
            case enumStrRegex(enumStr) => {
                if (enumStr == s) {
                    Some(enumType.all(x._2))
                } else {
                    acc
                }
            }
            case _ => println("error when parsing enum type names"); None
        }
    }
    def enumToString(enumType: ChiselEnum#Type) : String = {
        enumType.toString() match {
            case enumStrRegex(enumStr) => enumStr
            case _ => println("error when emitting enum type name"); ""
        }
    }
    
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
        test(new HostGenEquiv()) { dut => 
            val graph = new fsm.FSMGraph("src/test/scala/fsm/test-dotfiles/host_extra_states.dot")
            val adj_list = graph.build_adj_list()    
            val paths = graph.path_bfs(adj_list)
            for (path <- paths.keySet) {
                for (transitionStatePair <- paths(path)) {
                    dut.io.transition_opt.poke(stringToEnum(transitionStatePair._1, HostGenOptTransition).get)
                    dut.io.transition_unopt.poke(stringToEnum(transitionStatePair._1, HostGenUnoptTransition).get)
                    dut.clock.step()
                    assert(enumToString(dut.io.state_opt.peek()) == enumToString(dut.io.state_unopt.peek()))
                }
            }
        }
    }
}