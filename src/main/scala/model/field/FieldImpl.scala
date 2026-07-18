package org.pps.functus
package model.field

import model.deck.card.Card

case class FieldImpl(private val cards: Vector[Card] = Vector.empty) extends Field:

  override def length: Int = cards.length

  override def replace(index: Int, card: Card): (Card, Field) =
    if index < 0 || index >= cards.length then throw IndexOutOfBoundsException()
    val old = cards(index)
    (old, copy(cards.updated(index, card)))

  override def addCard(card: Card): Field = copy(cards :+ card)

  override def getCard(index: Int): (Card, Field) =
    if index < 0 || index >= cards.length then throw IndexOutOfBoundsException()
    (cards(index), copy(cards.drop(index)))
