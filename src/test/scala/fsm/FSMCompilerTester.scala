package fsm

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.BeforeAndAfterAll
import java.io.File

class FSMCompilerTester extends AnyFlatSpec with ChiselScalatestTester with BeforeAndAfterAll {
    override def beforeAll() = {
        val graph = new fsm.FSMGraph("src/test/scala/fsm/test-dotfiles/sample_3.dot")
        val adj_list = graph.build_adj_list()
        val unreachable_states = graph.reachability_bfs(adj_list)
        assert(unreachable_states.size == 1)
        val unopt_model = new FSMCompiler(false, "FSMGenUnopt")
        val opt_model = new FSMCompiler(true, "FSMGenOpt")
        unopt_model.build(graph)
        opt_model.build(graph)
        
        val unopt_file = new File("src/test/scala/fsm/outputs/test_unopt.scala")
        unopt_file.getParentFile().mkdirs()
        if (unopt_file.exists && unopt_file.isFile) {
            unopt_file.delete()
        }
        unopt_model.generation(os.Path("src/test/scala/fsm/outputs/test_unopt.scala", os.pwd))

        val opt_file = new File("src/test/scala/fsm/outputs/test_opt.scala")
        opt_file.getParentFile().mkdirs()
        if (opt_file.exists && unopt_file.isFile) {
            opt_file.delete()
        }
        opt_model.generation(os.Path("src/test/scala/fsm/outputs/test_opt.scala", os.pwd))
    }
    it should "generate a chisel source file (no optimization) without errors" in {
        val graph = new fsm.FSMGraph("src/test/scala/fsm/test-dotfiles/sample.dot")
        val adj_list = graph.build_adj_list()
        val unreachable_states = graph.reachability_bfs(adj_list)
        assert(unreachable_states.size == 0)
        val model = new FSMCompiler(false, "FSMGen")
        model.build(graph)
        val file = new File("src/test/scala/fsm/outputs/test.scala")
        file.getParentFile().mkdirs()
        if (file.exists && file.isFile) {
            file.delete()
        }
        model.generation(os.Path("src/test/scala/fsm/outputs/test.scala", os.pwd))
    }
    it should "generate a chisel source file (no optimization) without errors and detect unreachable states" in {
        val graph = new fsm.FSMGraph("src/test/scala/fsm/test-dotfiles/sample_3.dot")
        val adj_list = graph.build_adj_list()
        val unreachable_states = graph.reachability_bfs(adj_list)
        assert(unreachable_states.size == 1)
        val model = new FSMCompiler(false, "FSMGen")
        model.build(graph)
        val file = new File("src/test/scala/fsm/outputs/test.scala")
        file.getParentFile().mkdirs()
        if (file.exists && file.isFile) {
            file.delete()
        }
        model.generation(os.Path("src/test/scala/fsm/outputs/test.scala", os.pwd))
    }
    it should "generate a chisel source file (optimized) without errors and detect unreachable states" in {
        val graph = new fsm.FSMGraph("src/test/scala/fsm/test-dotfiles/sample_3.dot")
        val adj_list = graph.build_adj_list()
        val model = new FSMCompiler(true, "FSMGen")
        model.build(graph)
        val file = new File("src/test/scala/fsm/outputs/test.scala")
        file.getParentFile().mkdirs()
        if (file.exists && file.isFile) {
            file.delete()
        }
        model.generation(os.Path("src/test/scala/fsm/outputs/test.scala", os.pwd))
    }
}