package fsm

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.flatspec.AnyFlatSpec
import chisel3.experimental.BundleLiterals._
import java.io.File

class FSMCompilerTester extends AnyFlatSpec with ChiselScalatestTester {
    it should "generate a correct chisel source file (no optimization)" in {
        val graph = new fsm.FSMGraph("src/test/scala/fsm/test-dotfiles/sample.dot")
        val adj_list = graph.build_adj_list()
        val unreachable_states = graph.reachability_bfs(adj_list)
        assert(unreachable_states.size == 0)
        val model = new FSMCompiler(false)
        model.build(graph)
        val file = new File("src/test/scala/fsm/outputs/test.scala")
        file.getParentFile().mkdirs()
        if (file.exists && file.isFile) {
            file.delete()
        }
        model.generation(os.Path("src/test/scala/fsm/outputs/test.scala", os.pwd))
    }
    it should "generate a correct chisel source file (no optimization) and detect unreachable states" in {
        val graph = new fsm.FSMGraph("src/test/scala/fsm/test-dotfiles/sample_3.dot")
        val adj_list = graph.build_adj_list()
        val unreachable_states = graph.reachability_bfs(adj_list)
        assert(unreachable_states.size == 1)
        val model = new FSMCompiler(false)
        model.build(graph)
        val file = new File("src/test/scala/fsm/outputs/test.scala")
        file.getParentFile().mkdirs()
        if (file.exists && file.isFile) {
            file.delete()
        }
        model.generation(os.Path("src/test/scala/fsm/outputs/test.scala", os.pwd))
    }
    it should "generate a correct chisel source file (with optimization) and detect unreachable states" in {
        val graph = new fsm.FSMGraph("src/test/scala/fsm/test-dotfiles/sample_3.dot")
        val adj_list = graph.build_adj_list()
        val model = new FSMCompiler(true)
        model.build(graph)
        val file = new File("src/test/scala/fsm/outputs/test.scala")
        file.getParentFile().mkdirs()
        if (file.exists && file.isFile) {
            file.delete()
        }
        model.generation(os.Path("src/test/scala/fsm/outputs/test.scala", os.pwd))
    }
}