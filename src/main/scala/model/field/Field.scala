package org.pps.functus
package model.field

import model.deck.card.Card

/** Represents a player's hand of cards on the game board. */
sealed trait Field():

  /** Returns the number of cards currently in the field. */
  def length: Int

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

final case class FieldImpl(cards: Vector[Card] = Vector.empty) extends Field:

  override def length: Int = cards.length

  override def replace(index: Int, card: Card): (Card, Field) =
    checkIndex(index)
    (cards(index), copy(cards.updated(index, card)))

  override def addCard(card: Card): Field = copy(cards :+ card)

  override def getCard(index: Int): (Card, Field) =
    checkIndex(index)
    (cards(index), copy(cards.patch(index, Nil, 1)))

  private def checkIndex(index: Int): Unit =
    if index < 0 || index >= cards.length then
      throw IndexOutOfBoundsException(s"Index $index is out of bounds for field of length ${cards.length}")
