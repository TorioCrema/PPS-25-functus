package org.pps.functus
package view

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import model.board.Player.*
import view.CLIView
import view.utils.{GameState, InputMode, Utils, ViewAction}

import org.scalatest.BeforeAndAfterEach

class CLIViewTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach:

  override def beforeEach(): Unit =
    Utils.viewBuilder.clear()

  // Sample helper to construct a default base GameState
  def createBaseGameState(
      inputMode: InputMode = InputMode.ActionMenu,
      selectedAction: Int = 0,
      selectedCardOnBoard: Int = 0,
      winner: Option[model.board.Player] = None,
      playerScore: Int = 0,
      adversaryScore: Int = 0,
      possibleAction: List[ViewAction] = List(ViewAction("draw", "Draw from deck"))
  ): GameState =
    GameState(
      adversaryCard = List(None, None, None, None),
      playerCard = List(None, None, None, None),
      remainingCardInDeck = 24,
      lastDiscardedCard = None,
      cardsInHand = List(None),
      possibleAction = possibleAction,
      inputMode = inputMode,
      selectedAction = selectedAction,
      selectedCardOnBoard = selectedCardOnBoard,
      winner = winner,
      playerScore = playerScore,
      adversaryScore = adversaryScore
    )

  "CLIView.render" should "render standard gameplay components under ActionMenu mode" in {
    val view = new CLIView()
    val state = createBaseGameState(inputMode = InputMode.ActionMenu)

    view.render(state)

    val output = Utils.viewBuilder.toString()
    output should include("ADVERSARY")
    output should include("PLAYER")
    output should include("DECK")
    output should include("DISCARD")
    output should include("AVAILABLE ACTIONS (Use ↑/↓ and Press ENTER):")
    output should include("1. Draw from deck")
  }

  it should "highlight the active selection in ActionMenu mode correctly" in {
    val view = new CLIView()
    val actions = List(
      ViewAction("draw", "Draw from deck"),
      ViewAction("cactus", "Call Cactus!")
    )
    val state = createBaseGameState(
      inputMode = InputMode.ActionMenu,
      selectedAction = 1,
      possibleAction = actions
    )

    view.render(state)

    val output = Utils.viewBuilder.toString()
    output should include("1. Draw from deck")
    output should include("-> 2. Call Cactus!")
  }

  it should "render card targeting instruction prompts when in SelectCardOnBoard mode" in {
    val view = new CLIView()
    val state = createBaseGameState(
      inputMode = InputMode.SelectCardOnBoard,
      selectedCardOnBoard = 2
    )

    view.render(state)

    val output = Utils.viewBuilder.toString()
    output should include(" SELECT A CARD ON THE BOARD (Use ←/→ and press ENTER to exchange):")
    output should include("Selected Card: Position 3")
  }

  it should "render adversary targeting instruction prompts when in SelectAdversaryCardOnBoard mode" in {
    val view = new CLIView()
    val state = createBaseGameState(
      inputMode = InputMode.SelectAdversaryCardOnBoard,
      selectedCardOnBoard = 1
    )

    view.render(state)

    val output = Utils.viewBuilder.toString()
    output should include(" SELECT A CARD ON ADVERSARY BOARD (Use ←/→ and press ENTER to exchange):")
    output should include("Selected Card: Position 2")
  }

  it should "render the WaitingRoom privacy transition layout" in {
    val view = new CLIView()
    val state = createBaseGameState(inputMode = InputMode.WaitingRoom)

    view.render(state)

    val output = Utils.viewBuilder.toString()
    output should include("PLAYER SWAP")
    output should include("Make sure the other player isn't watching!")
    output should include("[ Press ENTER to begin the turn ]")

    // Waiting Room bypasses the standard main play UI components
    output should not include "AVAILABLE ACTIONS"
    output should not include "CARD IN HAND:"
  }

  it should "render victory messages and scores correctly in EndGame mode" in {
    val view = new CLIView()
    val state = createBaseGameState(
      inputMode = InputMode.EndGame,
      winner = Some(Player1),
      playerScore = 12,
      adversaryScore = 25
    )

    view.render(state)

    val output = Utils.viewBuilder.toString()
    output should include("END GAME")
    output should include("GAME IS OVER Player1 WIN")
    output should include("Player 1 has done 12 points | Player 2 has done 25 points")
    output should include("[ Press Q or ENTER to return to main Menu ]")
  }

  it should "render a tie message in EndGame mode when there is no winner" in {
    val view = new CLIView()
    val state = createBaseGameState(
      inputMode = InputMode.EndGame,
      winner = None,
      playerScore = 18,
      adversaryScore = 18
    )

    view.render(state)

    val output = Utils.viewBuilder.toString()
    output should include("GAME IS ENDED IN A TIE")
    output should include("Player 1 has done 18 points | Player 2 has done 18 points")
  }

  it should "properly format deck count and empty discard pile label" in {
    val view = new CLIView()
    val state = createBaseGameState().copy(
      remainingCardInDeck = 15,
      lastDiscardedCard = None
    )

    view.render(state)

    val output = Utils.viewBuilder.toString()
    output should include("15")
    output should include("EMPTY")
  }

  it should "display '[ No card drawn ]' in hand zone when hand is empty" in {
    val view = new CLIView()
    val state = createBaseGameState().copy(cardsInHand = List(None))

    view.render(state)

    val output = Utils.viewBuilder.toString()
    output should include("CARD IN HAND:")
    output should include("[ No card drawn ]")
  }
  
  "CLIView.renderMatchStatus" should "render current match scores and target score correctly" in {
    val view = new CLIView()
    val scores = Map(Player1 -> 15, Player2 -> 22)
    val maxScore = 50
  
    view.renderMatchStatus(scores, maxScore)
  
    val output = Utils.viewBuilder.toString()
    output should include("MATCH IN PROGRESS")
    output should include("TARGET SCORE TO REACH: 50")
    output should include("CURRENT SCORES:")
    output should include("Player 1: 15 pts  |  Player 2: 22 pts")
    output should include("[ Press ENTER to start the next Game ]")
  }
  
  "CLIView.renderMatchEnd" should "render the winner and final scores when match is over" in {
    val view = new CLIView()
    val scores = Map(Player1 -> 52, Player2 -> 30)
    val winner = Some(Player2)
    val maxScore = 50
  
    view.renderMatchEnd(scores, winner, maxScore)
  
    val output = Utils.viewBuilder.toString()
    output should include("MATCH OVER!")
    output should include("🏆 WINNER OF THE MATCH IS Player2! 🏆")
    output should include("FINAL SCORES (Target: 50):")
    output should include("Player 1: 52 pts  |  Player 2: 30 pts")
    output should include("[ Press ENTER or Q to return to Main Menu ]")
  }
  
  it should "render a draw message when match ends with no winner" in {
    val view = new CLIView()
    val scores = Map(Player1 -> 55, Player2 -> 55)
    val winner = None
    val maxScore = 50
  
    view.renderMatchEnd(scores, winner, maxScore)
  
    val output = Utils.viewBuilder.toString()
    output should include("MATCH OVER!")
    output should include("THE MATCH ENDED IN A DRAW!")
    output should include("FINAL SCORES (Target: 50):")
    output should include("Player 1: 55 pts  |  Player 2: 55 pts")
    output should include("[ Press ENTER or Q to return to Main Menu ]")
  }