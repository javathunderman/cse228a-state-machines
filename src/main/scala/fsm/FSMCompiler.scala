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
    val openLine = "package fsm\nimport chisel3._\nimport chisel3.util._\n"
    val parent = None
    val child = None
    val closeLine = "\n"
}

class StateEnumElem(val states: Seq[State]) extends ChiselASTELem {
    val openLine = "object FSMState extends ChiselEnum {\n\t\tval " + states.foldLeft(""){case (acc, x) => {
        acc + x.label + ", "
    }}.stripSuffix(", ") + " = Value\n}\n"
    val parent = None
    val child = None
    val closeLine = "\n"
}

class TransitionEnumElem(val transitions: Seq[Transition]) extends ChiselASTELem {
    val openLine = "object FSMTransition extends ChiselEnum {\n\t\t val " + transitions.foldLeft(""){case (acc, x) => {
        acc + x.label + ", "
    }}.stripSuffix(", ") + " = Value\n}\n"
    val parent = None
    val child = None
    val closeLine = "\n"
}
class TopElem(val states: Seq[State], val transitions: Seq[Transition]) extends ChiselASTELem {
       
    val openLine = "class FSMGen extends Module {\n\tval state = RegInit(FSMState." + states(0).label + ")\n\tswitch(state) {\n"
    val parent = None
    val child = Some(new ArrayBuffer(states.length))
    val closeLine = "\n\t}\n}\n"
}
class StateElem(val parent_arg: ChiselASTELem, val state_arg: State) extends ChiselASTELem {
    val state = state_arg
    val openLine = s"\t is(FSMState.${state.label}) {\n"
    val parent = Some(parent_arg)
    val child = Some(new ArrayBuffer(0))
    val closeLine = "\n\t}\n"
}

class TransitionElem(val parent_arg: ChiselASTELem, val transition: Transition) extends ChiselASTELem {
    val openLine = parent_arg match {
        case _ : StateElem => "\t\twhen(io.transition === FSMTransition." + transition.label + ") {\n\t\t\t"
        case _ : TransitionElem => "\t\t.elsewhen(io.transition === FSMTransition." + transition.label + ") {\n\t\t\t"
        case _ => println("Illegal parent in syntax tree. Parsing bug?"); ""
    }
    
    val parent = Some(parent_arg)
    val child = None
    val closeLine = "\n\t\t}\n"
}

class FSMCompiler {
    val ast = new ArrayBuffer[ChiselASTELem](2)
    def build(graph: FSMGraph) : ArrayBuffer[ChiselASTELem] = {
        ast.addOne(new StaticTopElem)
        ast.addOne(new StateEnumElem(graph.statesTransitions._2))
        ast.addOne(new TransitionEnumElem(graph.statesTransitions._1))
        val topElem = new TopElem(graph.statesTransitions._2, graph.statesTransitions._1)
        
        for (i <- 0 until graph.statesTransitions._2.length) {
            val state_elem = new StateElem(topElem, graph.statesTransitions._2(i))
            val transition_elem = graph.statesTransitions._1.filter(x => x.source == graph.statesTransitions._2(i)).foldLeft(Seq.empty[TransitionElem]) {case (acc, x) => {
                if (acc.length > 0) {
                    acc :+ new TransitionElem(acc(0), x)
                } else {
                    acc :+ new TransitionElem(state_elem, x)
                }
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
