package org.pps.functus
package model.deck

import model.deck.card.{CardImpl, Suit}

/** Factory for creating standard [[Deck]] instances. */
object DeckFactory:

  /** Creates a new unshuffled deck containing cards with values 1–10 for each [[Suit]].
    *
    * @return
    *   a [[Deck]] of `Suit.values.length * 10` cards
    */
  def apply(): Deck =
    val cards = for
      suit <- Suit.values
      value <- 1 to 10
    yield CardImpl(value, suit)
    DeckImpl(cards.toVector)
