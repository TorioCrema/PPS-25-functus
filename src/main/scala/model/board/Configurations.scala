package org.pps.functus
package model.board

import model.deck.sugar.DeckDSL.deck
import model.deck.sugar.DeckDSL.deck.*
import model.deck.sugar.CardDSL.*
import model.deck.sugar.BoardDSL.*
import model.deck.card.Suit.*
import model.deck.sugar.FieldDSL.{*, given}

object Configurations:

  private val deckForSixEffect = deck from
    (six of Swords) |
    (three of Cups) |
    (king of Cups) |
    (king of Swords)

  private val deckForSevenEffect = deck from
    (seven of Swords) |
    (six of Cups) |
    (three of Cups) |
    (king of Cups)

  private val deckForJackEffect = deck from
    (jack of Swords) |
    (three of Cups) |
    (jack of Swords) |
    (knight of Swords)

  private val deckForKingEffect = deck from
    (king of Swords) |
    (three of Cups) |
    (king of Cups) |
    (knight of Pentacles)

  private val discardPileWithKing = (king of Swords) | (three of Cups)

  private val discardPileWithoutKing = (three of Cups) | (seven of Cups)

  def boardForSixEffect = (lockedBoard from default)
    .withCustom(customDeck(deckForSixEffect))
    .withCustom(discardPile(discardPileWithoutKing))
    .withCustom(playerOne((three of Pentacles) and (knight of Pentacles)))
    .withCustom(playerTwo((four of Pentacles) and (five of Pentacles)))

  def boardForSevenEffect = (lockedBoard from default)
    .withCustom(customDeck(deckForSevenEffect))
    .withCustom(discardPile(discardPileWithoutKing))
    .withCustom(playerOne((three of Swords) and (knight of Cups)))
    .withCustom(playerTwo((four of Pentacles) and (five of Pentacles)))

  def boardForJackEffect = (lockedBoard from default)
    .withCustom(customDeck(deckForJackEffect))
    .withCustom(discardPile(discardPileWithoutKing))
    .withCustom(playerOne((knight of Swords) and (ace of Pentacles)))
    .withCustom(playerTwo((knight of Pentacles) and (six of Cups)))

  def boardForKingDraw = (lockedBoard from default)
    .withCustom(customDeck(deckForKingEffect))
    .withCustom(discardPile(discardPileWithKing))
    .withCustom(playerOne((three of Cups) and (four of Swords)))
    .withCustom(playerTwo((four of Pentacles) and (five of Pentacles)))

  def boardForSuccessfulDiscard = (lockedBoard from default)
    .withCustom(customDeck(deckForSixEffect))
    .withCustom(discardPile(discardPileWithKing))
    .withCustom(playerOne((three of Cups) and (knight of Pentacles)))
    .withCustom(playerTwo((four of Pentacles) and (five of Pentacles)))

  def boardForFailedDiscard = (lockedBoard from default)
    .withCustom(customDeck(deckForSixEffect))
    .withCustom(discardPile(discardPileWithoutKing))
    .withCustom(playerOne((five of Swords) and (knight of Swords)))
    .withCustom(playerTwo((four of Pentacles) and (jack of Cups)))
