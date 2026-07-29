package org.pps.functus
package model.field

import model.deck.card.Card

/** Represents a player's hand of cards on the game board. */
sealed trait Field():

  /** Returns the number of cards currently in the field. */
  def length: Int

  /** Returns all cards in the field as a list. */
  def cardsList: List[Card]

  /** Removes and returns the card at the given index.
    *
    * @param index
    *   the position of the card to draw
    * @return
    *   a tuple of the drawn card and the updated field
    * @throws IndexOutOfBoundsException
    *   if the index is out of bounds
    */
  def getCard(index: Int): (Card, Field)

  /** Replaces the card at the given index with a new card.
    *
    * @param index
    *   the position of the card to replace
    * @param card
    *   the new card to place at the given index
    * @return
    *   a tuple of the replaced card and the updated field
    * @throws IndexOutOfBoundsException
    *   if the index is out of bounds
    */
  def replace(index: Int, card: Card): (Card, Field)

  /** Adds a card to the end of the field.
    *
    * @param card
    *   the card to add
    * @return
    *   the updated field
    */
  def addCard(card: Card): Field

  /** Inserts a card at the given index, shifting subsequent cards to the right.
    *
    * @param card
    *   the card to insert
    * @param index
    *   the position at which to insert the card
    * @return
    *   the updated field
    * @throws IndexOutOfBoundsException
    *   if the index is out of bounds
    */
  def addCardAtIndex(card: Card, index: Int): Field

/** A concrete implementation of [[Field]] backed by a [[Vector]] of cards.
  *
  * @param cards
  *   the internal vector holding the cards; defaults to an empty vector
  */
final case class FieldImpl(cards: Vector[Card] = Vector.empty) extends Field:

  override def length: Int = cards.length

  override def cardsList: List[Card] = cards.toList

  override def getCard(index: Int): (Card, Field) =
    checkIndex(index)
    (cards(index), copy(cards.patch(index, Nil, 1)))

  override def replace(index: Int, card: Card): (Card, Field) =
    checkIndex(index)
    (cards(index), copy(cards.updated(index, card)))

  override def addCard(card: Card): Field = copy(cards :+ card)

  override def addCardAtIndex(card: Card, index: Int): Field =
    checkIndex(index)
    val (before, after) = cards.splitAt(index)
    copy(before ++ Vector(card) ++ after)

  private def checkIndex(index: Int): Unit =
    if index < 0 || index >= cards.length then
      throw IndexOutOfBoundsException(s"Index $index is out of bounds for field of length ${cards.length}")
