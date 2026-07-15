package model

import model.deck.card.Suit.*
import model.deck.card.{Card, CardImpl}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CardTest extends AnyFlatSpec with Matchers:

  val cardTestPentacles: Card = CardImpl(1, Pentacles)
  val cardTestSwords: Card = CardImpl(2, Swords)
  val cardTestCups: Card = CardImpl(3, Cups)
  val cardTestWands: Card = CardImpl(4, Wands)

  "A Card" should "always have a value" in:
    cardTestPentacles.value should be(1)
    cardTestSwords.value should be(2)
    cardTestCups.value should be(3)
    cardTestWands.value should be(4)

  it should "always have a suit" in:
    cardTestPentacles.suit should be(Pentacles)
    cardTestSwords.suit should be(Swords)
    cardTestCups.suit should be(Cups)
    cardTestWands.suit should be(Wands)
