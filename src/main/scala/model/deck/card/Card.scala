package org.pps.functus
package model.deck.card

/** Represents the four suits of an Italian deck. */
enum Suit:
  case Pentacles, Cups, Swords, Wands

/** Represents a playing card with a numeric value and a suit. */
sealed trait Card:
  /** The numeric value of the card. */
  val value: Int

  /** The suit of the card. */
  val suit: Suit

/** A concrete implementation of [[Card]].
  *
  * @param value
  *   the numeric value of the card
  * @param suit
  *   the suit of the card
  */
final case class CardImpl(value: Int, suit: Suit) extends Card
