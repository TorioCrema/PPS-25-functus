package org.pps.functus
package model.turn

import model.deck.card.Card
import model.board.{Board, Player}
import model.turn.Action.*
import model.deck.sugar.CardDSL.*

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
case class Turn(hand: List[Card], board: Board, player: Player, actions: List[Action], cactus: Boolean = false):
  private val observableCards = 2

  private def drawnFromField(index: Int) =
    val (drawn, newBoard) = board.drawPlayerCard(player, index)
    Turn(hand.appended(drawn), newBoard, player, actions)

  private def withActions(newActions: List[Action]): Turn = Turn(hand, board, player, newActions)

  /** Executes the given [[Action]] and returns the next phase of the turn
    * @param action
    *   the [[Action]] to execute
    * @return
    *   the next phase of the [[Turn]]
    */
  def act(action: Action): Turn =
    require(actions.contains(action))
    action match
      case Observe =>
        List
          .fill(observableCards)(0)
          .foldLeft(this)((turn, index) => turn.drawnFromField(index))
          .withActions(action.next)
      case Confirm =>
        val newBoard = hand.foldRight(board)((card, b) => b.placeCardInField(card, player, Option(0)))
        Turn(Nil, newBoard, player, action.next)
      case Draw =>
        val (drawn, newBoard) = board.draw().getOrElse(throw IllegalStateException("Deck is empty during draw action"))
        Turn(drawn :: hand, newBoard, player, action.next)
      case DrawKing =>
        board.kingTopDiscardStack() match
          case Right(drawnKing, newBoard) => Turn(drawnKing :: Nil, newBoard, player, action.next)
          case Left(kingNotOnTop)         => throw IllegalStateException(kingNotOnTop)
      case Activate => Turn(hand, board, player, (0 until board.getField(player).length).map(ChooseReplace(_)).toList)
      case ChooseReplace(index) =>
        Turn(Nil, board.replace(player, index, hand.head), player, action.next)
      case ChooseDiscard(index) =>
        val (chosen, _) = board.getField(player).getCard(index)
        if chosen.value == board.getTopDiscardStack.value then
          Turn(Nil, board.discard(chosen).drawPlayerCard(player, index)._2, player, action.next)
        else
          val (drawn, boardAfterDraw) =
            board.draw().getOrElse(throw IllegalStateException("Deck is empty during draw action"))
          val newBoard = boardAfterDraw.placeCardInField(drawn, player, Option.empty)
          Turn(Nil, newBoard, player, action.next)
      case EndTurn => Turn(hand, board, player, EndTurn.next, cactus)
      case Cactus  => Turn(hand, board, player, Cactus.next, true)

  /** @return
    *   [[true]] if the [[Turn]] is over.
    */
  def isOver: Boolean = this match
    case Turn(_, _, _, Nil, _) => true
    case _                     => false

/** Factory methods for the [[Turn]] class
  */
object Turns:

  object FirstTurn:
    /** Creates a [[Turn]] that allows the [[Player]] to observe cards on his field.
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
        case None => throw IllegalStateException("Cio madonna puTtana sono finite le carte amooo")
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
