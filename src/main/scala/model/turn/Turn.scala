package org.pps.functus
package model.turn

import model.deck.card.Card
import model.board.{Board, BoardImpl, Player}
import model.turn.Action.{Confirm, Observe}
import model.deck.sugar.FieldDSL.*
import model.deck.sugar.FieldDSL.given

import model.field.FieldImpl

case class Turn(hand: List[Card], board: Board, player: Player, actions: List[Action]):
  def act(action: Action): Turn = action match
    case Observe =>
      List(0, 0).foldLeft(this)((turn, index) => turn.drawnFromField(player, index)).withActions(Observe.next)
//    case Confirm =>
//      val fieldFromHand = hand.foldLeft(emptyField())((field, card) => field and card)
//      val fieldFromBoard = board.getField(player) match
//        case FieldImpl(cards) => cards
//      val newField = fieldFromBoard.foldLeft(fieldFromHand)((field, card) => field and card)
  private def drawnFromField(player: Player, index: Int) =
    val (drawn, newBoard) = board.drawPlayerCard(player, index)
    Turn(hand.appended(drawn), newBoard, player, actions)

  private def withActions(newActions: List[Action]): Turn = Turn(hand, board, player, newActions)

object Turns:
  object FirstTurn:
    def apply(board: Board, player: Player): Turn = Turn(Nil, board, player, List(Observe))
