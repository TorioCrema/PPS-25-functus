package org.pps.functus
package model.board

import model.deck.DeckFactory
import model.field.FieldImpl

object BoardFactory:
  def apply(): Board =
    BoardImpl(
      deck = DeckFactory().shuffle(),
      players = Player.values.map(p => p -> FieldImpl()).toMap
    )

  def BoardWithPopulatedFields(): Board = init(Player.values.toList, 4, BoardFactory())

  private def init(players: List[Player], cardsPerPlayer: Int, board: Board): Board =
    players.foldLeft(board) { (board1, player) =>
      (0 until cardsPerPlayer).foldLeft(board1) { (b, index) =>
        val (card, newBoard) = b.draw()
        newBoard.replace(player, index, card)
      }
    }
