package org.pps.functus
package model

import model.playable.turn.Turns.*
import model.board.Player.*
import model.opponent.Opponent
import model.playable.turn.Action.*
import model.deck.sugar.CardDSL.*
import model.deck.card.Suit.*
import model.deck.sugar.FieldDSL.given
import model.deck.sugar.BoardDSL.*
import model.deck.sugar.DeckDSL.*
import model.deck.sugar.DeckDSL.deck.*
import model.board.Board
import model.playable.turn.{Action, Turn}

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class OpponentTest extends AnyFlatSpec with Matchers:
  private val opponentField = (six of Swords) and (five of Wands)
  private val otherField = (three of Cups) and (seven of Pentacles) and (four of Wands) and (six of Pentacles)
  private val longOpponentField = (ace of Swords) and (three of Wands) and (jack of Wands) and (four of Pentacles)
  private val adversaryFieldForDiscard = (six of Pentacles) and (jack of Swords)
  private val shortBoard = board from default withCustom playerOne(opponentField) withCustom playerTwo(otherField)
  private val longBoard = board from default withCustom playerOne(longOpponentField) withCustom playerTwo(otherField)
  private val firstTurn = FirstTurn(longBoard, Player1)
  private val observeAndDiscardBoard = board from default withCustom playerOne(opponentField) withCustom
    playerTwo(adversaryFieldForDiscard) withCustom customDeck(
    deck from ((six of Wands) | (six of Pentacles) | (four of Pentacles))
  )

  private def playFirstTurn(board: Board, opponent: Opponent): Turn =
    var turn = opponent.play(FirstTurn(board, Player1))._1
    while !turn.isOver do turn = opponent.play(turn)._1
    turn

  private def playSimpleTurn(board: Board, opponent: Opponent): (Turn, Action) = playSimpleTurn(board, opponent, 100)
  private def playSimpleTurn(board: Board, opponent: Opponent, maxMoves: Int): (Turn, Action) =
    var (turn, lastAction) = opponent.play(SimpleTurn(board, Player1))
    var moves = 1
    while !turn.isOver && moves < maxMoves do
      val (nextPhase, action) = opponent.play(turn)
      moves = moves + 1
      turn = nextPhase
      lastAction = action
    (turn, lastAction)

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
    val opponent = Opponent()
    playSimpleTurn(boardWithDrawableKing, opponent, 1)._2 should be(DrawKing)

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
    playFirstTurn(boardWithDiscard, opponent)
    val (_, chosenAction) = playSimpleTurn(boardWithDiscard, opponent, 1)
    chosenAction should be(ChooseDiscard(0))
    opponent.getKnownCard(0) should be(Some(five of Wands))
    opponent.getKnownCard(1) should be(None)

  it should "Replace drawn card with an unknown card whenever possible and remember the new card" in:
    val replaceBoard = longBoard withCustom customDeck(deck from single(four of Cups))
    val opponent = Opponent()
    playFirstTurn(replaceBoard, opponent)
    val (_, chosenAction) = playSimpleTurn(replaceBoard, opponent, 3)
    chosenAction should be(ChooseReplace(2))
    opponent.getKnownCard(2) should be(Some(four of Cups))

  it should "Replace drawn card with known card of max value when all cards are known and remember the new card" in:
    val replaceBoard = shortBoard withCustom customDeck(deck from single(three of Wands))
    val opponent = Opponent()
    playFirstTurn(replaceBoard, opponent)
    val (_, chosenAction) = playSimpleTurn(replaceBoard, opponent, 3)
    chosenAction should be(ChooseReplace(0))
    opponent.getKnownCard(0) should be(Some(three of Wands))

  it should "call cactus when half or more of its field is known and known values amount to five or less" in:
    val replaceBoard = longBoard withCustom customDeck(deck from single(ace of Cups))
    val opponent = Opponent()
    playFirstTurn(replaceBoard, opponent)
    val (afterCactus, chosenAction) = playSimpleTurn(replaceBoard, opponent, 4)
    chosenAction should be(Cactus)
    val (_, chosenAfterCactus) = opponent.play(afterCactus)
    chosenAfterCactus should be(EndTurn)

  it should "end the turn when calling cactus isn't optimal" in:
    val replaceBoard = longBoard withCustom customDeck(deck from single(knight of Cups))
    val opponent = Opponent()
    playFirstTurn(replaceBoard, opponent)
    val (_, chosenAction) = playSimpleTurn(replaceBoard, opponent, 4)
    chosenAction should be(EndTurn)

  it should "observe adversary cards" in:
    val observeBoard = shortBoard withCustom customDeck(deck from single(six of Wands))
    val opponent = Opponent()
    playFirstTurn(observeBoard, opponent)
    val (_, chosenAction) = playSimpleTurn(observeBoard, opponent, 3)
    chosenAction should be(ObserveOpponent(0))
    opponent.getKnownAdversaryCard(0) should be(Some(otherField.getCard(0)._1))

  it should "forget adversary cards when discarded" in:
    val opponent = Opponent()
    var turn = playFirstTurn(observeAndDiscardBoard, opponent)
    turn = playSimpleTurn(turn.board, opponent)._1
    turn = playSimpleTurn(turn.board, opponent)._1
    val adversaryTurn = SimpleTurn(turn.board, Player2)
    opponent.react(ChooseDiscard(0), adversaryTurn)
    opponent.getKnownAdversaryCard(0) should be(Some(jack of Swords))

  it should "forget adversary cards when replaced" in:
    val opponent = Opponent()
    var turn = playFirstTurn(observeAndDiscardBoard, opponent)
    turn = playSimpleTurn(turn.board, opponent)._1
    turn = playSimpleTurn(turn.board, opponent)._1
    turn = SimpleTurn(turn.board, Player2).actAll(Draw :: Activate :: Nil)
    opponent.react(ChooseReplace(0), turn)
    opponent.getKnownAdversaryCard(0) should be(None)
    opponent.getKnownAdversaryCard(1) should be(Some(adversaryFieldForDiscard.cardsList(1)))

  it should "remember cards when adversary discard is incorrect" in:
    val opponent = Opponent()
    var turn = playFirstTurn(observeAndDiscardBoard, opponent)
    turn = playSimpleTurn(turn.board, opponent)._1
    turn = playSimpleTurn(turn.board, opponent)._1
    turn = SimpleTurn(turn.board, Player2)
    opponent.react(ChooseDiscard(1), turn)
    opponent.getKnownAdversaryCard(0) should be(Some(adversaryFieldForDiscard.cardsList.head))
    opponent.getKnownAdversaryCard(1) should be(Some(adversaryFieldForDiscard.cardsList(1)))

  it should "not alter knowledge when adversary swaps unknown cards" in:
    val opponent = Opponent()
    val swapBoard = shortBoard withCustom CustomDeck(deck from single(jack of Pentacles))
    val turn = SimpleTurn(swapBoard, Player2).actAll(Draw :: Activate :: Nil)
    opponent.react(Swap(0,0), turn)
    opponent.getKnownCard(0) should be(None)
    opponent.getKnownAdversaryCard(0) should be(None)

  it should "alter knowledge when adversary swaps known owned card" in:
    val opponent = Opponent()
    val swapBoard = shortBoard withCustom CustomDeck(deck from single(jack of Pentacles))
    var turn = playFirstTurn(swapBoard, opponent)
    turn = SimpleTurn(turn.board, Player2).actAll(Draw :: Activate :: Nil)
    opponent.react(Swap(0, 0), turn)
    opponent.getKnownCard(0) should be(None)
    opponent.getKnownAdversaryCard(0) should be(Some(opponentField.cardsList.head))

  it should "alter knowledge when adversary swaps known adversary card" in:
    val opponent = Opponent()
    val swapBoard = shortBoard withCustom CustomDeck(deck from ((six of Pentacles) | (jack of Pentacles)))
    var turn = playSimpleTurn(swapBoard, opponent)._1
    turn = SimpleTurn(turn.board, Player2).actAll(Draw :: Activate :: Nil)
    opponent.react(Swap(0, 0), turn)
    opponent.getKnownCard(0) should be(Some(otherField.cardsList.head))

  it should "alter knowledge when adversary swaps two known cards" in:
    val opponent = Opponent()
    val swapBoard = shortBoard withCustom CustomDeck(deck from ((six of Pentacles) | (jack of Pentacles)))
    var turn = playFirstTurn(swapBoard, opponent)
    turn = playSimpleTurn(turn.board, opponent)._1
    turn = SimpleTurn(turn.board, Player2).actAll(Draw :: Activate :: Nil)
    opponent.react(Swap(0, 0), turn)
    opponent.getKnownCard(0) should be(Some(otherField.cardsList.head))
    opponent.getKnownAdversaryCard(0) should be(Some(opponentField.cardsList.head))

  it should "observe its cards" in:
    val observeBoard = longBoard withCustom customDeck(deck from single(seven of Wands))
    val opponent = Opponent()
    playFirstTurn(observeBoard, opponent)
    val (_, selectedAction) = playSimpleTurn(observeBoard, opponent, 3)
    selectedAction should be(ObservePlayer(2))
    opponent.getKnownCard(2) should be(Some(longOpponentField.getCard(2)._1))

  it should "chose to replace when all its cards are known" in:
    val observeBoard = shortBoard withCustom customDeck(deck from single(five of Wands))
    val opponent = Opponent()
    playFirstTurn(observeBoard, opponent)
    val (_, chosenAction) = playSimpleTurn(observeBoard, opponent, 3)
    chosenAction should be(ChooseReplace(0))

  it should "play a favourable swap" in:
    val swapBoard = shortBoard withCustom customDeck(deck from ((six of Wands) | (jack of Wands)))
    val opponent = Opponent()
    playFirstTurn(swapBoard, opponent)
    val (turn, _) = playSimpleTurn(swapBoard, opponent)
    val (afterSwap, chosenAction) = playSimpleTurn(turn.board, opponent, 5)
    chosenAction should be(Swap(0, 0))
    opponent.getKnownCard(0) should be(Some(afterSwap.board.players(Player1).getCard(0)._1))
    opponent.getKnownAdversaryCard(0) should be(Some(afterSwap.board.players(Player2).getCard(0)._1))

  it should "remember where the player puts the king it has drawn" in:
    val boardWithDrawableKing = firstTurn.board.discard(king of Cups)
    var turn = SimpleTurn(boardWithDrawableKing, Player2)
    val opponent = Opponent()
    opponent.react(DrawKing, turn)
    turn = turn.actAll(DrawKing :: Activate :: Nil)
    opponent.react(ChooseReplace(0), turn)
    opponent.getKnownAdversaryCard(0) should be(Some(king of Cups))