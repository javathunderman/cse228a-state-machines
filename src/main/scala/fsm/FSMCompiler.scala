package fsm

import java.io.{File, FileWriter, Writer}

import scala.collection.mutable
class FSMCompiler {
    def writeLines(lines: Seq[String], filePath: os.Path) = {
        os.write.append(filePath, lines)
    }
    def writeLine(line: String, filePath: os.Path) = {
        os.write.append(filePath, line)
    }
    def prelimGeneration(filePath: os.Path) = {
        writeLine("package fsm\nimport chisel3._\nimport chisel3.util\nimport scala.io.Source\nimport scala.util.matching.Regex\nclass FSM(val fsm_graph: FSMGraph) extends Module {\n", filePath)
    }
    // todo: build tree of program to make the generation less hacky
}
