package org.pps.functus
package model.playable.turn

import model.playable.turn.Turns.*
import model.playable.turn.Effects.effect

enum Action:
  /** Action used to observe cards on the player's field.
    */
  case Observe

  /** Action that is used to restore the board after [[Observe]].
    */
  case Confirm

  /** Ends the turn. */
  case EndTurn

  /** Draws from the deck to the player's hand. */
  case Draw

  /** Activates the card in the player's hand. */
  case Activate

  /** Calls Cactus */
  case Cactus

  /** Draws the king from the top of the discard pile. */
  case DrawKing

  /** Picks the card that will be drawn from the player's field and then discarded */
  case ChooseDiscard(index: Int)

  /** Discards the card from the player's hand, the index indicates where to place the card in case it wasn't the
    * correct value
    */
  case Discard(index: Int)

  /** Picks a card to replace with the card in the player's hand. */
  case ChooseReplace(index: Int)

  /** Picks which card to observe from the opponent's field. */
  case ObserveOpponent(index: Int)

  /** Returns the card drawn from the opponent's field to its original place. */
  case GiveBack(index: Int)

  /** Picks which card to observe from the player's field. */
  case ObservePlayer(index: Int)

  /** Returns the card to the player's field at the index. */
  case ReturnToField(index: Int)

  /** Swaps cards between the two player fields. */
  case Swap(playerIndex: Int, opponentIndex: Int)

  /** Returns the list of available actions after the current one. */
  def nextActions(using Option[Turn]): List[Action] = this match
    case Observe          => List(Confirm)
    case Confirm | Cactus => List(EndTurn)
    case Draw | DrawKing  => List(Activate)
    case Activate         =>
      require(summon[Option[Turn]].nonEmpty)
      val currentTurn = summon[Option[Turn]].get
      currentTurn.hand.head.effect(currentTurn)
    case ChooseReplace(_) | GiveBack(_) | ReturnToField(_) | Swap(_, _) => List(Cactus, EndTurn)
    case ChooseDiscard(index)                                           => List(Discard(index))
    case Discard(_)                                                     => List(Draw)
    case ObserveOpponent(index)                                         => List(GiveBack(index))
    case ObservePlayer(index)                                           => List(ReturnToField(index))
    case _                                                              => Nil

  /** Returns the next [[Turn]] after executing the [[Action]]
    * @param currentTurn
    *   the [[Turn]] to execute the [[Action]] on.
    * @return
    *   the next [[Turn]]
    */
  def nextTurn(currentTurn: Turn): Turn =
    given Option[Turn] = Some(currentTurn)
    execute(currentTurn).filterActions(nextActions)

  private def execute(currentTurn: Turn): Turn =
    this match
      case Observe                => currentTurn.drawnFromField(0).drawnFromField(0)
      case Confirm                => currentTurn.returnObservedCards
      case Draw                   => currentTurn.drawFromDeck
      case DrawKing               => currentTurn.drawFromPile
      case Activate               => currentTurn
      case ObserveOpponent(index) => currentTurn.discardWithoutPenalty.drawFromPlayer(index, currentTurn.player.other)
      case ObservePlayer(index)   => currentTurn.discardWithoutPenalty.drawFromPlayer(index, currentTurn.player)
      case GiveBack(index)        => currentTurn.placeHandInField(currentTurn.player.other, index)
      case ReturnToField(index)   => currentTurn.placeHandInField(currentTurn.player, index)
      case ChooseReplace(index)   => currentTurn.replaceHandIntoField(index)
      case ChooseDiscard(index)   => currentTurn.drawFromPlayer(index, currentTurn.player)
      case Discard(index)         => currentTurn.discardHand(index)
      case Swap(playerIndex, opponentIndex) => currentTurn.swapWithOpponent(playerIndex, opponentIndex)
      case Cactus                           => currentTurn.callCactus
      case EndTurn                          => currentTurn
