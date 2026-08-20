package org.pps.functus
package model.board

import model.deck.{Deck, DeckFactory}
import model.field.{Field, FieldImpl}

/** Factory for creating [[Board]] instances in various configurations. */
object BoardFactory:

  /** Creates a new board with a shuffled deck and empty fields for all players.
    *
    * @return
    *   a [[Board]] ready to start a game
    */
  def apply(): Board =
    BoardImpl(
      deck = DeckFactory().shuffle(),
      players = Player.values.map(p => p -> FieldImpl()).toMap
    )

  /** Creates a board where each player's field is pre-populated with 4 cards drawn from a shuffled deck.
    *
    * @return
    *   a [[Board]] with 4 cards dealt to each player
    */
  def BoardWithPopulatedFields(): Board = init(Player.values.toList, 4, BoardFactory())

  /** Creates a board with custom fields for each player and an optional custom deck.
    *
    * The `players` list must contain exactly one [[Field]] per player, in the same order as [[Player.values]].
    *
    * @param players
    *   the fields to assign to each player, ordered by [[Player.ordinal]]
    * @param deck
    *   the deck to use; defaults to a new standard unshuffled deck
    * @return
    *   a [[Board]] with the given fields and deck
    * @throws IllegalArgumentException
    *   if `players.length` does not equal the number of players
    */
  def CustomBoard(players: List[Field], deck: Deck = DeckFactory()): Board =
    require(
      players.length == Player.values.length,
      s"players list must have exactly ${Player.values.length} elements, got ${players.length}"
    )
    BoardImpl(
      deck = deck,
      players = Player.values.map(p => p -> players(p.ordinal)).toMap
    )

  /** Deals `cardsPerPlayer` cards from the deck to each player's field.
    *
    * @param players
    *   the list of players to deal to
    * @param cardsPerPlayer
    *   the number of cards to deal to each player
    * @param board
    *   the initial board state
    * @return
    *   the updated board after dealing
    */
  private def init(players: List[Player], cardsPerPlayer: Int, board: Board): Board =
    players.foldLeft(board) { (board1, player) =>
      (0 until cardsPerPlayer).foldLeft(board1) { (b, index) =>
        val (card, newBoard) = b.draw
        newBoard.placeCardInField(card, player)
      }
    }
