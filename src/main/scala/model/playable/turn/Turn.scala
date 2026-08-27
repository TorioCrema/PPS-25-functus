package org.pps.functus
package model.playable.turn

import model.deck.card.Card
import model.board.{Board, Player}
import Action.*
import model.deck.sugar.CardDSL.*
import Effects.effect
import model.playable.Playable

/** Turn class that allows to play a turn from start to finish via the [[act]] method.
  * @param hand
  *   the player's hand
  * @param board
  *   the current game board
  * @param player
  *   the player playing this turn
  * @param actions
  *   the available actions
  * @param cactus
  *   whether cactus was called
  */
case class Turn(hand: List[Card], board: Board, player: Player, actions: List[Action], cactus: Boolean)
    extends Playable[Turn]:
  private val observableCards = 2

  private def drawnFromField(index: Int) =
    val (drawn, newBoard) = board.drawPlayerCard(player, index)
    copy(hand.appended(drawn), newBoard)

  private def withActions(newActions: List[Action]): Turn =
    val filteredActions =
      if cactus then for action <- newActions if action != Cactus yield action
      else newActions
    copy(actions = filteredActions)

  private def discardHand(): Turn = copy(Nil, board.discard(hand.head))

  private def drawFromPlayer(index: Int, from: Player): Turn =
    val (drawn, newBoard) = board.drawPlayerCard(from, index)
    copy(hand.appended(drawn), newBoard)

  private def placeHandInField(fieldOwner: Player, index: Int) =
    copy(hand.tail, board.placeCardInField(hand.head, fieldOwner, Some(index)))

  /** Executes the given [[Action]] and returns the next phase of the turn
    * @param action
    *   the [[Action]] to execute
    * @return
    *   the next phase of the [[Turn]]
    */
  override def act(action: Action): Turn =
    require(actions.contains(action))
    action match
      case Observe =>
        List
          .fill(observableCards)(0)
          .foldLeft(this)((turn, index) => turn.drawnFromField(index))
          .withActions(action.next)
      case Confirm =>
        val newBoard = hand.foldRight(board)((card, b) => b.placeCardInField(card, player, Some(0)))
        Turn(Nil, newBoard, player, action.next, cactus)
      case Draw =>
        val (drawn, newBoard) = board.draw().getOrElse(throw IllegalStateException("Deck is empty during draw action"))
        Turn(drawn :: hand, newBoard, player, action.next, cactus)
      case DrawKing =>
        val (kingFromDiscard, newBoard) = board.kingTopDiscardStack()
        Turn(kingFromDiscard :: Nil, newBoard, player, action.next, cactus)
      case Activate               => hand.head.effect(this)
      case ObserveOpponent(index) => discardHand().drawFromPlayer(index, player.other).withActions(action.next)
      case ObservePlayer(index)   => discardHand().drawFromPlayer(index, player).withActions(action.next)
      case GiveBack(index)        => placeHandInField(player.other, index).withActions(action.next)
      case ReturnToField(index)   => placeHandInField(player, index).withActions(action.next)
      case ChooseReplace(index)   => copy(Nil, board.replace(player, index, hand.head), player).withActions(action.next)
      case ChooseDiscard(index)   => drawFromPlayer(index, player).withActions(action.next)
      case Discard(index)         =>
        val topOfDiscardStackValue = board.getTopDiscardStack.value
        hand.head.value match
          case `topOfDiscardStackValue` => Turn(Nil, board.discard(hand.head), player, action.next, cactus)
          case _                        =>
            val restoredBoard = board.placeCardInField(hand.head, player, Some(index))
            val (drawn, boardAfterDraw) =
              restoredBoard.draw().getOrElse(throw IllegalStateException("Deck is empty during draw action"))
            Turn(Nil, boardAfterDraw.placeCardInField(drawn, player), player, action.next, cactus)
      case Swap(playerIndex, opponentIndex) =>
        discardHand()
          .drawFromPlayer(opponentIndex, player.other)
          .drawFromPlayer(playerIndex, player)
          .placeHandInField(player, playerIndex)
          .placeHandInField(player.other, opponentIndex)
          .withActions(action.next)
      case Cactus  => Turn(hand, board, player, action.next, true)
      case EndTurn => Turn(hand, board, player, action.next, cactus)

  /** @return
    *   [[true]] if the [[Turn]] is over.
    */
  override def isOver: Boolean = this match
    case Turn(_, _, _, Nil, _) => true
    case _                     => false

/** Factory methods for the [[Turn]] class
  */
object Turns:

  object FirstTurn:
    /** Creates a [[Turn]] that allows the [[Player]] to observe the first 2 cards on their field.
      */
    def apply(board: Board, player: Player): Turn = Turn(Nil, board, player, List(Observe), false)

  object SimpleTurn:
    /** Creates a [[Turn]]
      * @param board
      *   the [[Board]] at the start of the [[Turn]]
      * @param player
      *   the [[Player]] that plays the [[Turn]]
      * @return
      *   the [[Turn]]
      */
    def apply(board: Board, player: Player): Turn =
      board.draw() match
        case None => throw IllegalStateException("There are no cards in either the deck or the discard pile")
        case _    =>
          board.discardPile.length match
            case 0 => Turn(Nil, board, player, List(Draw), false)
            case _ =>
              if board.checkKingTopDiscardStack then Turn(Nil, board, player, List(Draw, DrawKing), false)
              else
                val nextActions =
                  Draw :: (0 until board.getField(player).length).map(index => ChooseDiscard(index)).toList
                Turn(Nil, board, player, nextActions, false)

  object LastTurn:
    /** Creates the last [[Turn]] within a [[Game]], functionally the same as a [[SimpleTurn]], but the [[Cactus]]
      * action is not present.
      * @param board
      *   the [[Board]] at the start of the [[Turn]]
      * @param player
      *   the [[Player]] that plays the [[Turn]]
      * @return
      *   the [[Turn]]
      */
    def apply(board: Board, player: Player): Turn =
      SimpleTurn(board, player).copy(cactus = true)
