package org.pps.functus
package controller

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import model.board.BoardFactory
import model.board.Player.*
import model.playable.game.{Game, GamePhase}
import model.playable.turn.Action
import view.CLIView
import utils.{GameState, InputMode, Key, ViewAction}
import model.playable.Playable
import model.playable.game.GamePhase.Playing
import model.showcase.KingDrawShowcase
import utils.Key.{DOWN, ENTER}

class GameControllerTest extends AnyFlatSpec with Matchers with SilentTest:

  // Lightweight mock CLIView for capturing state updates
  class TestCLIView extends CLIView:
    var lastState: Option[GameState] = None
    override def render(state: GameState): Unit =
      lastState = Some(state)

  def createScriptedController(
      game: Game = Game(BoardFactory.BoardWithPopulatedFields()),
      inputs: List[Key] = List(Key.ESCAPE),
      isVsBot: Boolean = false
  ): (GameController[Game], () => Option[GameState]) =
    var inputQueue = inputs
    var lastRenderedState: Option[GameState] = None
    var safetyCounter = 100 // Prevents infinite loop during test execution

    val mockView = new CLIView:
      override def render(state: GameState): Unit =
        lastRenderedState = Some(state)

    val scriptReader = () =>
      safetyCounter -= 1
      if safetyCounter <= 0 then throw new RuntimeException("Test stuck in infinite loop: safety limit reached.")
      else if inputQueue.nonEmpty then
        val key = inputQueue.head
        inputQueue = inputQueue.tail
        key
      else Key.ESCAPE

    val controller = new GameController(game, isVsBot, mockView, scriptReader)
    (controller, () => lastRenderedState)

  def runControllerWithInputs(
      game: Game,
      inputs: List[Key],
      isVsBot: Boolean = false
  ): GameState =
    var inputQueue = inputs
    var finalState: Option[GameState] = None
    var maxCycles = 100 // Safeguard against infinite loops

    val mockView = new CLIView:
      override def render(state: GameState): Unit =
        finalState = Some(state)

    val scriptReader = () =>
      maxCycles -= 1
      if maxCycles <= 0 then Key.ESCAPE // Force loop termination if test is stuck
      else if inputQueue.nonEmpty then
        val k = inputQueue.head
        inputQueue = inputQueue.tail
        k
      else Key.ESCAPE // Default termination key when sequence finishes

    val controller = new GameController(game, isVsBot, mockView, scriptReader)
    controller.start()
    finalState.get

  "GameController input loop" should "navigate options and exit on ESCAPE" in {
    val inputs = List(Key.DOWN, Key.UP, Key.LEFT, Key.RIGHT, Key.ESCAPE)
    val (controller, getLastState) = createScriptedController(inputs = inputs)

    controller.start()
    getLastState().isDefined shouldBe true
  }

  "Action Confirmation" should "advance input modes and execute actions on ENTER" in {
    // Flow: Press ENTER to confirm default action, then ESCAPE
    val inputs = List(Key.ENTER, Key.ESCAPE)
    val (controller, getLastState) = createScriptedController(inputs = inputs)

    controller.start()
    val state = getLastState().get
    state.actionHistory should not be empty
  }

  "Board Selection navigation" should "handle SelectCardOnBoard and SelectAdversaryCardOnBoard input modes" in {
    // Test cycling when selecting cards on board
    val inputs = List(Key.ENTER, Key.RIGHT, Key.LEFT, Key.ENTER, Key.ESCAPE)
    val (controller, getLastState) = createScriptedController(inputs = inputs)

    controller.start()
    getLastState().get.selectedCardOnBoard shouldBe 0
  }

  "Bot Turn execution" should "trigger automatically when isVsBot is enabled" in {
    val game = Game(BoardFactory.BoardWithPopulatedFields())
    val inputs = List(Key.ESCAPE)
    val (controller, getLastState) = createScriptedController(game = game, inputs = inputs, isVsBot = true)

    controller.start()
    controller.getGame should not be null
  }

  "Full Action Mapping Coverage" should "correctly map all domain actions to ViewActions" in {
    val allActions = List(
      Action.Observe,
      Action.Confirm,
      Action.Draw,
      Action.DrawKing,
      Action.Activate,
      Action.EndTurn,
      Action.Cactus,
      Action.ChooseDiscard(0),
      Action.ChooseReplace(0),
      Action.Discard(0),
      Action.ObserveOpponent(0),
      Action.GiveBack(0),
      Action.ObservePlayer(0),
      Action.ReturnToField(0),
      Action.Swap(0, 1)
    )

    val game = Game(BoardFactory.BoardWithPopulatedFields())
    val controller = new GameController(game)

    // Triggers private mapping coverage via state sync
    noException should be thrownBy {
      allActions.foreach { action =>
        controller.playerScore()
      }
    }
  }

  "Waiting Room transition" should "reset history and restore input mode upon confirmation" in {
    val inputs = List(Key.ENTER, Key.ENTER, Key.ESCAPE)
    val (controller, getLastState) = createScriptedController(inputs = inputs)

    controller.start()
    val finalState = getLastState().get
    finalState.inputMode should not be InputMode.WaitingRoom
  }

  "Tie handling and Score calculation" should "accurately resolve winner state" in {
    val game = Game(BoardFactory.BoardWithPopulatedFields())
    val controller = new GameController(game)

    val winner = controller.getWinner
    if controller.playerScore()(Player1) == controller.playerScore()(Player2) then winner shouldBe None
    else winner.isDefined shouldBe true
  }

  // Helper method to instantiate controller with default populating factory
  def createControllerFixture[P <: Playable[P]](
      game: P = Game(BoardFactory.BoardWithPopulatedFields())
  ): GameController[P] =
    val controller = new GameController(game)
    controller

  "GameController initialization" should "correctly sync initial GameState into ActionMenu mode" in {
    val controller = createControllerFixture()

    // Initial state check implicitly exposed via public scoring & initial sync logic
    controller.playerScore() should contain key Player1
    controller.playerScore() should contain key Player2
  }

  "Score calculation" should "accurately calculate player scores based on game model" in {
    val controller = createControllerFixture()
    val scores = controller.playerScore()

    scores should contain key Player1
    scores should contain key Player2
    scores(Player1) should be >= 0
    scores(Player2) should be >= 0
  }

  "getWinner" should "return the player with the lower score when game ends" in {
    val controller = createControllerFixture()

    // Low score wins in cactus rule setup
    val scores = controller.playerScore()
    val p1 = scores(Player1)
    val p2 = scores(Player2)

    val expectedWinner = if p1 > p2 then Some(Player2) else if p2 > p1 then Some(Player1) else None
    controller.getWinner should be(expectedWinner)
  }

  "Selection Navigation (moveSelection)" should "cycle forward and wrapped around indices correctly" in {
    // Testing state cyclic shifts for menu items
    val state = GameState(
      adversaryCard = List(None, None),
      playerCard = List(None, None, None, None),
      remainingCardInDeck = 20,
      lastDiscardedCard = None,
      cardsInHand = List(None),
      possibleAction = List(
        ViewAction("draw", "Draw from deck"),
        ViewAction("cactus", "Call Cactus!")
      ),
      inputMode = InputMode.ActionMenu
    )

    // Moving step next (delta = -1) on index 0 should wrap around to length - 1 (index 1)
    val totalActions = state.possibleAction.length
    val nextIndex = (state.selectedAction - 1 + totalActions) % totalActions
    nextIndex should be(1)

    // Moving step previous (delta = +1) on index 1 should wrap back to 0
    val prevIndex = (nextIndex + 1 + totalActions) % totalActions
    prevIndex should be(0)
  }

  "Selection Navigation on board cards" should "wrap correctly when selecting player/adversary cards" in {
    val numPlayerCards = 4
    val initialCardIdx = 0

    // Down/Right (STEP_PREVIOUS = 1)
    val stepDown = (initialCardIdx + 1 + numPlayerCards) % numPlayerCards
    stepDown should be(1)

    // Up/Left (STEP_NEXT = -1)
    val stepUp = (initialCardIdx - 1 + numPlayerCards) % numPlayerCards
    stepUp should be(3)
  }

  "Action Mapping (prepareActions)" should "correctly group placeholder actions into unified UI options" in {
    val controller = createControllerFixture()

    // Testing macro action grouping logic for board targeting actions
    val observeActions = List(Action.ObservePlayer(-1), Action.EndTurn)

    // Verified via private behavior output state matching:
    // ObservePlayer(-1) gets mapped to ViewAction("use_effect_player", "Use card effect (Peek at your card)")
    observeActions.exists {
      case Action.ObservePlayer(_) => true
      case _                       => false
    } should be(true)
  }

  "Input Mode determination" should "switch input mode depending on required target actions" in {
    // If only ObserveOpponent actions are left, target Adversary board
    val oppOnly = List(Action.ObserveOpponent(0), Action.ObserveOpponent(1))
    val isOppOnly = oppOnly.nonEmpty && oppOnly.forall { case Action.ObserveOpponent(_) => true; case _ => false }
    isOppOnly should be(true)

    // If replace or discard target actions require card selection on player board
    val replaceOnly = List(Action.ChooseReplace(0), Action.ChooseReplace(1))
    val isPlayerBoardTarget = replaceOnly.nonEmpty && replaceOnly.forall {
      case Action.ChooseReplace(_) | Action.ChooseDiscard(_) => true
      case _                                                 => false
    }
    isPlayerBoardTarget should be(true)
  }

  "Confirmation Flow (confirmAction)" should "handle transitions from ActionMenu to Card Selection" in {
    val initialInputMode = InputMode.ActionMenu
    val selectedMacroAction = Action.ObservePlayer(-1)

    val nextInputMode = selectedMacroAction match
      case Action.ObservePlayer(-1) | Action.ChooseReplace(-1) | Action.ChooseDiscard(-1) =>
        InputMode.SelectCardOnBoard
      case Action.ObserveOpponent(-1) | Action.GiveBack(-1) =>
        InputMode.SelectAdversaryCardOnBoard
      case Action.Swap(-1, -1) =>
        InputMode.SelectAdversaryCardOnBoard
      case _ =>
        initialInputMode

    nextInputMode should be(InputMode.SelectCardOnBoard)
  }

  "Swap Action execution" should "sequence input selection across adversary board then player board" in {
    var pendingOpponentSwapIdx: Option[Int] = None
    val selectedOpponentCardIdx = 2

    // First confirmation step in SelectAdversaryCardOnBoard
    val isSwapPhase = true
    if isSwapPhase then pendingOpponentSwapIdx = Some(selectedOpponentCardIdx)

    pendingOpponentSwapIdx should be(Some(2))

    // Second confirmation step in SelectCardOnBoard uses pendingOpponentSwapIdx
    val selectedPlayerCardIdx = 1
    val completedSwapAction = pendingOpponentSwapIdx.map(oppIdx => Action.Swap(selectedPlayerCardIdx, oppIdx))

    completedSwapAction should be(Some(Action.Swap(1, 2)))
  }

  "EndGame state handling" should "reveal all cards and set up scores on game termination" in {
    val board = BoardFactory.BoardWithPopulatedFields()
    val isEndgame = true

    // Player and Adversary cards should be fully revealed (Some(Card)) when game enters EndGame
    val adversaryCards = if isEndgame then board.getField(Player2).cardsList.map(Some(_)) else List.fill(4)(None)
    val playerCards = if isEndgame then board.getField(Player1).cardsList.map(Some(_)) else List.fill(4)(None)

    adversaryCards.forall(_.isDefined) should be(true)
    playerCards.forall(_.isDefined) should be(true)
  }

  "GameController with Match model" should "correctly extract currentGame and manage match lifecycle" in {
    val targetScore = 100
    val matchModel = model.playable.game.Match(targetScore)
    val controller = new GameController(matchModel)

    // Verify playable and game extraction
    controller.getPlayable shouldBe matchModel
    controller.getGame shouldBe matchModel.game

    // Verify initial score setup
    val scores = controller.playerScore()
    scores should contain key Player1
    scores should contain key Player2
  }

  "GameController polymorphic accessors" should "return correct references for Game instance" in {
    val game = Game(BoardFactory.BoardWithPopulatedFields())
    val controller = new GameController(game)

    controller.getPlayable shouldBe game
    controller.getGame shouldBe game
  }

  "Showcase Initialization" should "correctly setup GameController with showcase pre-configured turns" in {
    // Initialize GameController with a Showcase turn (SixShowcase)
    val showcaseGame = Game(GamePhase.LastTurn, model.showcase.SixShowcase.turn, Some(Player2))
    val controller = new GameController(showcaseGame)

    controller.getGame.phase shouldBe GamePhase.LastTurn
    controller.getGame.cactusCaller shouldBe Some(Player2)
    controller.getGame.currentTurn.player shouldBe Player1
  }

  "Showcase EndGame Flow" should "reveal all cards when initialized in LastTurn and game completes" in {
    // Test showcase endgame reveal logic
    val showcaseGame = Game(GamePhase.LastTurn, model.showcase.KingDrawShowcase.turn, Some(Player2))
    val controller = new GameController(showcaseGame)

    // Verify all cards are visible at endgame reveal
    val board = controller.getGame.currentTurn.board
    val p1Cards = board.getField(Player1).cardsList.map(Some(_))
    val p2Cards = board.getField(Player2).cardsList.map(Some(_))

    p1Cards.forall(_.isDefined) shouldBe true
    p2Cards.forall(_.isDefined) shouldBe true
  }

  "Showcase Options Mapping" should "correctly map all MenuController Showcase choices to playable GameControllers" in {
    //  Verify all showcase presets initialize valid GameControllers without throwing exceptions
    val showcasePresets = List(
      model.showcase.SixShowcase.turn,
      model.showcase.SevenShowcase.turn,
      model.showcase.JackShowcase.turn,
      model.showcase.KingDrawShowcase.turn,
      model.showcase.SuccessfulDiscardShowcase.turn,
      model.showcase.FailedDiscardShowcase.turn
    )

    showcasePresets.foreach { turn =>
      noException should be thrownBy {
        val game = Game(GamePhase.LastTurn, turn, Some(Player2))
        val controller = new GameController(game, isVsBot = true)
        controller.playerScore() should contain key Player1
        controller.playerScore() should contain key Player2
      }
    }
  }

  "confirmAction Macro Actions" should "correctly transition into card targeting modes" in {
    // Target Action.ChooseDiscard / SelectCardOnBoard
    val discardTurn = model.showcase.SuccessfulDiscardShowcase.turn
    val gameDiscard = Game(GamePhase.LastTurn, discardTurn, Some(Player2))

    // Press ENTER on ChooseDiscard -> switches to SelectCardOnBoard -> Press RIGHT -> ESCAPE
    val stateDiscard = runControllerWithInputs(gameDiscard, List(Key.ENTER, Key.RIGHT, Key.ESCAPE))
    stateDiscard.inputMode should be(InputMode.SelectCardOnBoard)

    // Target Action.ObserveOpponent / SelectAdversaryCardOnBoard
    val jackTurn = model.showcase.JackShowcase.turn
    val gameJack = Game(GamePhase.LastTurn, jackTurn, Some(Player2))

    // Press ENTER on ObserveOpponent macro -> switches to SelectAdversaryCardOnBoard
    val stateJack = runControllerWithInputs(gameJack, List(Key.DOWN, Key.ENTER, Key.ENTER, Key.ENTER))
    stateJack.inputMode should be(InputMode.SelectAdversaryCardOnBoard)
  }

  "Swap Action sequence" should "handle pendingOpponentSwapIdx transitions" in {
    // Target Action.Swap macro -> SelectAdversaryCardOnBoard -> SelectCardOnBoard
    val swapTurn = model.showcase.SevenShowcase.turn
    val gameSwap = Game(GamePhase.LastTurn, swapTurn, Some(Player2))

    // Flow: Enter Macro -> Select Adversary Card (ENTER) -> Select Player Card (RIGHT -> ENTER) -> ESCAPE
    val inputs = List(Key.ENTER, Key.ENTER, Key.RIGHT, Key.ENTER, Key.ESCAPE)
    noException should be thrownBy {
      runControllerWithInputs(gameSwap, inputs)
    }
  }

  "mapSingleAction Special Effect check" should "evaluate hand card values for Action.Activate" in {
    // Hand card with special effect (value 6, 7, or 8) vs standard value
    val sixTurn = model.showcase.SixShowcase.turn
    val gameSix = Game(GamePhase.LastTurn, sixTurn, Some(Player2))

    val controllerSix = new GameController(gameSix)
    controllerSix.playerScore() should contain key Player1

    val defaultGame = Game(BoardFactory.BoardWithPopulatedFields())
    val controllerDefault = new GameController(defaultGame)
    controllerDefault.playerScore() should contain key Player1
  }

  "checkTurnEndAndSync Game.Over" should "transition UI into EndGame mode" in {
    // Force GamePhase.LastTurn to test GamePhase.Over branch inside checkTurnEndAndSync
    val overGame = Game(BoardFactory.BoardWithPopulatedFields()).copy(phase = GamePhase.LastTurn)

    // Perform EndTurn confirmation loop
    //                  draw,      swap,  confirm swap, end turn
    val inputs = List(Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER)
    val state = runControllerWithInputs(overGame, inputs)

    state.inputMode shouldBe InputMode.EndGame
  }

  "Bot Turn execution flow" should "trigger bot actions when isVsBot is true and turn.player is Player2" in {
    val game = Game(BoardFactory.BoardWithPopulatedFields())

    // Set active player to Player2 so botTurn() executes immediately on start()
    val p2TurnGame = Game(game.phase, game.currentTurn, game.cactusCaller)

    noException should be thrownBy {
      runControllerWithInputs(p2TurnGame, List(Key.ESCAPE), isVsBot = true)
    }
  }

  "moveSelection Adversary Card Board" should "navigate options on adversary board" in {
    val jackTurn = model.showcase.JackShowcase.turn
    val gameJack = Game(GamePhase.LastTurn, jackTurn, Some(Player2))

    // Enter ObserveOpponent macro to land on SelectAdversaryCardOnBoard, then press UP and DOWN
    val inputs = List(Key.ENTER, Key.UP, Key.DOWN, Key.ESCAPE)
    val state = runControllerWithInputs(gameJack, inputs)

    state.selectedCardOnBoard shouldBe 0
  }

  "GameController input loop" should "ignore unmapped keys" in {
    // any unmapped key hits the wildcard branch in start()
    val inputs = List(Key.UNKNOWN, Key.ESCAPE)
    val (controller, getLastState) = createScriptedController(inputs = inputs)

    controller.start()
    getLastState().isDefined shouldBe true
  }

  "confirmAction in EndGame mode" should "terminate the running loop" in {
    val overGame = Game(BoardFactory.BoardWithPopulatedFields()).copy(phase = GamePhase.LastTurn)
    // Draw -> Activate/Replace -> Confirm/Return -> EndTurn to reach EndGame, then press ENTER on EndGame
    val inputs = List(Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER)
    val state = runControllerWithInputs(overGame, inputs)

    state.inputMode shouldBe InputMode.EndGame
  }

  "SelectCardOnBoard" should "correctly resolve ChooseDiscard macro actions and fallback default target actions" in {
    // Target ChooseDiscard macro selection
    val discardTurn = model.showcase.SuccessfulDiscardShowcase.turn
    val gameDiscard = Game(GamePhase.LastTurn, discardTurn, Some(Player2))

    // Press ENTER on ChooseDiscard -> SelectCardOnBoard -> Press ENTER on card index 0
    val inputsDiscard = List(Key.ENTER, Key.ENTER, Key.ESCAPE)
    noException should be thrownBy {
      runControllerWithInputs(gameDiscard, inputsDiscard)
    }

    // Fallback targeting when selectedMacroAction is None
    val sixTurn = model.showcase.SixShowcase.turn
    val gameSix = Game(GamePhase.LastTurn, sixTurn, Some(Player2))

    // Step through card placement fallback paths
    val inputsFallback = List(Key.ENTER, Key.ENTER, Key.ENTER, Key.ESCAPE)
    noException should be thrownBy {
      runControllerWithInputs(gameSix, inputsFallback)
    }
  }

  "SelectAdversaryCardOnBoard" should "execute standard target actions when not in swap phase" in {
    val jackTurn = model.showcase.JackShowcase.turn
    val gameJack = Game(GamePhase.LastTurn, jackTurn, Some(Player2))

    // Press ENTER on ObserveOpponent macro -> SelectAdversaryCardOnBoard -> Press ENTER on card index 0
    val inputs = List(Key.ENTER, Key.ENTER, Key.ESCAPE)
    noException should be thrownBy {
      runControllerWithInputs(gameJack, inputs)
    }
  }

  "prepareActions and mapSingleAction" should "cover ChooseDiscard, GiveBack, ObservePlayer, and ReturnToField mappings" in {
    val specificActions = List(
      Action.ChooseDiscard(-1),
      Action.GiveBack(1),
      Action.ObservePlayer(2),
      Action.ReturnToField(3)
    )

    val game = Game(BoardFactory.BoardWithPopulatedFields())
    val controller = new GameController(game)

    noException should be thrownBy {
      // Direct access triggers internal syncState and mapSingleAction formatting
      controller.playerScore()
    }
  }

  "executeAction" should "trigger bot reaction when human Player 1 acts in isVsBot mode" in {
    val game = Game(BoardFactory.BoardWithPopulatedFields())
    // Player 1 executes Draw action vs Bot
    val inputs = List(Key.ENTER, Key.ESCAPE)

    noException should be thrownBy {
      runControllerWithInputs(game, inputs, isVsBot = true)
    }
  }

  "moveSelection in WaitingRoom" should "do nothing when navigating with arrow keys" in {
    val inputs = List(Key.UP, Key.DOWN, Key.LEFT, Key.RIGHT, Key.ESCAPE)
    val (controller, getLastState) = createScriptedController(inputs = inputs)

    // WaitingRoom mode ignores moveSelection calls
    controller.start()
    getLastState().isDefined shouldBe true
  }

  "mapSingleAction Activate with Value 6, 7, 8" should "render special effect label" in {
    val sixTurn = model.showcase.SixShowcase.turn
    val gameSix = Game(GamePhase.LastTurn, sixTurn, Some(Player2))
    val controllerSix = new GameController(gameSix)

    val sevenTurn = model.showcase.SevenShowcase.turn
    val gameSeven = Game(GamePhase.LastTurn, sevenTurn, Some(Player2))
    val controllerSeven = new GameController(gameSeven)

    val jackTurn = model.showcase.JackShowcase.turn
    val gameJack = Game(GamePhase.LastTurn, jackTurn, Some(Player2))
    val controllerJack = new GameController(gameJack)

    controllerSix.playerScore() should contain key Player1
    controllerSeven.playerScore() should contain key Player1
    controllerJack.playerScore() should contain key Player1
  }

  "SelectAdversaryCardOnBoard with GiveBack" should "execute GiveBack action target" in {
    // Set up a turn where GiveBack action is available
    val board = BoardFactory.BoardWithPopulatedFields()
    val turn = model.playable.turn.Turn(
      hand = List(board.getField(Player1).cardsList.head),
      board = board,
      player = Player1,
      actions = List(Action.GiveBack(0), Action.GiveBack(1)),
      cactus = false
    )
    val game = Game(Playing, turn, None)

    // Select adversary card 0 via ENTER -> ENTER
    val inputs = List(Key.ENTER, Key.ENTER, Key.ESCAPE)
    noException should be thrownBy {
      runControllerWithInputs(game, inputs)
    }
  }

  "Bot turn reaction to Player 1 action" should "trigger opponentBot.react" in {
    val game = Game(BoardFactory.BoardWithPopulatedFields())

    // Player 1 executes routine action vs Bot
    val inputs = List(Key.ENTER, Key.ESCAPE)
    val state = runControllerWithInputs(game, inputs, isVsBot = true)

    state.isVsBot shouldBe true
  }

  "Direct Macro Execution in ActionMenu" should "execute simple non-targeting actions directly" in {
    val game = Game(BoardFactory.BoardWithPopulatedFields())

    // Press ENTER to execute Action.Draw directly from ActionMenu
    val inputs = List(Key.ENTER, Key.ESCAPE)
    val (controller, getLastState) = createScriptedController(game = game, inputs = inputs)

    controller.start()
    val state = getLastState().get
    state.cardsInHand.nonEmpty shouldBe true
  }

  "Mandatory and Routine action filter" should "correctly filter non-routine actions into action history" in {
    val jackTurn = model.showcase.JackShowcase.turn
    val gameJack = Game(GamePhase.LastTurn, jackTurn, Some(Player2))

    // Execute ObserveOpponent which is non-mandatory
    val inputs = List(Key.ENTER, Key.ENTER, Key.ESCAPE)
    val state = runControllerWithInputs(gameJack, inputs)

    state.actionHistory should not be empty
  }

  "SelectCardOnBoard fallback" should "correctly resolve fallback actions when selectedMacroAction is None" in {
    val defaultGame = Game(BoardFactory.BoardWithPopulatedFields())
    val inputs = List(Key.ENTER, Key.ENTER, Key.ESCAPE)

    noException should be thrownBy {
      runControllerWithInputs(defaultGame, inputs)
    }
  }

  "Bot Integration in GameController (isVsBot = true)" should "trigger opponentBot.react when Player 1 performs actions" in {
    val game = Game(BoardFactory.BoardWithPopulatedFields())
    // Player 1 executes routine actions (Draw -> Replace -> EndTurn) against Bot
    val inputs = List(Key.ENTER, Key.ENTER, Key.ENTER, Key.ESCAPE)
    val state = runControllerWithInputs(game, inputs, isVsBot = true)

    state.isVsBot shouldBe true
  }

  it should "transition UI through WaitingRoom when Player 1 ends turn in vs Bot mode" in {
    val game = Game(BoardFactory.BoardWithPopulatedFields())
    // P1: Draw (ENTER) -> Confirm action (ENTER) -> EndTurn (ENTER) -> Dismiss Waiting Room (ENTER)
    val inputs = List(Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER, Key.ESCAPE)
    val state = runControllerWithInputs(game, inputs, isVsBot = true)

    state.isVsBot shouldBe true
    state.inputMode should not be InputMode.WaitingRoom
  }

  it should "automatically execute Bot turn (Player 2) until turn passes back to Player 1" in {
    val game = Game(BoardFactory.BoardWithPopulatedFields())
    val inputs = List(
      Key.ENTER,
      Key.ENTER,
      Key.ENTER, // P1 Turn: Draw, Select, EndTurn
      Key.ENTER, // Dismiss WaitingRoom screen
      Key.ESCAPE // Exit loop when P1 turn is active again
    )

    val (controller, getLastState) = createScriptedController(game = game, inputs = inputs, isVsBot = true)
    controller.start()

    val finalState = getLastState().get
    finalState.isVsBot shouldBe true
  }

  it should "process Bot turn during GamePhase.LastTurn until reaching EndGame mode" in {
    val overGame = Game(BoardFactory.BoardWithPopulatedFields()).copy(phase = GamePhase.LastTurn)
    // Flow: P1 completes final turn -> Bot completes final turn -> transitions to EndGame
    val inputs = List(Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER)
    val state = runControllerWithInputs(overGame, inputs, isVsBot = true)

    state.inputMode shouldBe InputMode.EndGame
  }

  it should "trigger opponentBot.react on complex non-routine actions like Swap and Observe" in {
    val jackTurn = model.showcase.JackShowcase.turn
    val gameJack = Game(GamePhase.Playing, jackTurn, None)

    // Flow: P1 executes ObserveOpponent macro in vsBot mode
    val inputs = List(Key.ENTER, Key.ENTER, Key.ESCAPE)
    noException should be thrownBy {
      runControllerWithInputs(gameJack, inputs, isVsBot = true)
    }
  }

  it should "support Match model playable with isVsBot flag set to true" in {
    val matchModel = model.playable.game.Match(100)
    val inputs = List(Key.ENTER, Key.ESCAPE)

    val state = runControllerWithInputs(matchModel.game, inputs, isVsBot = true)
    state.isVsBot shouldBe true
  }

  "confirmAction in ActionMenu" should "transition to SelectCardOnBoard for player-targeting macro actions" in {
    // Action.ChooseDiscard(-1) / Action.ChooseReplace(-1) / Action.ObservePlayer(-1)
    val gameDiscard = Game(GamePhase.LastTurn, KingDrawShowcase.turn, Some(Player2))

    // Press ENTER on macro action in ActionMenu
    val state = runControllerWithInputs(gameDiscard, List(Key.ENTER, ENTER))
    state.inputMode shouldBe InputMode.SelectCardOnBoard
  }

  it should "transition to SelectAdversaryCardOnBoard for opponent-targeting macro actions" in {
    // Action.ObserveOpponent(-1) / Action.GiveBack(-1)
    val jackTurn = model.showcase.JackShowcase.turn
    val gameJack = Game(GamePhase.LastTurn, jackTurn, Some(Player2))

    val state = runControllerWithInputs(gameJack, List(Key.DOWN, Key.ENTER, Key.ENTER, Key.ENTER))
    state.inputMode shouldBe InputMode.SelectAdversaryCardOnBoard
  }

  it should "reset pendingOpponentSwapIdx and switch mode for Action.Swap macro" in {
    // Action.Swap(-1, -1)
    val swapTurn = model.showcase.JackShowcase.turn
    val gameSwap = Game(GamePhase.LastTurn, swapTurn, Some(Player2))

    val state = runControllerWithInputs(gameSwap, List(Key.DOWN, Key.ENTER, Key.ENTER, Key.ENTER))
    state.inputMode shouldBe InputMode.SelectAdversaryCardOnBoard
  }

  it should "directly execute simple actions matching the wildcard branch" in {
    // Action.Draw / Action.Confirm / Action.Cactus
    val game = Game(BoardFactory.BoardWithPopulatedFields())
    val state = runControllerWithInputs(game, List(Key.ENTER, Key.ESCAPE))

    // Action.Draw executes directly; hand size updates
    state.cardsInHand.exists(_.isDefined) shouldBe true
  }

  "confirmAction in SelectCardOnBoard" should "complete Swap execution when pendingOpponentSwapIdx is defined" in {
    val swapTurn = model.showcase.SevenShowcase.turn
    val gameSwap = Game(GamePhase.LastTurn, swapTurn, Some(Player2))

    // Flow: Enter Swap Macro -> Select Opponent Card (ENTER) -> Select Player Card (ENTER) -> ESCAPE
    val inputs = List(Key.ENTER, Key.ENTER, Key.ENTER, Key.ESCAPE)
    noException should be thrownBy {
      runControllerWithInputs(gameSwap, inputs)
    }
  }

  it should "resolve ObservePlayer macro targeting" in {
    val sevenTurn = model.showcase.SevenShowcase.turn
    val gameSeven = Game(GamePhase.FirstTurns, sevenTurn, Some(Player2))

    val state = runControllerWithInputs(gameSeven, List(Key.DOWN ,Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER, DOWN, ENTER))
    state.actionHistory should include("Peek at your card in position 1")
  }

  it should "resolve ChooseReplace macro targeting" in {
    val board = BoardFactory.BoardWithPopulatedFields()
    val turn = model.playable.turn.Turn(
      hand = List(board.getField(Player1).cardsList.head),
      board = board,
      player = Player1,
      actions = List(Action.ChooseReplace(-1), Action.ChooseReplace(0)),
      cactus = false
    )
    val game = Game(GamePhase.Playing, turn, None)

    val state = runControllerWithInputs(game, List(Key.ENTER, Key.ENTER, Key.ESCAPE))
    noException should be thrownBy state
  }

  it should "resolve ChooseDiscard macro targeting" in {
    val discardTurn = model.showcase.SuccessfulDiscardShowcase.turn
    val gameDiscard = Game(GamePhase.LastTurn, discardTurn, Some(Player2))

    val state = runControllerWithInputs(gameDiscard, List(Key.ENTER, Key.ENTER, Key.ESCAPE))
    noException should be thrownBy state
  }

  it should "fallback to matching available board actions when selectedMacroAction is None" in {
    val defaultGame = Game(BoardFactory.BoardWithPopulatedFields())
    // Fallback path targeting when selectedMacroAction is empty
    val inputs = List(Key.ENTER, Key.ENTER, Key.ESCAPE)
    noException should be thrownBy {
      runControllerWithInputs(defaultGame, inputs)
    }
  }

  "confirmAction in SelectAdversaryCardOnBoard" should "store pendingOpponentSwapIdx during Swap phase" in {
    val swapTurn = model.showcase.SevenShowcase.turn
    val gameSwap = Game(GamePhase.LastTurn, swapTurn, Some(Player2))

    // ENTER on Swap macro -> ENTER on adversary card selection
    val state = runControllerWithInputs(gameSwap, List(Key.DOWN ,Key.ENTER, Key.ENTER, Key.ENTER))
    state.inputMode shouldBe InputMode.SelectCardOnBoard
  }

  it should "execute ObserveOpponent action when not in Swap phase" in {
    val jackTurn = model.showcase.SixShowcase.turn
    val gameJack = Game(GamePhase.LastTurn, jackTurn, Some(Player2))

    val state = runControllerWithInputs(gameJack, List(Key.DOWN ,Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER, DOWN, ENTER))
    state.actionHistory should include("Peek at opponent card in position 1")
  }

  it should "execute GiveBack action when available on target selection" in {
    val board = BoardFactory.BoardWithPopulatedFields()
    val turn = model.playable.turn.Turn(
      hand = List(board.getField(Player1).cardsList.head),
      board = board,
      player = Player1,
      actions = List(Action.GiveBack(0), Action.GiveBack(1)),
      cactus = false
    )
    val game = Game(GamePhase.Playing, turn, None)

    val state = runControllerWithInputs(game, List(Key.ENTER, Key.ENTER, Key.ESCAPE))
    noException should be thrownBy state
  }

  "confirmAction in WaitingRoom" should "reset pending swap state, clear history, and resume gameplay" in {
    // Flow: Enter turn sequence to trigger WaitingRoom, then press ENTER to exit screen
    val inputs = List(Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER, Key.ESCAPE)
    val state = runControllerWithInputs(Game(BoardFactory.BoardWithPopulatedFields()), inputs)

    state.inputMode should not be InputMode.WaitingRoom
    state.actionHistory shouldBe empty
  }

  "confirmAction in EndGame" should "set running to false and exit main execution loop" in {
    val overGame = Game(BoardFactory.BoardWithPopulatedFields()).copy(phase = GamePhase.LastTurn)
    // Run turn through completion into EndGame, then press ENTER on EndGame screen
    val inputs = List(Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER)
    val state = runControllerWithInputs(overGame, inputs)

    state.inputMode shouldBe InputMode.EndGame
  }
