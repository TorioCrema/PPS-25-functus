package org.pps.functus
package model

import model.deck.card.Card
import model.deck.{Deck, DeckFactory}
import model.deck.sugar.DeckDSL.deck

import model.deck.card.Suit.*
import model.deck.sugar.CardDSL.*
import model.deck.sugar.DeckDSL.deck.|

import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

class DeckTest extends AnyFlatSpec with Matchers:
  private val deckTest: Deck = deck()
  private val deckFromTest: Deck = deck from (ace of Cups) | (seven of Pentacles) | (king of Wands)

  "A default Deck" should "have 40 cards" in:
    deckTest.cards.size should be(40)

  it should "return a card when drawn" in:
    val (drawnCard, _) = deckTest.draw().get
    drawnCard should not be null

  it should "have 39 cards after a draw" in:
    val (_, remainingDeck) = deckTest.draw().get
    remainingDeck.cards.size should be(39)

  it should "contain the same cards after a draw" in:
    val (drawnCard, remainingDeck) = deckTest.draw().get
    (remainingDeck.cards :+ drawnCard) should contain theSameElementsAs deckTest.cards

  it should "change order when shuffled" in:
    val attempts = (1 to 5).map(_ => deckTest.shuffle().cards)
    attempts.distinct.size should be > 1

  it should "pick the ace of cups as the first card" in:
    val (drawnCard, remainingDeck) = deckFromTest.draw().get
    drawnCard should be(ace of Cups)
    remainingDeck.cards.length should be(2)
