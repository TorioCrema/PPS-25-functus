package org.pps.functus
package view

import model.deck.card.Suit.*
import model.deck.card.{Card, Suit}
import view.Drawers.given

import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock

import scala.io.AnsiColor.*

class CardDrawTest extends AnyFlatSpec with Matchers:
  private val swordsColor = s"$BLUE"
  private val wandsColor = s"$GREEN"
  private val pentaclesColor = s"$YELLOW"
  private val cupsColor = s"$RED"

  private def mockCard(value: Int, suit: Suit): Card =
    val card = mock[Card]
    when(card.value).thenReturn(value)
    when(card.suit).thenReturn(suit)
    card

  private def getSuitColor(suit: Suit): String = suit match
    case Swords    => swordsColor
    case Wands     => wandsColor
    case Pentacles => pentaclesColor
    case Cups      => cupsColor

  private def getValueString(value: Int): String = value match
    case 0 => "K"
    case 1 => "A"
    case _ => value.toString

  "Card Drawer" should "draw cards" in:
    val cardDrawer = summon[Drawable[Card]]
    for
      value <- 0 until 10
      suit <- Suit.values
    do cardDrawer.draw(mockCard(value, suit)) should be(getSuitColor(suit) + getValueString(value) + s"$RESET")
