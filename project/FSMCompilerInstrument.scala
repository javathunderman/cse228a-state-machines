import java.io.File

class FSMCompilerInstrument {
    val graph = new FSMGraph("src/test/scala/fsm/test-dotfiles/sample_3.dot")
    val adj_list = graph.build_adj_list()
    val unreachable_states = graph.reachability_bfs(adj_list)

    val unopt_model = new FSMCompiler(false, "FSMGenUnopt")
    unopt_model.build(graph)
    
    val unopt_file = new File("compiler/src/main/scala/compiler/test_unopt.scala")
    unopt_file.getParentFile().mkdirs()
    if (unopt_file.exists && unopt_file.isFile) {
        unopt_file.delete()
    }
    unopt_model.generation(os.Path("compiler/src/main/scala/compiler/test_unopt.scala", os.pwd))
}