package org.pps.functus
package model

import model.board.BoardFactory.CustomBoard
import model.board.Player.*
import model.board.{BoardFactory, Player}
import model.field.Field
import model.deck.card.Card
import model.deck.card.Suit.*
import model.deck.sugar.CardDSL.*
import model.deck.sugar.FieldDSL.{*, given}
import model.playable.turn.Action.*
import model.playable.turn.Turns.*
import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FirstTurnTest extends AnyFlatSpec with Matchers:
  private val player1Field = (three of Cups) and (two of Swords) and (jack of Wands) and (seven of Cups)
  private val player2Field = (five of Wands) and (ace of Pentacles) and (six of Swords) and (four of Wands)
  private val board = CustomBoard(List(player1Field, player2Field))
  private val firstTurn = FirstTurn(board, Player1)

  "FirstTurn" should "have Confirm as next action" in:
    firstTurn.actions should be(Observe :: Nil)

  it should "draw from the player's field when observing" in:
    val observedCards = 2
    def getHand(field: Field): List[Card] = field.cardsList.slice(0, observedCards)
    val playerHands = Map((Player1, getHand(player1Field)), (Player2, getHand(player2Field)))
    val playerFields = board.getField
    for player <- Player.values
    do
      val afterObserve = FirstTurn(board, player).act(Observe)
      afterObserve.hand should be(playerHands(player))
      val expectedField = playerFields(player).cardsList.slice(observedCards, playerFields(player).length)
      afterObserve.board.getField(player).cardsList should be(expectedField)

  it should "have Confirm as the only available action after Observe" in:
    firstTurn.act(Observe).actions should be(Confirm :: Nil)

  it should "restore the board after executing the Confirm action" in:
    for player <- Player.values
    do
      val afterConfirm = FirstTurn(board, player).actAll(Observe :: Confirm :: Nil)
      afterConfirm.board.getField(player) should be(board.getField(player))
      afterConfirm.board.getField(player.other) should be(board.getField(player.other))

  it should "end after Confirm action" in:
    firstTurn.act(Observe).act(Confirm).actions should be(EndTurn :: Nil)

  it should "be over after EndTurn" in:
    firstTurn.act(Observe).act(Confirm).act(EndTurn).isOver should be(true)

  it should "throw an IllegalArgumentException when the action is not in the actions list" in:
    an[IllegalArgumentException] should be thrownBy firstTurn.act(Confirm)
    an[IllegalArgumentException] should be thrownBy firstTurn.act(EndTurn)
    an[IllegalArgumentException] should be thrownBy firstTurn.act(Observe).act(Observe)
