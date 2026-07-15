package org.pps.functus
package model

import model.deck.card.Card
import model.deck.{Deck, DeckFactory}
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

class DeckTest extends AnyFlatSpec with Matchers:
  val deck: Deck = DeckFactory()
  val result: (Card, Deck) = deck.draw()

  "A default Deck" should "always have 40 cards" in:
    deck.cards.size should be(40)

  it should "the result of a draw be a card" in:
    val drawnCard: Card = result._1
    drawnCard should not be null

  it should "the remaining deck have 39 cards" in:
    val remainingDeck: Deck = result._2
    remainingDeck.cards.size should be(39)

  it should "usually change the order when shuffled" in:
    val shuffledDeck = deck.shuffle()
    val shuffledDeck2 = deck.shuffle()
    shuffledDeck.cards should not be shuffledDeck2.cards
