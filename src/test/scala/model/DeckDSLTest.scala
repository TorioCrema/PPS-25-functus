package org.pps.functus
package model

import model.deck.sugar.DeckDSL.deck

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import model.deck.card.Suit.{Cups, Pentacles, Wands}
import model.deck.sugar.CardDSL.*
import model.deck.sugar.DeckDSL.deck.|

class DeckDSLTest extends AnyFlatSpec with Matchers:

  private val decktest = deck()
  private val deckfrom = deck from (ace of Cups) | (seven of Pentacles) | (king of Wands)

  "Default deck" should "return a deck with all 40 cards" in:
    decktest.cards.length should be(40)

  "Custom deck" should "return a deck with 3 cards" in:
    deckfrom.cards.length should be(3)

  it should "pick the ace of cups as the first card" in:
    deckfrom.draw()._1 should be(ace of Cups)
    deckfrom.draw()._2.cards.length should be(2)
