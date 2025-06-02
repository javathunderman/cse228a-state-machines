package fsm

import java.io.{File, FileWriter, Writer}
import scala.collection.mutable.ArrayBuffer

abstract class ASTElem {
    val openLine : String = ""
    val parent : Option[ASTElem] = None
    val child : Option[ArrayBuffer[ASTElem]] = None
    val closeLine : String = "\n"
    def generate(filePath: os.Path) : Unit = {
        os.write.append(filePath, openLine)
        if (child != None) {
            child.get.foreach(elem => elem.generate(filePath))
        }
        os.write.append(filePath, closeLine)
    }
}
class StaticTopElem extends ASTElem {
    override val openLine = "package fsm\nimport chisel3._\nimport chisel3.util._\n"
}

class EnumElem(val st: Seq[String], val enumName: String) extends ASTElem {
    override val openLine = "object " + enumName + " extends ChiselEnum {\n\tval " + st.foldLeft(""){case (acc, x) => {
        acc + x + ", "
    }}.stripSuffix(", ") + " = Value\n}\n"
    
}
class TopElem(val states: Seq[State], val transitions: Seq[Transition], val module_name: String, val state_enum_label: String, val transition_enum_label: String) extends ASTElem {
    override val openLine = "class " + module_name + " extends Module {\n\tval io = IO(new Bundle {\n\t\tval transition = Input(" + transition_enum_label + "())\n\t\tval state = Output(" + state_enum_label + "())\n\t})\n\tval state = RegInit(" + state_enum_label + "." + states(0).label + ")\n\tswitch(state) {\n"
    override val child = Some(new ArrayBuffer(states.length))
    override val closeLine = "\n\t}\n\tio.state := state\n}\n"
}
class StateElem(val parent_arg: ASTElem, val state_arg: State, val state_enum_label: String) extends ASTElem {
    val state = state_arg
    override val openLine = s"\t is(${state_enum_label}.${state.label}) {\n"
    override val parent = Some(parent_arg)
    override val child = Some(new ArrayBuffer(0))
    override val closeLine = "\t}\n"
}

class TransitionElem(val parent_arg: ASTElem, val transition: Transition, state_enum_label: String, transition_enum_label: String) extends ASTElem {
    override val openLine = parent_arg match {
        case _ : StateElem => "\t\twhen(io.transition === "+ transition_enum_label + "." + transition.label + ") {\n\t\t\tstate := " + state_enum_label + "." + transition.dest.label
        case _ : TransitionElem => "\t\t.elsewhen(io.transition === " + transition_enum_label + "." + transition.label + ") {\n\t\t\tstate := " + state_enum_label + "." + transition.dest.label
        case _ => println("Illegal parent in syntax tree. Parsing bug?"); ""
    }
    override val parent = Some(parent_arg)
    override val child = None
    override val closeLine = "\n\t\t}\n"
}

class FSMCompiler(val optimization: Boolean, module_name: String) {
    val ast = new ArrayBuffer[ASTElem](2)
    def build(graph: FSMGraph) : ArrayBuffer[ASTElem] = {
        // Check that states are reachable - warn if they are not
        val adj_list = graph.build_adj_list()
        val unreachable_states = graph.reachability_bfs(adj_list)
        if (unreachable_states.size > 0) {
            println(s"Warning: Unreachable states ${unreachable_states}")
        }
        // emit the imports
        ast.addOne(new StaticTopElem)
        val state_enum_label = module_name + "State"
        val transition_enum_label = module_name + "Transition"
        // create the enum types for states and transitions
        ast.addOne(new EnumElem(graph.statesTransitions._2.map(x => x.label), state_enum_label))
        ast.addOne(new EnumElem(graph.statesTransitions._1.map(x => x.label), transition_enum_label))
        
        // create the module type
        // all states/transitions are within this element of the AST
        val topElem = new TopElem(graph.statesTransitions._2, graph.statesTransitions._1, module_name, state_enum_label, transition_enum_label)
        // optionally prune out orphaned/unreachable states
        val states_set = if (optimization) graph.statesTransitions._2.diff(unreachable_states.toSeq) else graph.statesTransitions._2
        for (current_state <- states_set) {
            val state_elem = new StateElem(topElem, current_state, state_enum_label)
            val transition_elem = graph.statesTransitions._1.filter(x => x.source == current_state).foldLeft(Seq.empty[TransitionElem]) {case (acc, x) => {
                // handle difference between when and elsewhen for multiple possible transitions out of a state
                if (acc.length > 0) {
                    acc :+ new TransitionElem(acc(0), x, state_enum_label, transition_enum_label)
                } else {
                    acc :+ new TransitionElem(state_elem, x, state_enum_label, transition_enum_label)
                }
            }}

            // add all of the transition AST elements to a source state element
            // add the state to the tree
            if (transition_elem.length > 0) {
                state_elem.child.get.addAll(transition_elem)
                topElem.child.get.addOne(state_elem)
            }
            
        }
        ast.addOne(topElem)
    }
    def generation(filePath: os.Path) = {
        // rely on polymorphism to generate the right outputs
        for (i <- 0 until ast.length) {
            ast(i).generate(filePath)
        }
    }
}
