package org.pps.functus
package model.board

import model.deck.card.Card
import model.field.Field
import model.deck.{Deck, DeckFactory, DeckImpl}

enum Player:
  case Player1, Player2

/** Board interface
  */
sealed trait Board:

  /** Draws the card on top of the draw stack.
    * @return
    *   the updated board
    */
  def draw(): (Card, Board)

  /** Discards a card on top of the discard stack.
    * @param card
    *   the card to discard
    * @return
    *   the updated board
    */
  def discard(card: Card): Board

  /** Replaces a card on the player's field with a given card, then discards the replaced card.
    * @param player
    *   the player that is replacing the card
    * @param cardIndex
    *   index used to identify the card that will be replaced
    * @param card
    *   the card that will replace the old one
    * @return
    *   the updated board
    */
  def replace(player: Player, cardIndex: Int, card: Card): Board

  /** Getter for the card on top of the discard stack.
    * @return
    *   the card on top of the discard stack
    */
  def getTopDiscardStack: Card

  /** Replaces the selected card in the player's field with the King on top of the discard pile, removing the King from
    * the discard pile. This method can only be used when the top card of the discard pile is a King.
    *
    * @param player
    *   the player that is replacing the card
    * @param cardIndex
    *   index used to identify the card that will be replaced
    * @return
    *   the updated board
    */
  def kingTopDiscardStack(player: Player, cardIndex: Int): Board

  /** Getter for a player's field
    * @param player
    *   the player to which the field belongs to
    * @return
    *   the player's field
    */
  def getField(player: Player): Field

final case class BoardImpl(
    deck: Deck = DeckFactory(),
    discardPile: List[Card] = List.empty,
    players: Map[Player, Field] = Map.empty
) extends Board:

  override def draw(): (Card, BoardImpl) =
    checkDeckAndDiscardPile()
    val checked = checkDeck()
    val draw = checked.deck.draw()
    (draw._1, checked.copy(deck = draw._2))

  override def discard(card: Card): BoardImpl = copy(discardPile = card :: discardPile)

  override def replace(player: Player, cardIndex: Int, card: Card): BoardImpl =
    val result: (Card, Field) = getField(player).replace(cardIndex, card)
    copy(
      players = players.updated(player, result._2),
      discardPile = result._1 :: discardPile
    )

  override def getTopDiscardStack: Card = discardPile.head

  override def kingTopDiscardStack(player: Player, cardIndex: Int): BoardImpl =
    checkKingTopDiscardStack()
    val king = getTopDiscardStack
    copy(discardPile = this.discardPile.tail).replace(player, cardIndex, king)

  override def getField(player: Player): Field = players(player)

  private def checkDeck(): BoardImpl =
    if this.deck.cards.isEmpty then copy(deck = DeckImpl(discardPile.toVector).shuffle(), discardPile = Nil) else this

  private def checkDeckAndDiscardPile(): Unit =
    if deck.cards.isEmpty && discardPile.isEmpty then
      throw IllegalStateException("Cannot draw: deck and discard pile are both empty.")

  private def checkKingTopDiscardStack(): Unit =
    if getTopDiscardStack.value != 0 then
      throw IllegalStateException("Cannot replace: discard pile top element is not a king.")
