// See README.md for license details.

package fsm

import chisel3._
import scala.io.Source
import scala.util.matching.Regex
case class State(name: String, label: String) {
  val transitions = Seq.empty[(String, State)]
}
class FSM(val filePath: String) { // extends Module
  val stateRegExp : Regex = raw"\s*(.*)\s*\[label\s\=\s\"(.*)\"\]\;".r
  val states : Seq[State] = Source.fromFile(filePath).getLines().foldLeft(Seq.empty[State])
  { 
    case (acc, line) => 
      if (line contains "->") {
        acc
      } else {
          line match {
            case stateRegExp(name, label) => acc :+ new State(name, label)
            case _ => acc
          }
      }
  }
}
