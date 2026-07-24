package org.pps.functus
package view

import model.board.{BoardFactory, BoardImpl, Player}
import model.turn.Action.*
import model.turn.Turns.*
import model.deck.sugar.CardDSL.*
import model.deck.sugar.FieldDSL.*
import model.deck.sugar.FieldDSL.given
import model.board.Player.*
import model.deck.card.Suit.*

import model.deck.card.Card
import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TurnTest extends AnyFlatSpec with Matchers:
  private val threeOfCups = three of Cups
  private val twoOfSwords = two of Swords
  private val fiveOfWands = five of Wands
  private val aceOfPentacles = ace of Pentacles
  private val player1Field = threeOfCups and twoOfSwords
  private val player2Field = fiveOfWands and aceOfPentacles
  private val board = BoardImpl(players = Map((Player1, player1Field), (Player2, player2Field)))

  "FirstTurn" should "have Confirm as first action" in:
    FirstTurn(BoardFactory(), Player1).actions should be(List(Observe))

  it should "draw from the player's field when observing" in:
    val player1Hand = List(threeOfCups, twoOfSwords)
    val player2Hand = List(fiveOfWands, aceOfPentacles)
    def testObserveDraw(player: Player, expectedHand: List[Card]): Assertion =
      val afterObserve =
        FirstTurn(board, player).act(Observe)
      afterObserve.hand should be(expectedHand)
      afterObserve.board.getField(player).length should be(0)
    List((Player1, player1Hand), (Player2, player2Hand)).foreach((player, hand) => testObserveDraw(player, hand))

  it should "have Confirm as the only available action after Observe" in:
    val afterObserve = FirstTurn(board, Player1).act(Observe)
    afterObserve.actions should be(List(Confirm))

//  it should "restore the board after executing the Confirm action" in:
//    val afterObserve = FirstTurn(board, Player1).act(Observe)
//    afterObserve.board should not be board
//    afterObserve.act(Confirm).board should be(board)
