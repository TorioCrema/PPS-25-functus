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
case class Turn(hand: List[Card], board: Board, player: Player, actions: List[Action], cactus: Boolean = false)
    extends Playable[Turn]:
  private val observableCards = 2

  private def drawnFromField(index: Int) =
    val (drawn, newBoard) = board.drawPlayerCard(player, index)
    Turn(hand.appended(drawn), newBoard, player, actions)

  private def withActions(newActions: List[Action]): Turn = Turn(hand, board, player, newActions)

  private def discardHand(): Turn = Turn(Nil, board.discard(hand.head), player, actions)

  private def drawFromPlayer(index: Int, from: Player): Turn =
    val (drawn, newBoard) = board.drawPlayerCard(from, index)
    Turn(hand.appended(drawn), newBoard, player, actions)

  private def placeHandInField(fieldOwner: Player, index: Int) =
    Turn(hand.tail, board.placeCardInField(hand.head, fieldOwner, Some(index)), player, actions)

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
        Turn(Nil, newBoard, player, action.next)
      case Draw =>
        val (drawn, newBoard) = board.draw().getOrElse(throw IllegalStateException("Deck is empty during draw action"))
        Turn(drawn :: hand, newBoard, player, action.next)
      case DrawKing =>
        board.kingTopDiscardStack() match
          case Right(drawnKing, newBoard) => Turn(drawnKing :: Nil, newBoard, player, action.next)
          case Left(kingNotOnTop)         => throw IllegalStateException(kingNotOnTop)
      case Activate               => hand.head.effect(this)
      case ObserveOpponent(index) => discardHand().drawFromPlayer(index, player.other).withActions(action.next)
      case ObservePlayer(index)   => discardHand().drawFromPlayer(index, player).withActions(action.next)
      case GiveBack(index)        => placeHandInField(player.other, index).withActions(action.next)
      case ReturnToField(index)   => placeHandInField(player, index).withActions(action.next)
      case ChooseReplace(index)   => Turn(Nil, board.replace(player, index, hand.head), player, action.next)
      case ChooseDiscard(index)   => drawFromPlayer(index, player).withActions(action.next)
      case Discard(index)         =>
        val topOfDiscardStackValue = board.getTopDiscardStack.value
        hand.head.value match
          case `topOfDiscardStackValue` => Turn(Nil, board.discard(hand.head), player, action.next)
          case _                        =>
            val restoredBoard = board.placeCardInField(hand.head, player, Some(index))
            val (drawn, boardAfterDraw) =
              restoredBoard.draw().getOrElse(throw IllegalStateException("Deck is empty during draw action"))
            Turn(Nil, boardAfterDraw.placeCardInField(drawn, player), player, action.next)
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
    def apply(board: Board, player: Player): Turn = Turn(Nil, board, player, List(Observe))

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
            case 0 => Turn(Nil, board, player, List(Draw))
            case _ =>
              board.getTopDiscardStack.value match
                case `king` => Turn(Nil, board, player, List(Draw, DrawKing))
                case _      =>
                  val nextActions =
                    Draw :: (0 until board.getField(player).length).map(index => ChooseDiscard(index)).toList
                  Turn(Nil, board, player, nextActions)
