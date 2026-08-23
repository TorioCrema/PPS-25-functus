package org.pps.functus
package model

import model.deck.sugar.DeckDSL.deck
import model.deck.sugar.DeckDSL.deck.*
import model.deck.sugar.CardDSL.*
import model.deck.card.Suit.*

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

  "CardBuilder |" should "append a card to an existing builder" in:
    val builder = ((ace of Cups) | (two of Cups)) | (three of Cups)
    builder.cards should be(Vector(ace of Cups, two of Cups, three of Cups))

  "deck from CardBuilder" should "produce a deck with the given cards" in:
    val d = deck from ((ace of Cups) | (two of Swords))
    d.cards should be(Vector(ace of Cups, two of Swords))

  it should "preserve the card order" in:
    val d = deck from ((three of Wands) | (ace of Cups) | (two of Pentacles))
    d.cards should be(Vector(three of Wands, ace of Cups, two of Pentacles))

  it should "produce a deck with a single card when the builder has one" in:
    val builder = CardBuilder(Vector(king of Cups))
    val d = deck from builder
    d.cards should be(Vector(king of Cups))

  it should "allow drawing cards in insertion order" in:
    val d = deck from ((ace of Cups) | (two of Swords) | (three of Wands))
    val (first, remaining) = d.draw().get
    first should be(ace of Cups)
    val (second, _) = remaining.draw().get
    second should be(two of Swords)

  it should "produce an empty deck from an empty builder" in:
    val d = deck from CardBuilder(Vector.empty)
    d.cards should be(Vector.empty)
    d.draw() should be(None)

  "A deck built from CardBuilder" should "return None when drawn from empty" in:
    val d = deck from CardBuilder(Vector.empty)
    d.draw() should be(None)

  it should "return the remaining deck after draw" in:
    val d = deck from ((ace of Cups) | (two of Swords))
    val (_, remaining) = d.draw().get
    remaining.cards should be(Vector(two of Swords))

  it should "be empty after drawing all cards" in:
    val d = deck from ((ace of Cups) | (two of Swords))
    val (_, r1) = d.draw().get
    val (_, r2) = r1.draw().get
    r2.draw() should be(None)
