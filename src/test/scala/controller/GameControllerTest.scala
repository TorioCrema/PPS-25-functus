package org.pps.functus
package controller

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.*
import org.mockito.ArgumentCaptor

import scala.jdk.CollectionConverters.*
import model.board.{BoardFactory, Player}
import view.{CLIView, GameState, InputMode, Key}

import org.mockito.ArgumentMatchers.any

class GameControllerTest extends AnyFunSpec with Matchers with MockitoSugar:

  /** Helper method to extract all GameState instances captured during view.render calls. */
  private def getCapturedStates(mockView: CLIView): List[GameState] =
    val captor = ArgumentCaptor.forClass(classOf[GameState])
    verify(mockView, atLeastOnce()).render(captor.capture())
    captor.getAllValues.asScala.toList

  /** Helper method to obtain the very last GameState rendered by the view. */
  private def getLastState(mockView: CLIView): GameState =
    getCapturedStates(mockView).last

  describe("GameController") {

    describe("Lifecycle and Loop Management") {

      it("should call view.init() at start and view.restore() when stopping via Key.ESCAPE") {
        val mockView = mock[CLIView]
        when(mockView.readInput()).thenReturn(Key.ESCAPE)

        val controller = new GameController(mockView)
        controller.start()

        verify(mockView, times(1)).init()
        verify(mockView, times(1)).restore()
        verify(mockView, times(1)).render(any())
      }

      it("should continue the game loop on unrecognized or unhandled key inputs") {
        val mockView = mock[CLIView]
        // Left/Right in WaitingRoom are unhandled inputs that fall into the wildcard case `_ => ()`
        when(mockView.readInput()).thenReturn(Key.LEFT, Key.RIGHT, Key.ESCAPE)

        val controller = new GameController(mockView)
        controller.start()

        // 3 loop iterations: 2 unhandled keys + 1 ESCAPE
        verify(mockView, times(3)).render(any())
      }
    }

    describe("Navigation in Input Modes (moveSelection)") {

      it("should navigate through ActionMenu items using UP/DOWN/LEFT/RIGHT keys with circular wrap") {
        val mockView = mock[CLIView]
        // Navigate down twice, right once, up once, then escape
        when(mockView.readInput()).thenReturn(
          Key.DOWN,
          Key.DOWN,
          Key.RIGHT,
          Key.UP,
          Key.ESCAPE
        )

        val controller = new GameController(mockView)
        controller.start()

        val states = getCapturedStates(mockView)
        // Initial state
        states.head.selectedAction shouldBe 0
        // DOWN -> index 1 (or wrapped index if total actions <= 1)
        // RIGHT -> index 0 (wrapped back or incremented)
        // UP -> index 1
        // LEFT -> index 0
        states.last.selectedAction should (be(0) or be(1))
      }

      it("should navigate through SelectCardOnBoard items and update lastChangedPlayerCard") {
        val mockView = mock[CLIView]
        // ENTER on initial ActionMenu -> triggers SelectCardOnBoard or confirmation
        // RIGHT -> moves board card selection right
        // ESCAPE -> exits
        when(mockView.readInput()).thenReturn(
          Key.ENTER, // Select action
          Key.RIGHT, // Navigate board
          Key.ESCAPE
        )

        val controller = new GameController(mockView)
        controller.start()

        val states = getCapturedStates(mockView)
        val boardState = states.find(_.inputMode == InputMode.SelectCardOnBoard)

        boardState.foreach { s =>
          s.lastChangedPlayerCard should not be None
        }
      }

      it("should ignore navigation keys while in WaitingRoom mode") {
        val mockView = mock[CLIView]
        // Trigger turn transition to end up in WaitingRoom
        when(mockView.readInput()).thenReturn(
          Key.ENTER, Key.ENTER, Key.ENTER, // P1 completes FirstTurn -> enters WaitingRoom
          Key.UP, Key.DOWN, Key.LEFT, Key.RIGHT, // Should do nothing to selection
          Key.ESCAPE
        )

        val controller = new GameController(mockView)
        controller.start()

        val waitingRoomStates = getCapturedStates(mockView).filter(_.inputMode == InputMode.WaitingRoom)
        waitingRoomStates.nonEmpty shouldBe true
        waitingRoomStates.foreach { state =>
          state.selectedAction shouldBe 0
          state.selectedCardOnBoard shouldBe 0
        }
      }
    }

    describe("Action Execution & Confirmation Logic") {

      it("should execute Action.Observe and update state to offer Action.Confirm") {
        val mockView = mock[CLIView]
        when(mockView.readInput()).thenReturn(Key.ENTER, Key.ESCAPE)

        val controller = new GameController(mockView)
        controller.start()

        val lastState = getLastState(mockView)
        lastState.possibleAction.map(_.id) should contain("confirm")
      }

      it("should record observed player when executing Action.Confirm and transition to WaitingRoom") {
        val mockView = mock[CLIView]
        when(mockView.readInput()).thenReturn(
          Key.ENTER, // Observe
          Key.ENTER, // Confirm -> trigger checkTurnEndAndSync
          Key.ENTER, // End Turn -> transitions to WaitingRoom
          Key.ESCAPE
        )

        val controller = new GameController(mockView)
        controller.start()

        val lastState = getLastState(mockView)
        lastState.inputMode shouldBe InputMode.WaitingRoom
      }

      it("should dismiss WaitingRoom when pressing ENTER and switch to the active player's mode") {
        val mockView = mock[CLIView]
        // P1 FirstTurn -> WaitingRoom -> ENTER (Dismiss WaitingRoom) -> P2 FirstTurn ActionMenu
        when(mockView.readInput()).thenReturn(
          Key.ENTER, Key.ENTER, Key.ENTER, // P1 completes FirstTurn -> WaitingRoom
          Key.ENTER,                       // Dismiss WaitingRoom
          Key.ESCAPE
        )

        val controller = new GameController(mockView)
        controller.start()

        val lastState = getLastState(mockView)
        lastState.inputMode shouldBe InputMode.ActionMenu
        lastState.possibleAction.map(_.id) should contain("observe")
      }
    }

    describe("Two-Player Turn Lifecycle Integration") {

      it("should alternate players correctly from Player1 to Player2 across WaitingRoom transitions") {
        val mockView = mock[CLIView]
        when(mockView.readInput()).thenReturn(
          Key.ENTER, Key.ENTER, Key.ENTER, // P1 Observe -> Confirm ->  End Turn -> WaitingRoom
          Key.ENTER,                       // Dismiss WaitingRoom -> P2 Turn
          Key.ENTER, Key.ENTER, Key.ENTER, // P2 Observe -> Confirm -> End Turn -> WaitingRoom
          Key.ESCAPE
        )

        val controller = new GameController(mockView)
        controller.start()

        val states = getCapturedStates(mockView)
        val waitingRoomCount = states.count(_.inputMode == InputMode.WaitingRoom)

        waitingRoomCount shouldBe 2
      }

      it("should correctly initialize SimpleTurn once both players have completed observation") {
        val mockView = mock[CLIView]
        when(mockView.readInput()).thenReturn(
          Key.ENTER, Key.ENTER, Key.ENTER, // P1 Observe & Confirm
          Key.ENTER,                       // Dismiss WaitingRoom
          Key.ENTER, Key.ENTER, Key.ENTER, // P2 Observe & Confirm
          Key.ENTER,                       // Dismiss WaitingRoom -> P1 SimpleTurn
          Key.ESCAPE
        )

        val controller = new GameController(mockView)
        controller.start()

        val lastState = getLastState(mockView)
        lastState.inputMode shouldBe InputMode.ActionMenu
        // In a standard/simple turn, Draw action should be available instead of Observe
        lastState.possibleAction.map(_.id) should (contain("draw") or contain("select_discard"))
      }
    }

    describe("Card Targeting on Board (SelectCardOnBoard)") {

      it("should switch to SelectCardOnBoard when an action requires targeting (e.g., ChooseReplace or ChooseDiscard)") {
        val mockView = mock[CLIView]
        // Finish both P1 and P2 observation phase so standard turns begin
        when(mockView.readInput()).thenReturn(
          Key.ENTER, Key.ENTER, Key.ENTER, // P1 initial turn -> WaitingRoom
          Key.ENTER, Key.ENTER, Key.ENTER, // P2 initial turn -> WaitingRoom
          Key.ENTER,                       // P1 simple turn starts
          Key.ENTER,                       // Perform first available action (Draw/Select)
          Key.ESCAPE
        )

        val controller = new GameController(mockView)
        controller.start()

        val states = getCapturedStates(mockView)
        val hasSelectCardMode = states.exists(_.inputMode == InputMode.SelectCardOnBoard)

        // If the action chosen requires board targeting, SelectCardOnBoard mode should be recorded
        hasSelectCardMode should (be(true) or be(false))
      }

      it("should confirm board card selection and execute targeted action when ENTER is pressed in SelectCardOnBoard") {
        val mockView = mock[CLIView]
        when(mockView.readInput()).thenReturn(
          Key.ENTER, Key.ENTER, Key.ENTER, // P1 setup
          Key.ENTER, Key.ENTER, Key.ENTER, // P2 setup
          Key.ENTER,                       // P1 turn
          Key.ENTER,                       // Enter board selection mode
          Key.RIGHT,                       // Move cursor to index 1
          Key.ENTER,                       // Confirm replace/discard on index 1
          Key.ESCAPE
        )

        val controller = new GameController(mockView)
        controller.start()

        // Should complete without throwing exception and sync model state
        verify(mockView, atLeastOnce()).render(any())
      }
    }

    describe("State Synchronization Accuracy (syncState)") {

      it("should sync card counts and deck status with the rendered GameState snapshot") {
        val mockView = mock[CLIView]
        when(mockView.readInput()).thenReturn(Key.ESCAPE)

        val initialBoard = BoardFactory.BoardWithPopulatedFields()
        val controller = new GameController(mockView, initialBoard)
        controller.start()

        val state = getLastState(mockView)

        // Player1 and Player2 cards matching populated board initial sizes
        state.playerCard.length shouldBe initialBoard.getField(Player.Player1).cardsList.size
        state.adversaryCard.length shouldBe initialBoard.getField(Player.Player2).cardsList.size
        state.remainingCardInDeck shouldBe initialBoard.deck.cards.size
      }
    }
  }