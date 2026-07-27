package org.pps.functus
package model.turn

import model.deck.card.Card
import model.board.{Board, Player}
import model.turn.Action.*
import model.deck.sugar.FieldDSL.*
import model.deck.sugar.FieldDSL.given
import model.deck.sugar.CardDSL.*

case class Turn(hand: List[Card], board: Board, player: Player, actions: List[Action]):
  def act(action: Action): Turn = action match
    case Observe =>
      List(0, 0).foldLeft(this)((turn, index) => turn.drawnFromField(index)).withActions(Observe.next)
    case Confirm =>
      val newBoard = hand.foldLeft(board)((b, card) => b.placeCardInField(card, player, Option.empty))
      Turn(Nil, newBoard, player, Confirm.next)
  private def drawnFromField(index: Int) =
    val (drawn, newBoard) = board.drawPlayerCard(player, index)
    Turn(hand.appended(drawn), newBoard, player, actions)

  private def withActions(newActions: List[Action]): Turn = Turn(hand, board, player, newActions)

object Turns:
  object FirstTurn:
    def apply(board: Board, player: Player): Turn = Turn(Nil, board, player, List(Observe))

  object SimpleTurn:
    def apply(board: Board, player: Player): Turn = board.discardPile.length match
      case 0 => Turn(Nil, board, player, List(Draw))
      case _ =>
        board.getTopDiscardStack.value match
          case `king` => Turn(Nil, board, player, List(Draw, DrawKing))
          case _      => Turn(Nil, board, player, List(Draw, Discard(Option.empty)))
