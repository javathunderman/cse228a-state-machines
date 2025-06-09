// See README.md for license details.

package fsm

import chisel3._
import chisel3.util
import scala.io.Source
import scala.util.matching.Regex
import scala.collection.mutable.{HashSet, HashMap, Queue}
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
  val entry_states = HashSet.empty[State]
  val final_states = HashSet.empty[State]
  val adj_list_map = HashMap.empty[State, Seq[(String, State)]]

  val stateRegExp : Regex = raw"\s*(S\d+)\s?\[label\s?\=\s?\"(.*)\"\]\;".r.unanchored
  val transitionRegExp : Regex = raw"\s*(S.*)\s?\-\>\s?(.*)\s*\[label\s?\=\s?\"(.*)\"\]\;".r.unanchored
  val statesTransitions : (Seq[Transition], Seq[State]) = Source.fromFile(filePath).getLines().foldLeft((Seq.empty[Transition], Seq.empty[State]))
  { 
    case ((accTransitions, accStates), line) => 
      if (line contains "->") {
        line match {
          case transitionRegExp(sourceStateStr, destStateStr, transitionLabel) => {
            val sourceDestStateTuple : (Option[State], Option[State]) = accStates.foldLeft((Option.empty[State], Option.empty[State])) {
              case ((foundSrcState, foundDstState), currState) => {
                if ((currState.name == sourceStateStr.strip()) && (currState.name == destStateStr.strip())) {
                  (Some(currState), Some(currState))
                } else if (currState.name == sourceStateStr.strip()) {
                  (Some(currState), foundDstState)
                } else if (currState.name == destStateStr.strip()) {
                  (foundSrcState, Some(currState))
                } else {
                  (foundSrcState, foundDstState)
                }
              }
            }
            if (reservedKeywords.find(_ == transitionLabel) == None && transitionLabel != " ") {
              (accTransitions :+ new Transition(sourceDestStateTuple._1.get, sourceDestStateTuple._2.get, transitionLabel.replace(" ", "")), accStates)
            } else {
              // try to correct a transition name if it's a reserved keyword in scala
              println("illegal transition name")
              if (transitionLabel == " ") {
                (accTransitions :+ new Transition(sourceDestStateTuple._1.get, sourceDestStateTuple._2.get, s"${sourceDestStateTuple._1.get.name}to${sourceDestStateTuple._2.get.name}"), accStates)
              } else {
                (accTransitions :+ new Transition(sourceDestStateTuple._1.get, sourceDestStateTuple._2.get, transitionLabel.replace(" ", "") + "_"), accStates)
              }
            }
            
          }
          case _ => (accTransitions, accStates)
        }
      } else {
          line match {
            case stateRegExp(name, label) => {
              (accTransitions, accStates :+ new State(name.strip(), 
                if (reservedKeywords.find(_ == label) == None) 
                  label.replace(" ", "")
                else 
                  label.replace(" ", "") + "_", 
                if (label.toLowerCase().contains("final"))
                  StateType.EndState
                else if (label.toLowerCase().contains("entry"))
                  StateType.EntryState
                else
                  StateType.StandardState
              ))
            }
            case _ => (accTransitions, accStates)
          }
      }
  }
  def build_adj_list() = {
    final_states.addAll(statesTransitions._2.filter(x => x.state_type == StateType.EndState))
    entry_states.addAll(statesTransitions._2.filter(x => x.state_type == StateType.EntryState))
    
    for (i <- 0 until statesTransitions._2.length) {
      adj_list_map(statesTransitions._2(i)) = statesTransitions._1.filter(x => x.source == statesTransitions._2(i)).foldLeft(Seq.empty[(String, State)])
      {
        case (acc, x) => acc :+ (x.label, x.dest)
      }
    }
    adj_list_map
  }

  def bfs(adj_list_map: HashMap[State, Seq[(String, State)]], start: Option[State], dest: Option[State]) : Option[Either[HashSet[State], HashMap[State, (String, State)]]] = {
    val visited = HashSet.empty[State]
    val pred = HashMap.empty[State, (String, State)]
    if (adj_list_map.size == 0) {
      println("Need to populate graph first")
      None
    } else {
      val queue = Queue.empty[State]
      if (start == None) {
        queue.addOne(statesTransitions._2(0))
        visited.addOne(statesTransitions._2(0))
      } else {
        queue.addOne(start.get)
        visited.addOne(start.get)
      }
      while (queue.size != 0) {
        val u = queue.dequeue()
        for (v <- adj_list_map(u)) {
          if (!(visited.contains(v._2))) {
            visited.addOne(v._2)
            queue.addOne(v._2)
            pred.addOne(v._2, (v._1, u))
            if (dest != None && dest.get == v._2) {
              return Some(Right(pred))
            }
          }
        }
      }
      if (dest == None) {
        Some(Left(visited))
      } else {
        None
      }
    }
  }

  def reachability_bfs(adj_list_map: HashMap[State, Seq[(String, State)]]) = {
    val visited_states = entry_states.foldLeft(HashSet.empty[State]){ case(acc, x) => {
      val bfs_initial = bfs(adj_list_map, Some(x), None).get
      bfs_initial match {
        case Left(visited) => acc.addAll(visited)
        case Right(predecessors) => acc
      }
    }}
    statesTransitions._2.toSet.diff(visited_states)
  }

  def path_bfs(adj_list_map: HashMap[State, Seq[(String, State)]]) = {
    val paths = HashMap.empty[(State, State), Seq[(String, State)]]
    for (entry_state <- entry_states) {
      for (end_state <- final_states) {
        val path_se = bfs(adj_list_map, Some(entry_state), Some(end_state))
        path_se match {
          case None => println(s"no path between ${entry_state} and ${end_state}")
          case Some(Left(visited)) => println("error while obtaining paths?")
          case Some(Right(pred)) => {
            var path = Seq.empty[(String, State)]
            var current_state = end_state
            while (current_state != entry_state) {
              path = path :+ pred(current_state)
              current_state = pred(current_state)._2
            }
            paths((entry_state, end_state)) = path.reverse
          }
        }
      }
    }
    paths
  }

  def dead_state_detection(adj_list_map: HashMap[State, Seq[(String, State)]]) : Set[State] = {
    val nonFinalStates = statesTransitions._2.toSet.diff(final_states) // get all the non-final states we could be stuck in
    val deadStates = nonFinalStates.foldLeft(Set.empty[State]){
      case (acc, x) => {
        if (adj_list_map(x).length == 0) { // no out-edges
          acc + x
        } else {
          acc
        }
      }
    }
    deadStates.diff(reachability_bfs(adj_list_map))
  }
}
