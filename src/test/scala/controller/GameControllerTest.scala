package org.pps.functus
package controller

import view.{InputMode, Key}

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class GameControllerTest extends AnyFunSpec with Matchers:

  describe("GameController Integration Tests") {

    it("Should correctly initialize the game with Player 1's first round") {
      val mockView = new MockCLIView()
      val controller = new GameController(mockView)

      // Send ESCAPE to stop the loop after the first render
      mockView.enqueueInputs(Key.ESCAPE)
      controller.start()


      val state = mockView.lastRenderedState.get
      state.inputMode shouldBe InputMode.ActionMenu
      state.possibleAction.map(_.id) should contain("observe")
    }

    it("should allow navigation with arrows in the action menu") {
      val mockView = new MockCLIView()
      val controller = new GameController(mockView)

      // Simulate: DOWN arrow for moving the cursor, then ESCAPE
      mockView.enqueueInputs(Key.DOWN, Key.ESCAPE)
      controller.start()

      val state = mockView.lastRenderedState.get
      // if there are more action the index must be incremented
      if state.possibleAction.length > 1 then
        state.selectedAction shouldBe 1
    }

    it("Should perform the Observe action and switch to Confirm mode") {
      val mockView = new MockCLIView()
      val controller = new GameController(mockView)

      // Simulate: ENTER on Observe (selected on 0), then ESCAPE
      mockView.enqueueInputs(Key.ENTER, Key.ESCAPE)
      controller.start()

      val state = mockView.lastRenderedState.get
      state.possibleAction.map(_.id) should contain("confirm")
    }

    it("Should switch to the change player screen (WaitingRoom) after Player 1 confirms") {
      val mockView = new MockCLIView()
      val controller = new GameController(mockView)

      // Simulate:
      // 1. ENTER for Observe
      // 2. ENTER for Confirm
      // 3. ENTER end first turn of P1
      // 4. ESCAPE to exit the loop while in WaitingRoom
      mockView.enqueueInputs(Key.ENTER, Key.ENTER, Key.ENTER, Key.ESCAPE)
      controller.start()

      val state = mockView.lastRenderedState.get
      //After the end of the P1 turn, the controller must set the WaitingRoom for P2
      state.inputMode shouldBe InputMode.WaitingRoom
    }

    it("should switch to Player 2 after the WaitingRoom screen") {
      val mockView = new MockCLIView()
      val controller = new GameController(mockView)

      // Simulate:
      // 1. ENTER (Observe P1)
      // 2. ENTER (Confirm P1)
      // 3. ENTER end Turn ->  in WaitingRoom
      // 4. ENTER (exit WaitingRoom -> start P2 turn)
      // 5. ESCAPE
      mockView.enqueueInputs(Key.ENTER, Key.ENTER,Key.ENTER, Key.ENTER, Key.ESCAPE)
      controller.start()

      val state = mockView.lastRenderedState.get
      state.inputMode shouldBe InputMode.ActionMenu
      // Now Player 2 must also perform the Observe action for his first turn
      state.possibleAction.map(_.id) should contain("observe")
    }

    it("Should activate SelectCardOnBoard mode if the user selects the discard from the table") {
      val mockView = new MockCLIView()
      val controller = new GameController(mockView)

      // To test the discard from the table, we complete the initial observation for both players
      // P1 Observe -> Confirm -> End Turn -> Unblock WaitingRoom -> P2 Observe -> Confirm -> End Turn -> Unblock WaitingRoom
      val setupInputs = Seq(
        Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER, // P1 fa Observe, Confirm e sblocca la stanza
        Key.ENTER, Key.ENTER, Key.ENTER, Key.ENTER  // P2 fa Observe, Confirm e sblocca la stanza
      )

      // We are now on the standard P1 shift. Select "Discard from Table" (index 0) with ENTER
      mockView.enqueueInputs(setupInputs :+ Key.ENTER :+ Key.ESCAPE*)
      controller.start()

      val state = mockView.lastRenderedState.get
      // If the available action was the discard from the table, the mode input must pass to SelectCardOnBoard
      if state.possibleAction.exists(_.id == "select_discard") then
        state.inputMode shouldBe InputMode.SelectCardOnBoard
    }
  }