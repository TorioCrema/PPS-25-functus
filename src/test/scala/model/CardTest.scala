package org.pps.functus
package model

import model.deck.card.Suit.*
import model.deck.card.Card
import model.deck.sugar.CardDSL.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CardTest extends AnyFlatSpec with Matchers:

  "A Card" should "have the correct value" in:
    (ace of Pentacles).value should be(1)
    (two of Swords).value should be(2)
    (three of Cups).value should be(3)
    (four of Wands).value should be(4)

  it should "have the correct suit" in:
    (ace of Pentacles).suit should be(Pentacles)
    (two of Swords).suit should be(Swords)
    (three of Cups).suit should be(Cups)
    (four of Wands).suit should be(Wands)

  it should "create a king with value 0" in:
    (king of Swords).value should be(0)

  it should "create cards with all supported named values" in:
    (ace of Swords).value should be(1)
    (two of Swords).value should be(2)
    (three of Swords).value should be(3)
    (four of Swords).value should be(4)
    (five of Swords).value should be(5)
    (six of Swords).value should be(6)
    (seven of Swords).value should be(7)
    (jack of Swords).value should be(8)
    (knight of Swords).value should be(9)
