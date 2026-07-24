package org.pps.functus
package model.board

import model.deck.{Deck, DeckFactory}
import model.field.{Field, FieldImpl}

object BoardFactory:
  def apply(): Board =
    BoardImpl(
      deck = DeckFactory().shuffle(),
      players = Player.values.map(p => p -> FieldImpl()).toMap
    )

  def BoardWithPopulatedFields(): Board = init(Player.values.toList, 4, BoardFactory())

  def CustomBoard(players: List[Field], deck: Deck = DeckFactory()): Board =
    require(
      players.length == Player.values.length,
      s"players list must have exactly ${Player.values.length} elements, got ${players.length}"
    )
    BoardImpl(
      deck = deck,
      players = Player.values.map(p => p -> players(p.ordinal)).toMap
    )

  private def init(players: List[Player], cardsPerPlayer: Int, board: Board): Board =
    players.foldLeft(board) { (board1, player) =>
      (0 until cardsPerPlayer).foldLeft(board1) { (b, index) =>
        val (card, newBoard) = b.draw()
        newBoard.replace(player, index, card)
      }
    }
