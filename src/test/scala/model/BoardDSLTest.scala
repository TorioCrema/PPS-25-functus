package org.pps.functus
package model

import model.board.Player.*
import model.deck.card.Suit.{Cups, Pentacles, Swords, Wands}
import model.deck.sugar.BoardDSL.*
import model.deck.sugar.CardDSL.*
import model.deck.sugar.DeckDSL.deck
import model.deck.sugar.FieldDSL.given_Conversion_Card_FieldBuilderLike
import model.deck.sugar.DeckDSL.deck.|
import model.deck.sugar.FieldDSL.{*, given}

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class BoardDSLTest extends AnyFlatSpec with Matchers:

  private val threeOfCups = 3 of Cups
  private val sixOfPentacles = 6 of Pentacles
  private val knightOfWands = 9 of Wands
  private val twoOfCups = 2 of Cups
  private val sixOfCups = 6 of Cups
  private val aceOfSwords = 1 of Swords
  private val twoOfSwords = 2 of Swords
  private val threeOfSwords = 3 of Swords
  private val fourOfSwords = 4 of Swords
  private val fiveOfWands = 5 of Wands

  "default board" should "return a fully populated board" in:
    val boardTest = default board

    boardTest.players(Player1).length should be(4)
    boardTest.players(Player2).length should be(4)
    boardTest.deck.cards.length should be(32)

  it should "have an empty discard pile" in:
    (default board).discardPile should be(Nil)

  "board from default" should "return a fully populated board" in:
    val boardTest = board from default

    boardTest.deck.cards.length should be(40)
    boardTest.players(Player1).length should be(4)
    boardTest.players(Player2).length should be(4)

  it should "allow overriding player one's field" in:
    val boardTest = (board from default) withCustom playerOne(threeOfCups and sixOfPentacles)

    boardTest.players(Player1).cardsList should contain(threeOfCups)
    boardTest.players(Player1).cardsList should contain(sixOfPentacles)
    boardTest.players(Player2).length should be(4)

  it should "allow overriding player two's field" in:
    val boardTest = board from default withCustom playerTwo(threeOfCups and sixOfPentacles)
    boardTest.players(Player2).cardsList should contain(threeOfCups)
    boardTest.players(Player2).cardsList should contain(sixOfPentacles)
    boardTest.players(Player1).length should be(4)

  it should "allow overriding both players' fields" in:
    val boardTest =
      board from default withCustom playerOne(threeOfCups and sixOfPentacles) withCustom playerTwo(
        knightOfWands
      ) withCustom discardPile(twoOfCups | sixOfCups)

    boardTest.players(Player1).cardsList should contain(threeOfCups)
    boardTest.players(Player2).cardsList should contain(knightOfWands)

  it should "not remove assigned cards from the deck" in:
    val b = (board from default) withCustom playerOne(threeOfCups and sixOfPentacles)
    b.deck.cards should contain(threeOfCups)
    b.deck.cards should contain(sixOfPentacles)

  it should "not remove discard pile cards from the deck" in:
    val b = (board from default) withCustom discardPile(twoOfCups | sixOfCups)
    b.discardPile should be(List(twoOfCups, sixOfCups))
    b.deck.cards should contain(twoOfCups)
    b.deck.cards should contain(sixOfCups)

  it should "use a custom deck when provided" in:
    val custom = deck from (aceOfSwords | twoOfSwords | threeOfSwords | fourOfSwords | fiveOfWands)
    val b = (board from default) withCustom customDeck(custom)
    b.deck.cards should contain(aceOfSwords)
    b.deck.cards should contain(fiveOfWands)
    b.deck.cards.length should be(5)

  it should "use the custom deck and still deal player fields from default when not overridden" in:
    val custom = deck from (
      aceOfSwords | twoOfSwords | threeOfSwords | fourOfSwords | fiveOfWands |
        (1 of Cups) | (2 of Cups) | (3 of Cups) | (4 of Cups) |
        (1 of Wands) | (2 of Wands) | (3 of Wands) | (4 of Wands)
    )
    val b = (board from default)
      .withCustom(playerOne((1 of Cups) and (2 of Cups) and (3 of Cups) and (4 of Cups)))
      .withCustom(playerTwo((1 of Wands) and (2 of Wands) and (3 of Wands) and (4 of Wands)))
      .withCustom(customDeck(custom))
    b.players(Player1).length should be(4)
    b.players(Player2).length should be(4)
    b.deck.cards.length should be(custom.cards.length)

  "lockedBoard from default" should "remove assigned player1 cards from the deck" in:
    val b = (lockedBoard from default) withCustom playerOne(threeOfCups and sixOfPentacles)
    b.deck.cards should not contain threeOfCups
    b.deck.cards should not contain sixOfPentacles

  it should "remove assigned player2 cards from the deck" in:
    val b = (lockedBoard from default) withCustom playerTwo(threeOfCups and sixOfPentacles)
    b.deck.cards should not contain threeOfCups
    b.deck.cards should not contain sixOfPentacles

  it should "remove cards from both players' fields" in:
    val b = (lockedBoard from default)
      .withCustom(playerOne(threeOfCups and sixOfPentacles))
      .withCustom(playerTwo(knightOfWands))
    b.deck.cards should not contain threeOfCups
    b.deck.cards should not contain sixOfPentacles
    b.deck.cards should not contain knightOfWands

  it should "not remove cards when none are assigned" in:
    val b = lockedBoard from default
    b.deck.cards.length should be(40 - 8)

  it should "remove discard pile cards from the deck" in:
    val b = (lockedBoard from default) withCustom discardPile(twoOfCups | sixOfCups)
    b.discardPile should be(List(twoOfCups, sixOfCups))
    b.deck.cards should not contain twoOfCups
    b.deck.cards should not contain sixOfCups

  it should "remove both player fields and discard pile cards from the deck" in:
    val b = (lockedBoard from default)
      .withCustom(playerOne(threeOfCups))
      .withCustom(discardPile(twoOfCups | sixOfCups))
    b.deck.cards should not contain threeOfCups
    b.deck.cards should not contain twoOfCups
    b.deck.cards should not contain sixOfCups

  it should "use a custom deck and remove cards from it but deal them to the second player" in:
    val custom = deck from (aceOfSwords | twoOfSwords | threeOfSwords | fourOfSwords | fiveOfWands)
    val b = (lockedBoard from default)
      .withCustom(customDeck(custom))
      .withCustom(playerOne(threeOfCups and sixOfPentacles))
    b.getField(Player2).cardsList should contain allOf (aceOfSwords, twoOfSwords, threeOfSwords, fourOfSwords)
    b.deck.cards.length should be(1)

  it should "preserve the discard pile order" in:
    val b = (lockedBoard from default) withCustom discardPile(twoOfCups | sixOfCups | fiveOfWands)
    b.discardPile should be(List(twoOfCups, sixOfCups, fiveOfWands))
