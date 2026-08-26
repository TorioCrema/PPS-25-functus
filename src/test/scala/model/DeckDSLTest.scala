package org.pps.functus
package model

import model.deck.sugar.DeckDSL.deck
import model.deck.sugar.DeckDSL.deck.*
import model.deck.sugar.CardDSL.*
import model.deck.card.Suit.*
import model.deck.Deck

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DeckDSLTest extends AnyFlatSpec with Matchers:

  "Card |" should "produce a CardBuilder with two cards" in:
    val builder = (ace of Cups) | (two of Cups)
    builder.cards should be(Vector(ace of Cups, two of Cups))

  it should "preserve insertion order" in:
    val builder = (ace of Cups) | (two of Swords) | (three of Wands)
    builder.cards should be(Vector(ace of Cups, two of Swords, three of Wands))

  it should "allow chaining many cards" in:
    val builder =
      (ace of Cups) | (two of Cups) | (three of Cups) | (four of Cups) | (five of Cups)
    builder.cards.length should be(5)

  "deck.single" should "produce a CardBuilder with exactly one card" in:
    val singleBuilder = deck single (ace of Cups)
    singleBuilder.cards should be(Vector(ace of Cups))

  it should "convert to a Deck with one card via given Conversion" in:
    val singleCardDeck: Deck = deck single (ace of Cups)
    singleCardDeck.cards should be(Vector(ace of Cups))

  "deck.fromShuffled" should "produce a deck with the same cards as the builder" in:
    val shuffledDeck = deck fromShuffled ((ace of Cups) | (two of Swords) | (three of Wands))
    shuffledDeck.cards should contain allOf (ace of Cups, two of Swords, three of Wands)
    shuffledDeck.cards.length should be(3)

  "CardBuilder" should "convert implicitly to a Deck" in:
    val implicitDeck: Deck = (ace of Cups) | (two of Swords)
    implicitDeck.cards should be(Vector(ace of Cups, two of Swords))

  it should "convert a single-card builder to a Deck" in:
    val singleCardDeck: Deck = deck single(ace of Cups)
    singleCardDeck.cards should be(Vector(ace of Cups))

  it should "convert an empty builder to an empty Deck" in:
    val emptyDeck: Deck = CardBuilder(Vector.empty)
    emptyDeck.cards should be(Vector.empty)
    emptyDeck.draw() should be(None)

  "deck from CardBuilder" should "produce a deck with the given cards" in:
    val twoCardDeck = deck from ((ace of Cups) | (two of Swords))
    twoCardDeck.cards should be(Vector(ace of Cups, two of Swords))

  it should "preserve the card order" in:
    val orderedDeck = deck from ((three of Wands) | (ace of Cups) | (two of Pentacles))
    orderedDeck.cards should be(Vector(three of Wands, ace of Cups, two of Pentacles))

  it should "produce a deck with a single card when the builder has one" in:
    val singleCardDeck = deck from CardBuilder(Vector(king of Cups))
    singleCardDeck.cards should be(Vector(king of Cups))

  it should "produce an empty deck from an empty builder" in:
    val emptyDeck = deck from CardBuilder(Vector.empty)
    emptyDeck.cards should be(Vector.empty)
    emptyDeck.draw() should be(None)

  "A deck built from CardBuilder" should "draw cards in insertion order" in:
    val threeCardDeck = deck from ((ace of Cups) | (two of Swords) | (three of Wands))
    val (firstCard, deckAfterFirst) = threeCardDeck.draw().get
    firstCard should be(ace of Cups)
    val (secondCard, _) = deckAfterFirst.draw().get
    secondCard should be(two of Swords)

  it should "return None when drawn from empty" in:
    val emptyDeck = deck from CardBuilder(Vector.empty)
    emptyDeck.draw() should be(None)

  it should "return the correct remaining deck after draw" in:
    val twoCardDeck = deck from ((ace of Cups) | (two of Swords))
    val (_, deckAfterDraw) = twoCardDeck.draw().get
    deckAfterDraw.cards should be(Vector(two of Swords))

  it should "be empty after drawing all cards" in:
    val twoCardDeck = deck from ((ace of Cups) | (two of Swords))
    val (_, afterFirst) = twoCardDeck.draw().get
    val (_, afterSecond) = afterFirst.draw().get
    afterSecond.draw() should be(None)
