package org.pps.functus
package model.deck

import model.deck.sugar.CardDSL.of
import model.deck.card.Suit

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
    yield value of suit
    DeckImpl(cards.toVector)
