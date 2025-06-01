// See README.md for license details.

package fsm

import chisel3._
import chisel3.util
import scala.io.Source
import scala.util.matching.Regex
object StateType extends Enumeration {
  type StateType = Value
  val EntryState, EndState, StandardState = Value
}
import StateType._

case class State(name: String, label: String, state_type: StateType)
case class Transition(source: State, dest: State, label: String)
case class FSMGraph(val filePath: String) {
  val st = scala.reflect.runtime.universe.asInstanceOf[scala.reflect.internal.SymbolTable]
  val reservedKeywords = st.nme.keywords.map(x => x.toString())

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
            if (reservedKeywords.find(_ == transitionLabel) == None) {
              (accTransitions :+ new Transition(sourceDestStateTuple._1.get, sourceDestStateTuple._2.get, transitionLabel), accStates)
            } else {
              // try to correct a transition name if it's a reserved keyword in scala
              println("illegal transition name")
              (accTransitions :+ new Transition(sourceDestStateTuple._1.get, sourceDestStateTuple._2.get, transitionLabel + "_"), accStates)
            }
            
          }
          case _ => (accTransitions, accStates)
        }
      } else {
          line match {
            case stateRegExp(name, label) => {
              (accTransitions, accStates :+ new State(name, 
                if (reservedKeywords.find(_ == label) == None) 
                  label 
                else 
                  label + "_", 
                label.toLowerCase() match {
                  case "final" => StateType.EndState
                  case "entry" => StateType.EntryState
                  case _ => StateType.StandardState
                }
              ))
            }
            case _ => (accTransitions, accStates)
          }
      }
  }
  def build_adj_list() = {
    val adj_list_map = collection.mutable.HashMap[State, Seq[(String, State)]]()
    for (i <- 0 until statesTransitions._2.length) {
      adj_list_map(statesTransitions._2(i)) = statesTransitions._1.filter(x => x.source == statesTransitions._2(i)).foldLeft(Seq.empty[(String, State)])
      {
        case (acc, x) => acc :+ (x.label, x.dest)
      }
    }
    adj_list_map
  }

  def reachability_bfs(adj_list_map: collection.mutable.HashMap[State, Seq[(String, State)]]) : Set[State] = {
    val visited = collection.mutable.Set.empty[State]
    if (adj_list_map.size == 0) {
      println("Need to populate graph first")
      visited.toSet
    } else {
      val queue = collection.mutable.Queue.empty[State]
      queue.addOne(statesTransitions._2(0))
      visited.addOne(statesTransitions._2(0)) 
      while (queue.size != 0) {
        val u = queue.dequeue()
        for (v <- adj_list_map(u)) {
          if (!(visited.contains(v._2))) {
            visited.addOne(v._2)
            queue.addOne(v._2)
          }
        }
      }
      statesTransitions._2.toSet.diff(visited.toSet)
    }
  }
}
