package org.pps.functus
package model.deck.card

enum Suit:
  case Pentacles, Cups, Swords, Wands

trait Card:
  val value: Int
  val suit: Suit
