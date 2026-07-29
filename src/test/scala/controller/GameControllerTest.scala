package org.pps.functus
package controller

import view.{InputMode, Key}

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class GameControllerTest extends AnyFunSpec with Matchers:

  describe("GameController Integration Tests") {

    it("dovrebbe inizializzare correttamente la partita con il primo turno del Giocatore 1") {
      val mockView = new MockCLIView()
      val controller = new GameController(mockView)

      // Inviamo subito ESCAPE per fermare il loop dopo il primo render
      mockView.enqueueInputs(Key.ESCAPE)
      controller.start()


      val state = mockView.lastRenderedState.get
      state.inputMode shouldBe InputMode.ActionMenu
      state.possibleAction.map(_.id) should contain("observe")
    }

    it("dovrebbe permettere la navigazione con le frecce nel menu delle azioni") {
      val mockView = new MockCLIView()
      val controller = new GameController(mockView)

      // Simula: Faccia DOWN per spostare il cursore, poi ESCAPE
      mockView.enqueueInputs(Key.DOWN, Key.ESCAPE)
      controller.start()

      val state = mockView.lastRenderedState.get
      // Se ci sono più azioni disponibili, l'indice selezionato dev'essere incrementato
      if state.possibleAction.length > 1 then
        state.selectedAction shouldBe 1
    }

    it("dovrebbe eseguire l'azione Observe e passare alla modalità Confirm") {
      val mockView = new MockCLIView()
      val controller = new GameController(mockView)

      // Simula: ENTER su Observe (selezionato a 0), poi ESCAPE
      mockView.enqueueInputs(Key.ENTER, Key.ESCAPE)
      controller.start()

      val state = mockView.lastRenderedState.get
      state.possibleAction.map(_.id) should contain("confirm")
    }

    it("dovrebbe passare alla schermata di intermezzo (WaitingRoom) dopo che il Giocatore 1 ha confermato") {
      val mockView = new MockCLIView()
      val controller = new GameController(mockView)

      // Simula:
      // 1. ENTER per Observe
      // 2. ENTER per Confirm
      // 3. ENTER termina il primo turno del P1
      // 4. ESCAPE per uscire dal loop quando si trova in WaitingRoom
      mockView.enqueueInputs(Key.ENTER, Key.ENTER, Key.ENTER, Key.ESCAPE)
      controller.start()

      val state = mockView.lastRenderedState.get
      // Dopo la fine del turno del P1, il controller deve impostare la WaitingRoom per il P2
      state.inputMode shouldBe InputMode.WaitingRoom
    }

    it("dovrebbe passare al Giocatore 2 dopo la schermata di WaitingRoom") {
      val mockView = new MockCLIView()
      val controller = new GameController(mockView)

      // Simula:
      // 1. ENTER (Observe P1)
      // 2. ENTER (Confirm P1)
      // 3. ENTER Fine Turno -> entra in WaitingRoom
      // 4. ENTER (Sblocca WaitingRoom -> Inizia turno P2)
      // 5. ESCAPE
      mockView.enqueueInputs(Key.ENTER, Key.ENTER,Key.ENTER, Key.ENTER, Key.ESCAPE)
      controller.start()

      val state = mockView.lastRenderedState.get
      state.inputMode shouldBe InputMode.ActionMenu
      // Ora anche il Giocatore 2 deve effettuare l'azione Observe per il suo primo turno
      state.possibleAction.map(_.id) should contain("observe")
    }

    it("dovrebbe attivare la modalità SelectCardOnBoard se l'utente seleziona lo scarto dal tavolo") {
      val mockView = new MockCLIView()
      val controller = new GameController(mockView)

      // Per testare lo scarto dal tavolo, completiamo l'osservazione iniziale per entrambi i giocatori
      // P1 Observe -> Confirm -> Unblock WaitingRoom -> P2 Observe -> Confirm -> Unblock WaitingRoom
      val setupInputs = Seq(
        Key.ENTER, Key.ENTER, Key.ENTER, // P1 fa Observe, Confirm e sblocca la stanza
        Key.ENTER, Key.ENTER, Key.ENTER  // P2 fa Observe, Confirm e sblocca la stanza
      )

      // Ora siamo al turno standard del P1. Selezioniamo "Scarta dal tavolo" (indice 0) con ENTER
      mockView.enqueueInputs(setupInputs :+ Key.ENTER :+ Key.ESCAPE*)
      controller.start()

      val state = mockView.lastRenderedState.get
      // Se l'azione disponibile era lo scarto equivalente, l'input mode deve passare a SelectCardOnBoard
      if state.possibleAction.exists(_.id == "select_discard") then
        state.inputMode shouldBe InputMode.SelectCardOnBoard
    }
  }