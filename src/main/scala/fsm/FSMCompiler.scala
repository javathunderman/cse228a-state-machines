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
        if (child == None) {
            return
        } else {
            child.get.foreach(elem => elem.generate(filePath))
        }
        os.write.append(filePath, closeLine)
    }
}
class StaticTopElem extends ChiselASTELem {
    val openLine: String = "package fsm\nimport chisel3._\nimport chisel3.util\nimport scala.io.Source\nimport scala.util.matching.Regex\n"
    val parent : Option[ChiselASTELem] = None
    val child = None
    val closeLine: String = ""
}
class TopElem(val states: Seq[State]) extends ChiselASTELem {
    var openLineTest: String = "class FSMGen extends Module {\nobject FSMEnum extends ChiselEnum {\nval " + states.foldLeft(""){case (acc, x) => {
        acc + x.label + ", "
    }} + " = Value\n}"
    val openLine: String =  openLineTest
    val parent : Option[ChiselASTELem] = None
    val child = Some(new ArrayBuffer(states.length))
    val closeLine : String = "}"
}
class StateElem(val parent_arg: ChiselASTELem, val state_arg: State) extends ChiselASTELem {
    val state : State = state_arg
    val openLine : String = s"when (state === FSMEnum.${state.label})\n"
    val parent : Option[ChiselASTELem] = Some(parent_arg)
    val child : Option[ArrayBuffer[ChiselASTELem]] = None
    val closeLine : String = "}"
}

class FSMCompiler {
    val ast = new ArrayBuffer[ChiselASTELem](2)
    def build(graph: FSMGraph) : ArrayBuffer[ChiselASTELem] = {
        ast.addOne(new StaticTopElem)
        val topElem = new TopElem(graph.statesTransitions._2)
        
        for (i <- 0 until graph.statesTransitions._2.length) {
            topElem.child.get.addOne(new StateElem(topElem, graph.statesTransitions._2(i)))
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
