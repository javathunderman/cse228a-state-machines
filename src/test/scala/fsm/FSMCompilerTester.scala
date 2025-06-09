package fsm

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.BeforeAndAfterAll
import java.io.File
import scala.io.Source

class FSMCompilerTester extends AnyFlatSpec with ChiselScalatestTester {
    def default_test(filePath: String, unreachable_state_exp: Int, dead_state_exp: Int, optimization: Boolean, nameParam: Option[String], fsmName: Option[String]): Unit = {
      val graph = new fsm.FSMGraph(filePath)
      val adj_list = graph.build_adj_list()
      val unreachable_states = graph.reachability_bfs(adj_list)
      val dead_states = graph.dead_state_detection(adj_list)
      val outPath = "src/test/scala/fsm/outputs/"
      assert(unreachable_states.size == unreachable_state_exp)
      assert(dead_states.size == dead_state_exp)
      val model = new FSMCompiler(optimization, fsmName.getOrElse("FSMGen"))
      model.build(graph)
      val file = new File(outPath + nameParam.getOrElse("test.scala"))
      file.getParentFile().mkdirs()
      if (file.exists && file.isFile) {
          file.delete()
      }
      model.generation(os.Path(outPath + nameParam.getOrElse("test.scala"), os.pwd))
      file.deleteOnExit()
    }
    def unopt_opt_test(dotFilePath: String, outFileName: String, unreachable_state_count: Integer, templateName: String): Boolean = {
      val graph = new fsm.FSMGraph(dotFilePath)
      val adj_list = graph.build_adj_list()
      val unreachable_states = graph.reachability_bfs(adj_list)
      val output_dir_str = "src/test/scala/fsm/outputs/"
      val test_opt_dest = os.Path(output_dir_str + outFileName + "_opt.scala", os.pwd)
      val test_unopt_dest = os.Path(output_dir_str + outFileName + "_unopt.scala", os.pwd)
      val test_opt_default = os.Path(output_dir_str + outFileName + "_opt_default.scala", os.pwd)
      val test_unopt_default = os.Path(output_dir_str + outFileName + "_unopt_default.scala", os.pwd)
      assert(unreachable_states.size == unreachable_state_count)
      val unopt_model = new FSMCompiler(false, templateName + "Unopt")
      val opt_model = new FSMCompiler(true, templateName + "Opt")
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
    it should "generate the [un]optimized state machines without errors" in {
        unopt_opt_test("src/test/scala/fsm/test-dotfiles/sample_3.dot", "test", 1, "FSMGen")
    }
    it should "generate a chisel source file (no optimization) without errors" in {
        default_test("src/test/scala/fsm/test-dotfiles/sample.dot", 0, 0, false, None, None)
    }
    it should "generate a chisel source file (no optimization) without errors and detect unreachable states" in {
        default_test("src/test/scala/fsm/test-dotfiles/sample_3.dot", 1, 1, false, None, None)
    }
    it should "generate a chisel source file (optimized) without errors and detect unreachable states" in {
        default_test("src/test/scala/fsm/test-dotfiles/sample_3.dot", 1, 1, true, None, None)
    }
    it should "generate a chisel source file of the raccoon FSM without errors" in {
        default_test("src/test/scala/fsm/test-dotfiles/raccoon.dot", 0, 0, true, None, None)
    }
    it should "generate a chisel source file of the host FSM without errors" in {
        default_test("src/test/scala/fsm/test-dotfiles/host.dot", 0, 0, false, Some("host_fsm.scala"), Some("HostGen"))
        val sm_file = new File("src/test/scala/fsm/outputs/host_fsm.scala")
        val output_sm = Source.fromFile("src/test/scala/fsm/outputs/host_fsm.scala").getLines().toArray
        val output_sm_default = Source.fromFile("src/test/scala/fsm/outputs/host_fsm_default.scala").getLines().toArray
        assert(output_sm.sameElements(output_sm_default))
        sm_file.delete()
    }
    it should "generate an optimized/unoptimized version of the host FSM without errors" in {
        unopt_opt_test("src/test/scala/fsm/test-dotfiles/host_extra_states.dot", "host_extra_states", 2, "HostGen")
    }
    it should "detect states that we could get stuck in" in {
        default_test("src/test/scala/fsm/test-dotfiles/host_extra_states.dot", 2, 1, false, Some("host_extra_states_unopt.scala"), Some("HostGen"))
    }
}