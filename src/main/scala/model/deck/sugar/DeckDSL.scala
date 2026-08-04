package org.pps.functus
package model.deck.sugar

import model.deck.{Deck, DeckFactory, DeckImpl}
import model.deck.card.Card

/** DSL for creating and interacting with [[Deck]] instances.
  *
  * Usage:
  * {{{
  *   import model.deck.DeckDsl.*
  *
  *   val d1 = deck standard
  *   val d2 = deck con { Asso di Cuori | Re di Picche }
  * }}}
  */
object DeckDSL:

  object deck:

    /** Create a standard Italian Deck of cards. */
    infix def apply(): Deck = DeckFactory()

    /** Create a deck using [[CardBuilder]]. */
    infix def from(builder: CardBuilder): Deck = DeckImpl(builder.cards)

    /** Accumulates cards via the `|` operator.
      *
      * Produced automatically when two [[Card]]s are combined with `|`.
      *
      * {{{
      *   Ace of Spades | King of Hearts | Seven of Clubs
      * }}}
      */
    case class CardBuilder(cards: Vector[Card]):
      /** Appends a card to this builder. */
      infix def |(other: Card): CardBuilder = copy(cards = cards :+ other)

    /** Allows any [[Card]] to start a [[CardBuilder]] chain using `|`. */
    extension (card: Card)
      /** Combines this card with another into a [[CardBuilder]].
        *
        * {{{
        *   Ace of Spades | King of Hearts
        * }}}
        */
      infix def |(other: Card): CardBuilder = CardBuilder(Vector(card, other))
