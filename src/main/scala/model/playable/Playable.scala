package org.pps.functus
package model.playable

import model.playable.turn.Action

/** Represents a state machine can execute [[Action]]s to change state and signal when its final state has been reached
  */
trait Playable[S <: Playable[S]]:
  /** Execute the given action and return the new state
    * @param action
    *   the [[Action]] to execute
    * @return
    *   the new state of the [[Playable]]
    */
  def act(action: Action): S

  /** Returns [[true]] if the [[Playable]] is in its final state */
  def isOver: Boolean

  /** Executes a sequence of [[Action]]s in order
    * @param actions
    *   the [[Action]]s to execute
    * @return
    *   the [[Playable]] after all [[Action]]s have been executed
    */
  def actAll(actions: Seq[Action]): S = actions.foldLeft(this.asInstanceOf[S])((state, action) => state.act(action))
