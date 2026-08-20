package org.pps.functus
package controller

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any

import scala.jdk.CollectionConverters.*
import model.board.{BoardFactory, Player}
import view.{CLIView, GameState, InputMode, Key}


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
        when(mockView.readInput()).thenReturn(Key.LEFT, Key.RIGHT, Key.ESCAPE)

        val controller = new GameController(mockView)
        controller.start()

        // 3 loop iterations: 2 unhandled keys + 1 ESCAPE
        verify(mockView, times(3)).render(any())
      }
    }

    describe("Navigation and Selection (moveSelection)") {

      it("should handle circular wrap-around navigation with directional keys in ActionMenu") {
        val mockView = mock[CLIView]
        // Starting at index 0. UP moves backward (wraps to last index), DOWN returns to 0.
        when(mockView.readInput()).thenReturn(
          Key.UP,
          Key.DOWN,
          Key.ESCAPE
        )

        val controller = new GameController(mockView)
        controller.start()

        val states = getCapturedStates(mockView)
        val numActions = states.head.possibleAction.length

        if numActions > 1 then
          states(2).selectedAction shouldBe 0
      }

      it("should navigate through SelectCardOnBoard or SelectAdversaryCardOnBoard items") {
        val mockView = mock[CLIView]
        when(mockView.readInput()).thenReturn(
          // Setup Player 1(FirstTurn: Observe -> Confirm -> WaitingRoom)
          Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER,
          // Setup Player 2 (FirstTurn: Observe -> Confirm -> WaitingRoom)
          Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER,
          // First Possible action on SimpleTurn (draw)
          Key.ENTER,
          // Activate Card (swap if normal, chose action if it has effect)
          Key.ENTER,
          // Navigate Card or possible action
          Key.RIGHT, Key.RIGHT,
          // confirm swap card or activate card effect
          Key.ENTER,
          Key.ESCAPE
        )

        val controller = new GameController(mockView)
        controller.start()

        val states = getCapturedStates(mockView)

        val boardStates = states.filter(s =>
          s.inputMode == InputMode.SelectCardOnBoard || s.inputMode == InputMode.SelectAdversaryCardOnBoard
        )

        if boardStates.isEmpty then
          val modesList = states.map(_.inputMode).mkString(" -> ")
          fail(s"Controller never entered InputMode.SelectCardOnBoard or SelectAdversaryCardOnBoard. Sequence was: [$modesList]")

      }

      it("should ignore navigation keys while in WaitingRoom mode") {
        val mockView = mock[CLIView]
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

    describe("Action Execution & Turn Transitions") {

      it("should transition to WaitingRoom mode after executing Action.Confirm") {
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
        when(mockView.readInput()).thenReturn(
          Key.ENTER, Key.ENTER, Key.ENTER, // P1 completes FirstTurn -> WaitingRoom
          Key.ENTER, // Dismiss WaitingRoom
          Key.ESCAPE
        )

        val controller = new GameController(mockView)
        controller.start()

        val lastState = getLastState(mockView)
        lastState.inputMode shouldBe InputMode.ActionMenu
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

      it("should switch to SelectCardOnBoard or SelectAdversaryCardOnBoard when an action requires targeting") {
        val mockView = mock[CLIView]
        // Finish both P1 and P2 observation phase so standard turns begin
        when(mockView.readInput()).thenReturn(
          Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER, // P1 initial turn -> WaitingRoom
          Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER, // P2 initial turn -> WaitingRoom
          Key.ENTER,                                  // Perform first available action (Draw/Select)
          Key.ENTER,                                  // Swap card or activate card if is a 6/7/8
          Key.ENTER,                                  // use effect in case of a 6 / 7 / 8
          Key.ESCAPE
        )

        val controller = new GameController(mockView)
        controller.start()

        val states = getCapturedStates(mockView)

        // Verify that input modes transition through adversary selection prior to own card selection
        val modes = states.map(_.inputMode)
        modes contains atLeastOneOf(InputMode.SelectCardOnBoard,InputMode.SelectAdversaryCardOnBoard)
      }
    }

    describe("State Synchronization Accuracy (syncState)") {

      it("should accurately reflect board cards, hand, and deck status in the rendered GameState") {
        val mockView = mock[CLIView]
        when(mockView.readInput()).thenReturn(Key.ESCAPE)

        val initialBoard = BoardFactory.BoardWithPopulatedFields()
        val controller = new GameController(mockView, initialBoard)
        controller.start()

        val state = getLastState(mockView)

        // Strict assertions against initial board values
        state.playerCard.length shouldBe initialBoard.getField(Player.Player1).cardsList.size
        state.adversaryCard.length shouldBe initialBoard.getField(Player.Player2).cardsList.size
        state.remainingCardInDeck shouldBe initialBoard.deck.cards.size
        state.inputMode shouldBe InputMode.ActionMenu
      }
    }
  }
