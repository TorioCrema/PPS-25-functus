package org.pps.functus
package model.deck.card

import model.deck.card.{Card, Suit}

final case class CardImpl(value: Int, suit: Suit) extends Card:
  override def toString: String = s"$value $suit"
