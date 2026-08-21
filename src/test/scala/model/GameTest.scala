package org.pps.functus
package model

import model.deck.sugar.BoardDSL.*
import model.game.{Game, GamePhase}
import model.turn.Action.{Confirm, EndTurn, Observe}
import model.board.Player.{Player1, Player2}
import model.game.GamePhase.*

import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class GameTest extends AnyFlatSpec with Matchers:

  private def playFirstTurn(game: Game): Game =
    game.act(Observe).get.act(Confirm).get.act(EndTurn).get
  private def playBothFirstTurns(game: Game): Game =
    playFirstTurn(playFirstTurn(game))
  private def boardTest = default board

  "A new Game" should "start in the FirstTurns phase" in:
    val game = Game(boardTest)
    game.phase should be(FirstTurns)

  it should "start with Player1 as the current player" in:
    val game = Game(boardTest)
    game.currentPlayer should be(Player1)

  it should "expose Observe as the only available action at the beginning" in:
    val game = Game(boardTest)
    game.currentTurn.actions should be(List(Observe))

  it should "not be over at the start" in:
    val game = Game(boardTest)
    game.isOver should be(false)

  "A game while current player1" should "remain in FirstTurns after Player1 observes" in:
    val game = Game(boardTest).act(Observe).get
    game.phase shouldBe FirstTurns

  it should "still be Player1's turn after Observe" in:
    val game = Game(boardTest).act(Observe).get
    game.currentPlayer shouldBe Player1

  it should "expose only Confirm after Observe" in:
    val game = Game(boardTest).act(Observe).get
    game.currentTurn.actions shouldBe List(Confirm)

  it should "expose only EndTurn after Confirm" in:
    val game = Game(boardTest).act(Observe).get.act(Confirm).get
    game.currentTurn.actions shouldBe List(EndTurn)

  "Switching players new game" should "switch to Player2 after Player1 completes the first turn" in:
    val game = playFirstTurn(Game(boardTest))
    game.currentPlayer shouldBe Player2

  it should "remain in FirstTurns after Player1 finishes" in:
    val game = playFirstTurn(Game(boardTest))
    game.phase shouldBe FirstTurns

  it should "give Player2 Observe as first available action" in:
    val game = playFirstTurn(Game(boardTest))
    game.currentTurn.actions shouldBe List(Observe)

  "A game from first turn to simple turn" should "transition to Playing after both players complete their first turns" in:
    val game = playBothFirstTurns(Game(boardTest))
    game.phase shouldBe Playing

  it should "have Player1 as the current player when Playing begins" in:
    val game = playBothFirstTurns(Game(boardTest))
    game.currentPlayer shouldBe Player1

  it should "not be over after the first turns phase" in:
    val game = playBothFirstTurns(Game(boardTest))
    game.isOver shouldBe false

  it should "leave each player with 4 cards in their field after the first turns" in:
    val game = playBothFirstTurns(Game(boardTest))
    game.board.getField(Player1).length shouldBe 4
    game.board.getField(Player2).length shouldBe 4

  it should "have no cactusCaller set after the first turns phase" in:
    val game = playBothFirstTurns(Game(boardTest))
    game.cactusCaller shouldBe None

  "act" should "return None if called on a finished game" in:
    val overGame = Game(boardTest).copy(phase = Over)
    overGame.act(Observe) shouldBe None

  "finalCards" should "return None before the game is over" in:
    Game(boardTest).finalCards shouldBe None

  "playerScore" should "return a score for each player" in:
    val game = playBothFirstTurns(Game(boardTest))
    game.playerScore.keySet shouldBe Set(Player1, Player2)

  it should "be consistent with the cards on the field" in:
    val game = playBothFirstTurns(Game(boardTest))
    val expectedP1 = game.board.getField(Player1).cardsList.map(_.value).sum
    val expectedP2 = game.board.getField(Player2).cardsList.map(_.value).sum
    game.playerScore shouldBe Map(Player1 -> expectedP1, Player2 -> expectedP2)
