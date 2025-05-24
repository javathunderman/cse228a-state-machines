// See README.md for license details.

package fsm

import chisel3._
import scala.io.Source
import scala.util.matching.Regex
case class State(name: String, label: String)
case class Transition(source: State, dest: State, label: String)
class FSM(val filePath: String) { // extends Module
  val stateRegExp : Regex = raw"\s*(.*)\s*\[label\s\=\s\"(.*)\"\]\;".r
  val transitionRegExp : Regex = raw"\s*(.*)\-\>\s*(.*)\s*\[label\s\=\s\"(.*)\"\]\;".r
  val statesTransitions : (Seq[Transition], Seq[State]) = Source.fromFile(filePath).getLines().foldLeft((Seq.empty[Transition], Seq.empty[State]))
  { 
    case ((accTransitions, accStates), line) => 
      if (line contains "->") {
        line match {
          case transitionRegExp(sourceStateStr, destStateStr, transitionLabel) => {
            val sourceDestStateTuple : (Option[State], Option[State]) = accStates.foldLeft((Option.empty[State], Option.empty[State])) {
              case ((foundSrcState, foundDstState), currState) => {
                if ((currState.name == sourceStateStr) && (currState.name == destStateStr)) {
                  (Some(currState), Some(currState))
                } else if (currState.name == sourceStateStr) {
                  (Some(currState), foundDstState)
                } else if (currState.name == destStateStr) {
                  (foundSrcState, Some(currState))
                } else {
                  (foundSrcState, foundDstState)
                }
              }
            }
            (accTransitions :+ new Transition(sourceDestStateTuple._1.get, sourceDestStateTuple._2.get, transitionLabel), accStates)
          }
          case _ => (accTransitions, accStates)
        }
      } else {
          line match {
            case stateRegExp(name, label) => (accTransitions, accStates :+ new State(name, label))
            case _ => (accTransitions, accStates)
          }
      }
  }
}
