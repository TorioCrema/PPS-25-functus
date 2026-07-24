package org.pps.functus
package controller

import model.deck.card.CardImpl
import view.{Action, CLIView, GameState, InputMode, Key}
import model.deck.card.Suit.*

class GameController(private val view: CLIView):

  private val INITIAL_ACTION = List(
    Action("pesca_mazzo", "Pesca dal mazzo"),
    Action("pesca_scarto", "Pesca dallo scarto")
  )

  private var state: GameState = GameState(
    adversaryCard = List(None, None, None, None),
    playerCard = List(None, None, None, None),
    remainingCardInDeck = 38,
    lastDiscardedCard = Some(CardImpl(1, Swords)),
    cardsInHand = List(None),
    possibleAction = INITIAL_ACTION
  )

  def start(): Unit =
    view.init()
    var loop = true

    try
      while loop do
        view.render(state)
        val key = view.readInput()

        key match
          case Key.UP     => handleArrow("UP")
          case Key.DOWN   => handleArrow("DOWN")
          case Key.LEFT   => handleArrow("LEFT")
          case Key.RIGHT  => handleArrow("RIGHT")
          case Key.ENTER  => confirmAction()
          case Key.ESCAPE => loop = false
          case _          => ()
    finally
      view.restore()

  private def handleArrow(direction: String): Unit =
    state.inputMode match
      case InputMode.ActionMenu =>
        val numAction = state.possibleAction.length
        if numAction > 0 then
          val delta = if direction == "UP" then -1 else if direction == "DOWN" then 1 else 0
          if delta != 0 then
            val newIndex = (state.selectedAction + delta + numAction) % numAction
            state = state.copy(selectedAction = newIndex)

      case InputMode.SelectCardOnBoard =>
        val numCard = state.playerCard.length
        val delta = if direction == "LEFT" then -1 else if direction == "RIGHT" then 1 else 0
        if delta != 0 then
          val newIndex = (state.selectedCardOnBoard + delta + numCard) % numCard
          state = state.copy(selectedCardOnBoard = newIndex)

  private def confirmAction(): Unit =
    state.inputMode match
      case InputMode.ActionMenu =>
        if state.possibleAction.nonEmpty then
          val selectedAction = state.possibleAction(state.selectedAction)

          selectedAction.id match
            case "pesca_mazzo" =>
              val newAction = List(
                Action("scarta_mano", "Scarta carta pescata"),
                Action("scambia_tavolo", "Scambia con carta del tavolo")
              )
              state = state.copy(
                cardsInHand = List(Some(CardImpl(10, Cups))),
                remainingCardInDeck = state.remainingCardInDeck - 1,
                possibleAction = newAction,
                selectedAction = 0
              )

            case "pesca_scarto" =>
              val newAction = List(
                Action("scambia_tavolo", "Scambia con carta del tavolo")
              )
              state = state.copy(
                cardsInHand = List(state.lastDiscardedCard),
                lastDiscardedCard = None,
                possibleAction = newAction,
                selectedAction = 0
              )

            case "scarta_mano" =>
              state = state.copy(
                lastDiscardedCard = state.cardsInHand.head,
                cardsInHand = Nil,
                possibleAction = INITIAL_ACTION,
                selectedAction = 0
              )

            case "scambia_tavolo" =>
              state = state.copy(inputMode = InputMode.SelectCardOnBoard)

            case _ => ()

      case InputMode.SelectCardOnBoard =>
        val cardToBeReplaced = state.playerCard(state.selectedCardOnBoard)
        val newCard = state.playerCard.updated(state.selectedCardOnBoard, state.cardsInHand.head)

        state = state.copy(
          playerCard = newCard,
          lastDiscardedCard = cardToBeReplaced.orElse(state.cardsInHand.head),
          cardsInHand = Nil,
          possibleAction = INITIAL_ACTION,
          inputMode = InputMode.ActionMenu,
          selectedAction = 0
        )
