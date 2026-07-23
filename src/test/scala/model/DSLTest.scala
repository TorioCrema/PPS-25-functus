package org.pps.functus
package model

import model.deck.card.{CardImpl, Suit}
import model.deck.sugar.CardDSL.*
import model.deck.sugar.FieldDSL.{*, given}

import model.deck.card.Suit.{Cups, Pentacles, Wands}
import model.field.FieldImpl
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DSLTest extends AnyFlatSpec with Matchers:
  private val threeOfCups = 3 of Cups
  private val sixOfPentacles = 6 of Pentacles
  private val knightOfWands = 9 of Wands

  private val termValueMap =
    Map((king, 0), (ace, 1), (two, 2), (three, 3), (four, 4), (five, 5), (six, 6), (seven, 7), (jack, 8), (knight, 9))

  private def testTerm(term: Int, expected: Int): Unit =
    for suit <- Suit.values do term of suit should be(CardImpl(expected, suit))

  "Int of Suit" should "return Card(Int, Suit)" in:
    for
      value <- 0 until 10
      suit <- Suit.values
    do value of suit should be(CardImpl(value, suit))

  "term of Suit" should "return the correct card" in:
    termValueMap foreach ((term, expectedValue) => testTerm(term, expectedValue))

  "firstCard and secondCard" should "return a field made of firstCard and secondCard" in:
    threeOfCups and sixOfPentacles should be(FieldImpl(Vector(threeOfCups, sixOfPentacles)))

  "firstCard and secondCard and thirdCard" should "return a field made of firstCard, secondCard and thirdCard" in:
    threeOfCups and sixOfPentacles and knightOfWands should be(
      FieldImpl(Vector(threeOfCups, sixOfPentacles, knightOfWands))
    )

  "take the Int from field" should "return the taken card and the new field" in:
    take the 0 from (threeOfCups and sixOfPentacles and knightOfWands) should be(
      (threeOfCups, sixOfPentacles and knightOfWands)
    )
