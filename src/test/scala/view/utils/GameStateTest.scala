package org.pps.functus
package view.utils

import model.deck.card.CardImpl
import model.deck.card.Suit.{Cups, Swords}
import view.utils.{GameState, InputMode, ViewAction}

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import view.utils.{GameState, InputMode, ViewAction}

class GameStateTest extends AnyFunSpec with Matchers:

  // Mock / Sample Data for testing
  private val sampleCard1 = CardImpl(1, Swords)
  private val sampleCard2 = CardImpl(10, Cups)
  private val sampleAction = ViewAction("pesca_mazzo", "Pesca dal mazzo")

  describe("A GameState") {

    it("should instantiate correctly with default values") {
      val state = GameState(
        adversaryCard = List(None, None),
        playerCard = List(Some(sampleCard1), None),
        remainingCardInDeck = 38,
        lastDiscardedCard = Some(sampleCard2),
        cardsInHand = List(None),
        possibleAction = List(sampleAction)
      )

      state.inputMode shouldBe InputMode.ActionMenu
      state.remainingCardInDeck shouldBe 38
    }

    it("should allow overriding default arguments on instantiation") {
      val state = GameState(
        adversaryCard = Nil,
        playerCard = Nil,
        remainingCardInDeck = 20,
        lastDiscardedCard = None,
        cardsInHand = Nil,
        possibleAction = Nil,
        inputMode = InputMode.SelectCardOnBoard,
        selectedAction = 2,
        selectedCardOnBoard = 1
      )

      state.inputMode shouldBe InputMode.SelectCardOnBoard
      state.selectedCardOnBoard shouldBe 1
    }

    it("should immutably update state using copy") {
      val initialState = GameState(
        adversaryCard = List(None, None, None, None),
        playerCard = List(None, None, None, None),
        remainingCardInDeck = 40,
        lastDiscardedCard = None,
        cardsInHand = Nil,
        possibleAction = List(sampleAction)
      )

      val updatedState = initialState.copy(
        cardsInHand = List(Some(sampleCard1)),
        remainingCardInDeck = initialState.remainingCardInDeck - 1,
        selectedAction = 1
      )

      initialState.cardsInHand shouldBe empty

      updatedState.selectedAction shouldBe 1
    }

    it("should properly support structural equality") {
      val state1 = GameState(
        adversaryCard = List(None),
        playerCard = List(Some(sampleCard1)),
        remainingCardInDeck = 30,
        lastDiscardedCard = None,
        cardsInHand = Nil,
        possibleAction = List(sampleAction)
      )

      val state2 = GameState(
        adversaryCard = List(None),
        playerCard = List(Some(sampleCard1)),
        remainingCardInDeck = 30,
        lastDiscardedCard = None,
        cardsInHand = Nil,
        possibleAction = List(sampleAction)
      )

      state1 shouldBe state2
      state1.hashCode() shouldBe state2.hashCode()
    }
  }
