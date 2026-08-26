package org.pps.functus
package model.board

import model.deck.card.Card
import model.field.Field
import model.deck.{Deck, DeckFactory, DeckImpl}

import model.deck.sugar.CardDSL.king

enum Player:
  case Player1, Player2

  def other: Player = this match
    case Player1 => Player2
    case _       => Player1

/** Board interface
  */
sealed trait Board:

  /** The current deck of cards available for drawing. */
  val deck: Deck

  /** The current fields of the players. */
  val players: Map[Player, Field]

  /** The pile of cards that have been discarded during the game. The head of the list represents the top of the pile
    * (most recently discarded card).
    */
  val discardPile: List[Card]

  /** Draws the card on top of the draw stack.
    * @return
    *   [[Some]] containing the drawn [[Card]] and the updated [[Board]], or [[None]] if the deck and discardPile are
    *   empty
    */
  def draw(): Option[(Card, Board)]

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

  /** Returns the King on top of the discard pile and the updated Board with the King removed from the discard pile.
    *
    * @return
    *   [[Right]] containing the [[Card]] and the updated [[Board]], or [[Left]] with an error message if the top of the
    *   discard pile is not a King
    */
  def kingTopDiscardStack(): Either[String, (Card, Board)]

  /** Getter for a player's field
    * @param player
    *   the player to which the field belongs to
    * @return
    *   the player's field
    */
  def getField(player: Player): Field

  /** Draws a card from a player's field
    * @param player
    *   the player to which the field belongs to
    * @param index
    *   the index to identify the card that will be drawn
    * @return
    *   the card drawn and the updated board
    */
  def drawPlayerCard(player: Player, index: Int): (Card, Board)

  /** Places a new card into a player's field
    *
    * @param card
    *   the card that will be added
    * @param player
    *   the player to which the field belongs to
    * @param index
    *   the index to identify the card that will be added
    * @return
    *   the updated board
    */
  def placeCardInField(card: Card, player: Player, index: Option[Int] = None): Board

final case class BoardImpl(
    deck: Deck = DeckFactory(),
    discardPile: List[Card] = List.empty,
    players: Map[Player, Field] = Map.empty
) extends Board:

  override def draw(): Option[(Card, BoardImpl)] =
    val checked = checkDeck()
    checked.deck.draw().map((card, remainingDeck) => (card, checked.copy(deck = remainingDeck)))

  override def discard(card: Card): BoardImpl = copy(discardPile = card :: discardPile)

  override def replace(player: Player, cardIndex: Int, card: Card): BoardImpl =
    val result: (Card, Field) = getField(player).replace(cardIndex, card)
    copy(
      players = players.updated(player, result._2),
      discardPile = result._1 :: discardPile
    )

  override def getTopDiscardStack: Card = discardPile.head

  override def kingTopDiscardStack(): Either[String, (Card, BoardImpl)] =
    Either.cond(
      checkKingTopDiscardStack,
      (getTopDiscardStack, copy(discardPile = this.discardPile.tail)),
      "Cannot replace: discard pile top element is not a king."
    )

  override def getField(player: Player): Field = players(player)

  override def drawPlayerCard(player: Player, index: Int): (Card, Board) =
    val drawnCard = getField(player).getCard(index)
    (drawnCard._1, this.copy(players = players.updated(player, drawnCard._2)))

  def placeCardInField(card: Card, player: Player, index: Option[Int]): Board =
    index match
      case Some(i) if i < players(player).length && i >= 0 =>
        copy(players = players.updated(player, players(player).addCardAtIndex(card, i)))
      case _ => copy(players = players.updated(player, players(player).addCard(card)))

  private def checkDeck(): BoardImpl =
    if this.deck.cards.isEmpty then copy(deck = DeckImpl(discardPile.toVector).shuffle(), discardPile = Nil) else this

  private def checkKingTopDiscardStack: Boolean =
    getTopDiscardStack.value == king
