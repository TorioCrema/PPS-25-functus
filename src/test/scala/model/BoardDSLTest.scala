package org.pps.functus
package model

import model.board.Player
import model.deck.card.Suit.{Cups, Pentacles, Wands}
import model.deck.sugar.BoardDSL.*
import model.deck.sugar.CardDSL.*
import model.deck.sugar.FieldDSL.given_Conversion_Card_FieldBuilderLike

import org.pps.functus.model.deck.sugar.DeckDSL.deck.|
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class BoardDSLTest extends AnyFlatSpec with Matchers:

  private val threeOfCups = 3 of Cups
  private val sixOfPentacles = 6 of Pentacles
  private val knightOfWands = 9 of Wands
  private val twoOfCups = 2 of Cups
  private val sixOfCups = 6 of Cups

  "default board" should "return a fully populated board" in:
    val boardTest = default board

    boardTest.players(Player.Player1).length should be(4)
    boardTest.players(Player.Player2).length should be(4)
    boardTest.deck.cards.length should be(32)

  "board from default" should "return a fully populated board" in:
    val boardTest = board from default

    boardTest.deck.cards.length should be(40)
    boardTest.players(Player.Player1).length should be(4)
    boardTest.players(Player.Player2).length should be(4)

  it should "allow overriding player one's field" in:
    val boardTest = (board from default) withCustom playerOne(threeOfCups and sixOfPentacles)

    boardTest.players(Player.Player1).cardsList should contain(threeOfCups)
    boardTest.players(Player.Player1).cardsList should contain(sixOfPentacles)
    boardTest.players(Player.Player2).length should be(4)

  it should "allow overriding player two's field" in:
    val boardTest = board from default withCustom playerTwo(threeOfCups and sixOfPentacles)
    boardTest.players(Player.Player2).cardsList should contain(threeOfCups)
    boardTest.players(Player.Player2).cardsList should contain(sixOfPentacles)
    boardTest.players(Player.Player1).length should be(4)

  it should "allow overriding both players' fields" in:
    val boardTest =
      board from default withCustom playerOne(threeOfCups and sixOfPentacles) withCustom playerTwo(
        knightOfWands
      ) withCustom discardPile(twoOfCups | sixOfCups)

    boardTest.players(Player.Player1).cardsList should contain(threeOfCups)
    boardTest.players(Player.Player2).cardsList should contain(knightOfWands)

  "lockedBoard from default" should "remove assigned cards from the deck" in:
    val boardTest = lockedBoard from default withCustom playerOne(threeOfCups and sixOfPentacles)

    boardTest.deck.cards should not contain threeOfCups
    boardTest.deck.cards should not contain sixOfPentacles

  it should "not remove cards when none are assigned" in:
    val boardTest = lockedBoard from default
    boardTest.deck.cards.length should be(40 - 8)

  "board from default (unlocked)" should "not remove assigned cards from the deck" in:
    val boardTest = (board from default) withCustom playerOne(threeOfCups and sixOfPentacles)
    boardTest.deck.cards should contain(threeOfCups)
    boardTest.deck.cards should contain(sixOfPentacles)
