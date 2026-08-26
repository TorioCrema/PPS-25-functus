package org.pps.functus
package model

import model.playable.turn.Turns.*
import model.board.Player.Player1
import model.opponent.Opponent
import model.playable.turn.Action.*
import model.deck.sugar.CardDSL.*
import model.deck.card.Suit.*
import model.deck.sugar.FieldDSL.given
import model.deck.sugar.BoardDSL.*
import model.deck.sugar.DeckDSL.*
import model.deck.sugar.DeckDSL.deck.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class OpponentTest extends AnyFlatSpec with Matchers:
  private val opponentField = (six of Swords) and (five of Wands)
  private val otherField = (three of Cups) and (seven of Pentacles) and (four of Wands) and (six of Pentacles)
  private val longOpponentField = (ace of Swords) and (three of Wands) and (jack of Wands) and (four of Pentacles)
  private val shortBoard = board from default withCustom playerOne(opponentField) withCustom playerTwo(otherField)
  private val longBoard = board from default withCustom playerOne(longOpponentField) withCustom playerTwo(otherField)
  private val firstTurn = FirstTurn(longBoard, Player1)

  "Opponent" should "observe and remember observed cards" in:
    val opponent = Opponent()
    val (_, chosenAction) = opponent.play(firstTurn)
    chosenAction should be(Observe)
    for i <- 0 until 2 do opponent.getKnownCard(i) should be(Some(longOpponentField.cardsList(i)))
    for i <- 2 until longOpponentField.length do opponent.getKnownCard(i) should be(None)

  it should "Confirm and remember known cards" in:
    val opponent = Opponent()
    val (afterObserve, _) = opponent.play(firstTurn)
    val (_, chosenAction) = opponent.play(afterObserve)
    chosenAction should be(Confirm)
    for i <- 0 until 2 do opponent.getKnownCard(i) should be(Some(longOpponentField.cardsList(i)))
    for i <- 2 until longOpponentField.length do opponent.getKnownCard(i) should be(None)

  it should "DrawKing when available" in:
    val boardWithDrawableKing = firstTurn.board.discard(king of Cups)
    val drawKingTurn = SimpleTurn(boardWithDrawableKing, Player1)
    Opponent().play(drawKingTurn)._2 should be(DrawKing)

  it should "Draw when DrawKing is unavailable" in:
    Opponent().play(SimpleTurn(firstTurn.board, Player1))._2 should be(Draw)

  it should "Activate after drawing" in:
    val opponent = Opponent()
    val afterDraw = opponent.play(SimpleTurn(firstTurn.board, Player1))._1
    val (_, chosenAction) = opponent.play(afterDraw)
    chosenAction should be(Activate)

  it should "Discard when top of discard stack value matches a known card value" in:
    val boardWithDiscard = shortBoard.discard(six of Wands)
    val opponent = Opponent()
    val afterObserve = opponent.play(FirstTurn(boardWithDiscard, Player1))._1
    opponent.play(afterObserve)
    val discardTurn = SimpleTurn(boardWithDiscard, Player1)
    val (_, chosenAction) = opponent.play(discardTurn)
    chosenAction should be(ChooseDiscard(0))
    opponent.getKnownCard(0) should be(Some(five of Wands))
    opponent.getKnownCard(1) should be(None)

  it should "Replace drawn card with an unknown card whenever possible and remember the new card" in:
    val replaceBoard = longBoard withCustom customDeck(deck from single(four of Cups))
    val opponent = Opponent()
    opponent.play(FirstTurn(replaceBoard, Player1))
    val (afterDraw, _) = opponent.play(SimpleTurn(replaceBoard, Player1))
    val (afterActivate, _) = opponent.play(afterDraw)
    val (_, chosenAction) = opponent.play(afterActivate)
    chosenAction should be(ChooseReplace(2))
    opponent.getKnownCard(2) should be(Some(four of Cups))

  it should "Replace drawn card with known card of max value when all cards are known and remember the new card" in:
    val replaceBoard = shortBoard withCustom customDeck(deck from single(three of Wands))
    val opponent = Opponent()
    opponent.play(FirstTurn(replaceBoard, Player1))
    val (afterDraw, _) = opponent.play(SimpleTurn(replaceBoard, Player1))
    val (afterActivate, _) = opponent.play(afterDraw)
    val (_, chosenAction) = opponent.play(afterActivate)
    chosenAction should be(ChooseReplace(0))
    opponent.getKnownCard(0) should be(Some(three of Wands))

  it should "call cactus when half or more of its field is known and known values amount to five or less" in:
    val replaceBoard = longBoard withCustom customDeck(deck from single(ace of Cups))
    val opponent = Opponent()
    opponent.play(FirstTurn(replaceBoard, Player1))
    val (afterDraw, _) = opponent.play(SimpleTurn(replaceBoard, Player1))
    val (afterActivate, _) = opponent.play(afterDraw)
    val (afterReplace, _) = opponent.play(afterActivate)
    val (afterCactus, chosenAction) = opponent.play(afterReplace)
    chosenAction should be(Cactus)
    val (_, chosenAfterCactus) = opponent.play(afterCactus)
    chosenAfterCactus should be(EndTurn)

  it should "end the turn when calling cactus isn't optimal" in:
    val replaceBoard = longBoard withCustom customDeck(deck from single(knight of Cups))
    val opponent = Opponent()
    opponent.play(FirstTurn(replaceBoard, Player1))
    val (afterDraw, _) = opponent.play(SimpleTurn(replaceBoard, Player1))
    val (afterActivate, _) = opponent.play(afterDraw)
    val (afterReplace, replaced) = opponent.play(afterActivate)
    val (_, chosenAction) = opponent.play(afterReplace)
    chosenAction should be(EndTurn)

  it should "forget known cards" in:
    val opponent = Opponent()
    opponent.play(FirstTurn(shortBoard, Player1))
    opponent.getKnownCard(0) should be(Some(six of Swords))
    opponent.forgetOwn(0)
    opponent.getKnownCard(0) should be(None)

  it should "observe adversary cards and forget them" in:
    val observeBoard = shortBoard withCustom customDeck(deck from single(six of Wands))
    val opponent = Opponent()
    opponent.play(FirstTurn(observeBoard, Player1))
    val (drawnTurn, _) = opponent.play(SimpleTurn(observeBoard, Player1))
    val (activatedTurn, _) = opponent.play(drawnTurn)
    val (observedTurn, selectedAction) = opponent.play(activatedTurn)
    selectedAction should be(ObserveOpponent(0))
    opponent.getKnownAdversaryCard(0) should be(Some(otherField.getCard(0)._1))
    opponent.forgetAdversary(0)
    opponent.getKnownAdversaryCard(0) should be(None)

  it should "observe its cards" in:
    val observeBoard = longBoard withCustom customDeck(deck from single(seven of Wands))
    val opponent = Opponent()
    opponent.play(FirstTurn(observeBoard, Player1))
    val (drawnTurn, _) = opponent.play(SimpleTurn(observeBoard, Player1))
    val (activatedTurn, _) = opponent.play(drawnTurn)
    val (observedTurn, selectedAction) = opponent.play(activatedTurn)
    selectedAction should be(ObservePlayer(2))
    opponent.getKnownCard(2) should be(Some(longOpponentField.getCard(2)._1))

  it should "chose to replace when all its cards are known" in:
    val observeBoard = shortBoard withCustom customDeck(deck from single(five of Wands))
    val opponent = Opponent()
    var (turn, chosenAction) = opponent.play(FirstTurn(observeBoard, Player1))
    while !turn.isOver do turn = opponent.play(turn)._1
    turn = opponent.play(SimpleTurn(turn.board, Player1))._1
    turn = opponent.play(turn)._1
    opponent.play(turn)._2 should be(ChooseReplace(0))

  it should "play a favourable swap" in:
    val swapBoard = shortBoard withCustom customDeck(deck from ((six of Wands) :: (jack of Wands) :: Nil))
    val opponent = Opponent()
    var (turn, chosenAction) = opponent.play(FirstTurn(swapBoard, Player1))
    while !turn.isOver do turn = opponent.play(turn)._1
    turn = SimpleTurn(turn.board, Player1)
    while !turn.isOver do turn = opponent.play(turn)._1
    turn = SimpleTurn(turn.board, Player1)
    while chosenAction != Activate do
      val res = opponent.play(turn)
      chosenAction = res._2
      turn = res._1
    chosenAction = opponent.play(turn)._2
    chosenAction should be(Swap(0, 0))
