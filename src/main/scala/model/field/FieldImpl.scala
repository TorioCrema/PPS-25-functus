package org.pps.functus
package model.field

import model.deck.card.Card

case class FieldImpl(private val cards: Vector[Card] = Vector.empty) extends Field:

  override def length: Int = cards.length

  override def replace(index: Int, card: Card): (Card, Field) =
    checkIndex(index)
    (cards(index), copy(cards.updated(index, card)))

  override def addCard(card: Card): Field = copy(cards :+ card)

  override def getCard(index: Int): (Card, Field) =
    checkIndex(index)
    (cards(index), copy(cards.drop(index)))

  private def checkIndex(index: Int): Unit =
    if index < 0 || index >= cards.length then
      throw IndexOutOfBoundsException(s"Index $index is out of bounds for field of length ${cards.length}")
