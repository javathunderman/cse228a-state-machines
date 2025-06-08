package fsm

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.BeforeAndAfterAll
import java.io.File
import scala.io.Source

class FSMCompilerTester extends AnyFlatSpec with ChiselScalatestTester {
    def default_test(filePath: String, unreachable_state_exp: Int, optimization: Boolean, nameParam: Option[String]): Unit = {
      val graph = new fsm.FSMGraph(filePath)
      val adj_list = graph.build_adj_list()
      val unreachable_states = graph.reachability_bfs(adj_list)
      val outPath = "src/test/scala/fsm/outputs/"
      val fileName = nameParam match {
        case Some(value) => value
        case None => "test.scala"
      }
      assert(unreachable_states.size == unreachable_state_exp)
      val model = new FSMCompiler(optimization, "FSMGen")
      model.build(graph)
      val file = new File(outPath + fileName)
      file.getParentFile().mkdirs()
      if (file.exists && file.isFile) {
          file.delete()
      }
      model.generation(os.Path(outPath + fileName, os.pwd))
    }
    it should "generate the [un]optimized state machines without error" in {
        val graph = new fsm.FSMGraph("src/test/scala/fsm/test-dotfiles/sample_3.dot")
        val adj_list = graph.build_adj_list()
        val unreachable_states = graph.reachability_bfs(adj_list)
        val output_dir_str = "src/test/scala/fsm/outputs/"
        val test_opt_dest = os.Path(output_dir_str + "test_opt.scala", os.pwd)
        val test_unopt_dest = os.Path(output_dir_str + "test_unopt.scala", os.pwd)
        val test_opt_default = os.Path(output_dir_str + "test_opt_default.scala", os.pwd)
        val test_unopt_default = os.Path(output_dir_str + "test_unopt_default.scala", os.pwd)
        assert(unreachable_states.size == 1)
        val unopt_model = new FSMCompiler(false, "FSMGenUnopt")
        val opt_model = new FSMCompiler(true, "FSMGenOpt")
        unopt_model.build(graph)
        opt_model.build(graph)
        
        val unopt_file = new File(test_unopt_dest.toString())
        unopt_file.getParentFile().mkdirs()
        if (unopt_file.exists && unopt_file.isFile) {
            unopt_file.delete()
        }
        unopt_model.generation(test_unopt_dest)

        val opt_file = new File(test_opt_dest.toString())
        opt_file.getParentFile().mkdirs()
        if (opt_file.exists && unopt_file.isFile) {
            opt_file.delete()
        }
        opt_model.generation(test_opt_dest)
        val output_opt = Source.fromFile(test_opt_dest.toString()).getLines().toArray
        val output_unopt = Source.fromFile(test_unopt_dest.toString()).getLines().toArray
        val output_opt_default = Source.fromFile(test_opt_default.toString()).getLines().toArray
        val output_unopt_default = Source.fromFile(test_unopt_default.toString()).getLines().toArray
        assert(output_opt.sameElements(output_opt_default))
        assert(output_unopt.sameElements(output_unopt_default))

        opt_file.delete()
        unopt_file.delete()
    }
    it should "generate a chisel source file (no optimization) without errors" in {
        default_test("src/test/scala/fsm/test-dotfiles/sample.dot", 0, false, None)
    }
    
    it should "generate a chisel source file (no optimization) without errors and detect unreachable states" in {
        default_test("src/test/scala/fsm/test-dotfiles/sample_3.dot", 1, false, None)
    }
    it should "generate a chisel source file (optimized) without errors and detect unreachable states" in {
        default_test("src/test/scala/fsm/test-dotfiles/sample_3.dot", 1, true, None)
    }
    it should "generate a chisel source file of the raccoon FSM without errors" in {
        default_test("src/test/scala/fsm/test-dotfiles/raccoon.dot", 0, true, None)
    }
    it should "generate a chisel source file of the host FSM without errors" in {
        default_test("src/test/scala/fsm/test-dotfiles/host.dot", 0, false, Some("host_fsm.scala"))
        val sm_file = new File("src/test/scala/fsm/outputs/host_fsm.scala")
        val output_sm = Source.fromFile("src/test/scala/fsm/outputs/host_fsm.scala").getLines().toArray
        val output_sm_default = Source.fromFile("src/test/scala/fsm/outputs/host_fsm_default.scala").getLines().toArray
        assert(output_sm.sameElements(output_sm_default))
        sm_file.delete()
        
    }
}