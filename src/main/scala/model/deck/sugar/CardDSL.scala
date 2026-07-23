package org.pps.functus
package model.deck.sugar

import model.deck.card.{Card, CardImpl, Suit}

/** Allows to create [[Card]]s with sugar syntax.
  */
object CardDSL:
  /** Enables `king` to be used instead of the king card's value
    */
  val king = 0

  /** Enables `ace` to be used instead of the ace card's value
    */
  val ace = 1

  /** Enables `two` to be used instead of the two card's value
    */
  val two = 2

  /** Enables `three` to be used instead of the three card's value
    */
  val three = 3

  /** Enables `four` to be used instead of the four card's value
    */
  val four = 4

  /** Enables `five` to be used instead of the five card's value
    */
  val five = 5

  /** Enables `six` to be used instead of the six card's value
    */
  val six = 6

  /** Enables `seven` to be used instead of the seven card's value
    */
  val seven = 7

  /** Enables `jack` to be used instead of the jack card's value
    */
  val jack = 8

  /** Enables `knight` to be used instead of the knight card's value
    */
  val knight = 9

  extension (value: Int)
    /** Allows to write `<value> of <Suit>` to create a card
      * @param suit
      *   the card's suit
      * @return
      *   the card with given value and suit
      */
    infix def of(suit: Suit): Card = CardImpl(value, suit)
