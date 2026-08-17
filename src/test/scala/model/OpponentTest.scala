package org.pps.functus
package model

import model.board.BoardFactory
import model.turn.Turns.*
import model.board.Player.Player1
import model.opponent.Opponent
import model.turn.Action.*
import model.deck.sugar.CardDSL.*
import model.deck.card.Suit.*
import model.deck.sugar.FieldDSL.given
import model.deck.DeckImpl
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class OpponentTest extends AnyFlatSpec with Matchers:
  private val board = BoardFactory.BoardWithPopulatedFields()
  private val firstTurn = FirstTurn(board, Player1)
  private val observedCards = board.getField(Player1).cardsList.slice(0, 2)

  "Opponent" should "observe and remember observed cards" in:
    val opponent = Opponent()
    val (newTurn, chosenAction) = opponent.play(firstTurn)
    chosenAction should be(Observe)
    for i <- 0 until 2 do opponent.getKnownCard(i) should be(Some(observedCards(i)))
    for i <- 2 until board.getField(Player1).length do opponent.getKnownCard(i) should be(None)

  it should "Confirm and remember known cards" in:
    val opponent = Opponent()
    val (afterObserve, _) = opponent.play(firstTurn)
    val (_, chosenAction) = opponent.play(afterObserve)
    chosenAction should be(Confirm)
    for i <- 0 until 2 do opponent.getKnownCard(i) should be(Some(observedCards(i)))
    for i <- 2 until board.getField(Player1).length do opponent.getKnownCard(i) should be(None)

  it should "DrawKing when available" in:
    val boardWithDrawableKing = board.discard(king of Cups)
    val drawKingTurn = SimpleTurn(boardWithDrawableKing, Player1)
    Opponent().play(drawKingTurn)._2 should be(DrawKing)

  it should "Draw when DrawKing is unavailable" in:
    Opponent().play(SimpleTurn(board, Player1))._2 should be(Draw)

  it should "Activate after drawing" in:
    val opponent = Opponent()
    val afterDraw = opponent.play(SimpleTurn(board, Player1))._1
    val (_, chosenAction) = opponent.play(afterDraw)
    chosenAction should be(Activate)

  it should "Discard when top of discard stack value matches a known card value" in:
    val opponentField = (six of Swords) and (five of Wands)
    val otherField = (three of Cups) and (seven of Pentacles)
    val boardWithDiscard = BoardFactory.CustomBoard(opponentField :: otherField :: Nil).discard(six of Wands)
    val opponent = Opponent()
    val afterObserve = opponent.play(FirstTurn(boardWithDiscard, Player1))._1
    opponent.play(afterObserve)
    val discardTurn = SimpleTurn(boardWithDiscard, Player1)
    val (_, chosenAction) = opponent.play(discardTurn)
    chosenAction should be(ChooseDiscard(0))

  it should "Replace drawn card with an unknown card whenever possible" in:
    val opponentField = (six of Swords) and (five of Wands) and (jack of Wands) and (four of Pentacles)
    val otherField = (three of Cups) and (seven of Pentacles)
    val replaceBoard = BoardFactory.CustomBoard(opponentField :: otherField :: Nil, DeckImpl(Vector(four of Cups)))
    val opponent = Opponent()
    opponent.play(FirstTurn(replaceBoard, Player1))
    val (afterDraw, _) = opponent.play(SimpleTurn(replaceBoard, Player1))
    val (afterActivate, _) = opponent.play(afterDraw)
    val (_, chosenAction) = opponent.play(afterActivate)
    chosenAction should be(ChooseReplace(2))

  it should "Replace drawn card with known card of max value when all cards are known" in:
    val opponentField = (six of Swords) and (five of Wands)
    val otherField = (three of Cups) and (seven of Pentacles)
    val replaceBoard = BoardFactory.CustomBoard(opponentField :: otherField :: Nil, DeckImpl(Vector(three of Wands)))
    val opponent = Opponent()
    opponent.play(FirstTurn(replaceBoard, Player1))
    val (afterDraw, _) = opponent.play(SimpleTurn(replaceBoard, Player1))
    val (afterActivate, _) = opponent.play(afterDraw)
    val (_, chosenAction) = opponent.play(afterActivate)
    chosenAction should be(ChooseReplace(0))
