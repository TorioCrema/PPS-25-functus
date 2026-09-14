package org.pps.functus
package utils

import model.playable.turn.Action


private val TO_BE_SELECTED = -1

case class ViewAction(id: String, label: String)

/** Maps an individual model action ([[Action]]) to its corresponding UI view representation ([[ViewAction]]) with a
 * user-friendly display label.
 *
 * @param action
 *   the [[Action]] to be translated into a view component
 * @return
 *   the resulting [[ViewAction]] containing the action identifier and string label
 */
def mapSingleAction(action: Action): ViewAction = action match
  case Action.Observe  => ViewAction("observe", "Peek at the first two cards")
  case Action.Confirm  => ViewAction("confirm", "Confirm and cover")
  case Action.Draw     => ViewAction("draw", "Draw from deck")
  case Action.DrawKing => ViewAction("draw_king", "Take King from discard pile")
  case Action.Activate => ViewAction("activate", "Use card effect or replace")
  case Action.EndTurn            => ViewAction("end_turn", "End turn")
  case Action.Cactus             => ViewAction("cactus", "Call Cactus!")
  case Action.ChooseDiscard(i)   => ViewAction(s"discard_$i", s"Discard card in position ${i + 1}")
  case Action.ChooseReplace(i)   => ViewAction(s"replace_$i", s"Replace card in position ${i + 1}")
  case Action.Discard(i)         => ViewAction(s"discard_$i", s"Discard card in position ${i + 1}")
  case Action.ObserveOpponent(i) => ViewAction(s"obs_opp_$i", s"Peek at opponent card in position ${i + 1}")
  case Action.GiveBack(i)        => ViewAction(s"give_back_$i", s"Return card to opponent in position ${i + 1}")
  case Action.ObservePlayer(i)   => ViewAction(s"obs_player_$i", s"Peek at your card in position ${i + 1}")
  case Action.ReturnToField(i)   => ViewAction(s"return_$i", s"Return card to your field in position ${i + 1}")
  case Action.Swap(pIdx, oIdx)   =>
    ViewAction(
      s"swap_${pIdx}_$oIdx",
      s"Swap your card in position ${pIdx + 1} with opponent's card in position ${oIdx + 1}"
    )

/** Grouping logic for board-selection actions: If actions like [[Action.ChooseReplace]], [[Action.ChooseDiscard]],
 * [[Action.ObserveOpponent]], [[Action.ObservePlayer]], [[Action.Swap]] are present, groups them into a single
 * option. prepending them alongside the other available actions.
 *
 * @param actions
 *   the list of [[Action]] available in the current turn step
 * @return
 *   a tuple containing the filtered/mapped [[Action]] and their corresponding [[ViewAction]]
 */
def prepareActions(actions: List[Action]): (List[Action], List[ViewAction]) =
  val hasObserveOpponent = actions.exists { case Action.ObserveOpponent(_) => true; case _ => false }
  val hasObservePlayer = actions.exists { case Action.ObservePlayer(_) => true; case _ => false }
  val hasSwap = actions.exists { case Action.Swap(_, _) => true; case _ => false }
  val hasReplace = actions.exists { case Action.ChooseReplace(_) => true; case _ => false }
  val hasChooseDiscard = actions.exists { case Action.ChooseDiscard(_) => true; case _ => false }

  if hasObservePlayer then
    val otherActions = actions.filterNot { case Action.ObservePlayer(_) => true; case _ => false }
    val (otherModel, otherView) = prepareActions(otherActions)
    (
      Action.ObservePlayer(TO_BE_SELECTED) :: otherModel,
      ViewAction("use_effect_player", "Use card effect (Peek at your card)") :: otherView
    )
  else if hasObserveOpponent then
    val otherActions = actions.filterNot { case Action.ObserveOpponent(_) => true; case _ => false }
    val (otherModel, otherView) = prepareActions(otherActions)
    (
      Action.ObserveOpponent(TO_BE_SELECTED) :: otherModel,
      ViewAction("use_effect_opp", "Use card effect (Peek at opponent card)") :: otherView
    )
  else if hasSwap then
    val otherActions = actions.filterNot { case Action.Swap(_, _) => true; case _ => false }
    val (otherModel, otherView) = prepareActions(otherActions)
    (
      Action.Swap(TO_BE_SELECTED, TO_BE_SELECTED) :: otherModel,
      ViewAction("use_effect_swap", "Use card effect (Swap cards)") :: otherView
    )
  else if hasReplace then
    val otherActions = actions.filterNot { case Action.ChooseReplace(_) => true; case _ => false }
    val (otherModel, otherView) = prepareActions(otherActions)
    (
      Action.ChooseReplace(TO_BE_SELECTED) :: otherModel,
      ViewAction("select_replace", "Swap drawn card with a board card") :: otherView
    )
  else if hasChooseDiscard then
    val otherActions = actions.filterNot { case Action.ChooseDiscard(_) => true; case _ => false }
    val (otherModel, otherView) = prepareActions(otherActions)
    (
      Action.ChooseDiscard(TO_BE_SELECTED) :: otherModel,
      ViewAction("select_discard", "Discard matching card from board") :: otherView
    )
  else (actions, actions.map(utils.mapSingleAction))

  

    