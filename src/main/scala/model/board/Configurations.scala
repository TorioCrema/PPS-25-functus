package org.pps.functus
package model.board

import model.deck.sugar.DeckDSL.deck
import model.deck.sugar.DeckDSL.deck.*
import model.deck.sugar.CardDSL.*
import model.deck.sugar.BoardDSL.*
import model.deck.card.Suit.*

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

  private val discardPileWithKing = List(king of Swords, three of Cups)

  private val discardPileWithoutKing = List(three of Cups, seven of Cups)

  private val discardPileForSuccessfulDiscard = List(three of Cups)

  private val discardPileForFailedDiscard = List(three of Cups)

  val boardForSixEffect = lockedBoard from default
    .withCustom(customDeck(deckForSixEffect))
    .withCustom(discardPile(discardPileWithoutKing))
    .withCustom(playerOne(three of Pentacles and knight of Pentacles))
    .withCustom(playerTwo(four of Pentacles and five of Pentacles))

  val boardForSevenEffect = lockedBoard from default
    .withCustom(customDeck(deckForSevenEffect))
    .withCustom(discardPile(discardPileWithoutKing))
    .withCustom(playerOne(three of Pentacles and knight of Pentacles))
    .withCustom(playerTwo(four of Pentacles and five of Pentacles))

  val boardForJackEffect = lockedBoard from default
    .withCustom(customDeck(deckForJackEffect))
    .withCustom(discardPile(discardPileWithoutKing))
    .withCustom(playerOne(three of Pentacles and knight of Pentacles))
    .withCustom(playerTwo(four of Pentacles and five of Pentacles))

  val boardForKingDraw = lockedBoard from default
    .withCustom(customDeck(deckForKingEffect))
    .withCustom(discardPile(discardPileWithKing))
    .withCustom(playerOne(three of Pentacles and knight of Pentacles))
    .withCustom(playerTwo(four of Pentacles and five of Pentacles))

  val boardForSuccessfulDiscard = lockedBoard from default
    .withCustom(customDeck(deckForSixEffect))
    .withCustom(discardPile(discardPileForSuccessfulDiscard))
    .withCustom(playerOne(three of Cups and knight of Pentacles))
    .withCustom(playerTwo(four of Pentacles and five of Pentacles))

  val boardForFailedDiscard = lockedBoard from default
    .withCustom(customDeck(deckForSixEffect))
    .withCustom(discardPile(discardPileForFailedDiscard))
    .withCustom(playerOne(six of Cups and knight of Pentacles))
    .withCustom(playerTwo(four of Pentacles and five of Pentacles))
