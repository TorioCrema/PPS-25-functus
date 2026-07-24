package org.pps.functus
package model.deck.card

enum Suit:
  case Pentacles, Cups, Swords, Wands

sealed trait Card:
  val value: Int
  val suit: Suit

final case class CardImpl(value: Int, suit: Suit) extends Card
