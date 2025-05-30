package fsm

import java.io.{File, FileWriter, Writer}

import scala.collection.mutable.ArrayBuffer

trait ChiselASTELem {
    val openLine : String
    val parent : Option[ChiselASTELem]
    val child : Option[ArrayBuffer[ChiselASTELem]]
    val closeLine : String
    def generate(filePath: os.Path) : Unit = {
        os.write.append(filePath, openLine)
        if (child != None) {
            child.get.foreach(elem => elem.generate(filePath))
        }
        os.write.append(filePath, closeLine)
    }
}
class StaticTopElem extends ChiselASTELem {
    val openLine: String = "package fsm\nimport chisel3._\nimport chisel3.util._\n"
    val parent : Option[ChiselASTELem] = None
    val child = None
    val closeLine: String = "\n"
}
class TopElem(val states: Seq[State], val transitions: Seq[Transition]) extends ChiselASTELem {
    var openLineTest: String = "object FSMState extends ChiselEnum {\n\t\tval " + states.foldLeft(""){case (acc, x) => {
        acc + x.label + ", "
    }}.stripSuffix(", ") + " = Value\n}\n"
    openLineTest = openLineTest ++ "object FSMTransition extends ChiselEnum {\n\t\t val " + transitions.foldLeft(""){case (acc, x) => {
        acc + x.label + ", "
    }}.stripSuffix(", ") + " = Value\n}\n"
    openLineTest = openLineTest ++ "class FSMGen extends Module {\n\tval state = RegInit(FSMState." + states(0).label + ")\n\tswitch(state) {\n"
    val openLine: String =  openLineTest
    val parent : Option[ChiselASTELem] = None
    val child = Some(new ArrayBuffer(states.length))
    val closeLine : String = "\t\n}\n}\n"
}
class StateElem(val parent_arg: ChiselASTELem, val state_arg: State) extends ChiselASTELem {
    val state : State = state_arg
    val openLine : String = s"\t is(FSMState.${state.label}) {\n"
    val parent : Option[ChiselASTELem] = Some(parent_arg)
    val child : Option[ArrayBuffer[ChiselASTELem]] = Some(new ArrayBuffer(0))
    val closeLine : String = "\t\t\n}\n"
}

class TransitionElem(val parent_arg: StateElem, val transition: Transition ) extends ChiselASTELem {
    val openLine : String = "\t\twhen(io.transition === FSMTransition." + transition.label + ") {\n\t\t\t"
    val parent : Option[ChiselASTELem] = Some(parent_arg) // state
    val child : Option[ArrayBuffer[ChiselASTELem]] = None
    val closeLine : String = "\t\t\t\n}\n"
}

class FSMCompiler {
    val ast = new ArrayBuffer[ChiselASTELem](2)
    def build(graph: FSMGraph) : ArrayBuffer[ChiselASTELem] = {
        ast.addOne(new StaticTopElem)
        val topElem = new TopElem(graph.statesTransitions._2, graph.statesTransitions._1)
        
        for (i <- 0 until graph.statesTransitions._2.length) {
            val state_elem = new StateElem(topElem, graph.statesTransitions._2(i))
            val transition_elem = graph.statesTransitions._1.filter(x => x.source == graph.statesTransitions._2(i)).foldLeft(Seq.empty[TransitionElem]) {case (acc, x) => {
                acc :+ new TransitionElem(state_elem, x)
            }}
            if (transition_elem.length > 0) {
                state_elem.child.get.addAll(transition_elem)
            }
            topElem.child.get.addOne(state_elem)
        }
        ast.addOne(topElem)
    }
    def generation(filePath: os.Path) = {
        for (i <- 0 until ast.length) {
            ast(i).generate(filePath)
        }
    }
    // todo: build tree of program to make the generation less hacky
}
