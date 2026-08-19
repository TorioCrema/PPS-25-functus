package org.pps.functus
package model

import model.board.Player
import model.deck.card.Suit.{Cups, Pentacles, Wands}
import model.deck.sugar.BoardDSL.*
import model.deck.sugar.CardDSL.*
import model.deck.sugar.FieldDSL.given_Conversion_Card_FieldBuilderLike

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class BoardDSLTest extends AnyFlatSpec with Matchers:

  private val threeOfCups = 3 of Cups
  private val sixOfPentacles = 6 of Pentacles
  private val knightOfWands = 9 of Wands

  "default board" should "return a fully populated board" in:
    val b = default board

    b.players(Player.Player1).length should be(4)
    b.players(Player.Player2).length should be(4)

  "board from default" should "return a fully populated board" in:
    val b = board from default

    b.build.players(Player.Player1).length should be(4)
    b.build.players(Player.Player2).length should be(4)

  it should "allow overriding player one's field" in:
    val b = (board from default) withCustom playerOne(threeOfCups and sixOfPentacles)

    b.build.players(Player.Player1).cardsList should contain(threeOfCups)
    b.build.players(Player.Player1).cardsList should contain(sixOfPentacles)
    b.build.players(Player.Player2).length should be(4)

  it should "allow overriding player two's field" in:
    val b = (board from default) withCustom playerTwo(threeOfCups and sixOfPentacles)
    b.build.players(Player.Player2).cardsList should contain(threeOfCups)
    b.build.players(Player.Player2).cardsList should contain(sixOfPentacles)
    b.build.players(Player.Player1).length should be(4)

  it should "allow overriding both players' fields" in:
    val b = (board from default)
      .withCustom(playerOne(threeOfCups and sixOfPentacles))
      .withCustom(playerTwo(knightOfWands and sixOfPentacles))

    b.build.players(Player.Player1).cardsList should contain(threeOfCups)
    b.build.players(Player.Player2).cardsList should contain(knightOfWands)

  "lockedBoard from default" should "remove assigned cards from the deck" in:
    val b = (lockedBoard from default) withCustom playerOne(threeOfCups and sixOfPentacles)

    b.build.deck.cards should not contain threeOfCups
    b.build.deck.cards should not contain sixOfPentacles

  it should "not remove cards when none are assigned" in:
    val b = lockedBoard from default
    b.build.deck.cards.length should be(40 - 8)

  "board from default (unlocked)" should "not remove assigned cards from the deck" in:
    val b = (board from default) withCustom playerOne(threeOfCups and sixOfPentacles)
    b.build.deck.cards should contain(threeOfCups)
    b.build.deck.cards should contain(sixOfPentacles)
