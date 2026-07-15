package model.deck

import model.deck.card.{CardImpl, Suit}

object DeckFactory:

  def apply(): Deck =
    val cards = for
      suit <- Suit.values
      value <- 1 to 10
    yield CardImpl(value, suit)
    DeckImpl(cards.toVector)
