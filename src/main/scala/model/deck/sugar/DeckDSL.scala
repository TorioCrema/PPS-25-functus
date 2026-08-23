package org.pps.functus
package model.deck.sugar

import model.deck.{Deck, DeckFactory, DeckImpl}
import model.deck.card.Card

/** DSL for creating and interacting with [[Deck]] instances.
  *
  * Usage:
  * {{{
  *   import model.deck.sugar.DeckDSL.*
  *
  *   val d1 = deck()
  *   val d2 = deck from (ace of Cups | two of Swords)
  *   val d3 = deck fromShuffled (ace of Cups | two of Swords)
  *   val d4 = deck single (ace of Cups)
  *   val d5 = deck from List(ace of Cups, two of Swords)
  *
  *   // CardBuilder converts automatically to Deck via given Conversion
  *   val d6: Deck = (ace of Cups) | (two of Swords)
  * }}}
  */
object DeckDSL:

  object deck:

    /** Create a standard Italian Deck of cards. */
    infix def apply(): Deck = DeckFactory()

    /** Creates a [[Deck]] from a [[CardBuilder]] chain.
      *
      * @param builder
      *   the [[CardBuilder]] containing the cards
      * @return
      *   a [[Deck]] with the cards in the builder, in insertion order
      */
    infix def from(builder: CardBuilder): Deck = DeckImpl(builder.cards)

    /** Creates a [[Deck]] from a [[List]] of [[Card]]s.
      *
      * @param cards
      *   the list of cards
      * @return
      *   a [[Deck]] with the given cards, in list order
      */
    infix def from(cards: List[Card]): Deck = DeckImpl(cards.toVector)

    /** Creates a shuffled [[Deck]] from a [[CardBuilder]] chain.
      *
      * @param builder
      *   the [[CardBuilder]] containing the cards
      * @return
      *   a shuffled [[Deck]] with the cards in the builder
      */
    infix def fromShuffled(builder: CardBuilder): Deck = DeckImpl(builder.cards).shuffle()

    /** Creates a [[CardBuilder]] containing a single [[Card]].
      *
      * Useful when a one-card deck or a one-card builder chain is needed.
      *
      * @param card
      *   the card to wrap
      * @return
      *   a [[CardBuilder]] with the single card
      */
    infix def single(card: Card): CardBuilder = CardBuilder(Vector(card))

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
      infix def |(other: Card): CardBuilder = CardBuilder(cards :+ other)

    /** Allows a [[CardBuilder]] to be used wherever a [[Deck]] is expected.
      */
    given Conversion[CardBuilder, Deck] = b => DeckImpl(b.cards)

    /** Allows any [[Card]] to start a [[CardBuilder]] chain using `|`. */
    extension (card: Card)
      /** Combines this card with another into a [[CardBuilder]].
        *
        * {{{
        *   Ace of Spades | King of Hearts
        * }}}
        */
      infix def |(other: Card): CardBuilder = CardBuilder(Vector(card, other))
