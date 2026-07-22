package org.pps.functus
package view

import model.deck.card.{Card, Suit}
import model.deck.card.Suit.*
import scala.io.AnsiColor.*

object Drawers:
  private def getValueString(value: Int): String = value match
    case 0 => "K"
    case 1 => "A"
    case _ => value.toString

  private def getSuitColor(suit: Suit): String = suit match
    case Pentacles => s"$YELLOW"
    case Cups      => s"$RED"
    case Swords    => s"$BLUE"
    case Wands     => s"$GREEN"

  given Drawable[Card] = card => s"${getSuitColor(card.suit)}${getValueString(card.value)}$RESET"
