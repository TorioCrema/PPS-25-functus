package org.pps.functus
package model.field

import model.deck.card.Card

case class FieldImpl() extends Field:
  override def length: Int = ???

  override def replace(index: Int, card: Card): (Card, Field) = ???

  override def addCard(card: Card): Field = ???

  override def getCard(index: Int): (Card, Field) = ???
