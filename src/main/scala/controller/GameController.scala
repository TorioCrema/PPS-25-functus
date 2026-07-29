package org.pps.functus
package controller

import model.board.{Board, BoardFactory, Player}
import model.turn.{Action, Turn, Turns}
import view.{ViewAction, CLIView, GameState, InputMode, Key}

class GameController(
    private val view: CLIView,
    initialBoard: Board = BoardFactory.BoardWithPopulatedFields()
):

  private var currentPlayer: Player = Player.Player1
  private var turn: Turn = Turns.FirstTurn(initialBoard, currentPlayer)
  private var observedPlayers: Set[Player] = Set.empty

  private var currentModelActions: List[Action] = Nil
  private var state: GameState = syncState(InputMode.ActionMenu, 0, 0)

  /** Starts the main game loop, initializing the view and processing user input.
   *
   */
  def start(): Unit =
    view.init()
    var running = true

    try
      while running do
        view.render(state)
        view.readInput() match
          case Key.UP     => moveSelection(delta = -1)
          case Key.DOWN   => moveSelection(delta = 1)
          case Key.LEFT   => moveSelection(delta = -1)
          case Key.RIGHT  => moveSelection(delta = 1)
          case Key.ENTER  => confirmAction()
          case Key.ESCAPE => running = false
          case _          => ()
    finally
      view.restore()

  /** Handles the user's confirm input (e.g., pressing ENTER) based on the current [[InputMode]].
   *
   * Behavior per mode:
   *   [[InputMode.ActionMenu]]: Triggers the currently highlighted menu action. If the action
   *   requires board card targeting (represented by placeholder index -1), it switches the UI to
   *   [[InputMode.SelectCardOnBoard]].
   *   [[InputMode.SelectCardOnBoard]]: Finds the corresponding board-targeting action for the
   *   selected card index and executes it.
   *   [[InputMode.WaitingRoom]]: Dismisses the privacy screen transition and restores normal
   *   gameplay input mode for the active player.
   */
  private def moveSelection(delta: Int): Unit = state.inputMode match
    case InputMode.ActionMenu =>
      val total = state.possibleAction.length
      if total > 0 then
        val newIndex = (state.selectedAction + delta + total) % total
        state = state.copy(selectedAction = newIndex)

    case InputMode.SelectCardOnBoard =>
      val total = state.playerCard.length
      if total > 0 then
        val newIndex = (state.selectedCardOnBoard + delta + total) % total
        state = state.copy(selectedCardOnBoard = newIndex, lastChangedPlayerCard = Some(newIndex))

    case InputMode.WaitingRoom => ()

  /** Handles the user's confirm input (e.g., pressing ENTER) based on the current [[InputMode]].
   *
   * Behavior per mode:
   *   - [[InputMode.ActionMenu]]: Triggers the currently highlighted menu action. If the action
   *     requires board card targeting (represented by placeholder index -1), it switches the UI to
   *     [[InputMode.SelectCardOnBoard]].
   *   - [[InputMode.SelectCardOnBoard]]: Finds the corresponding board-targeting action for the
   *     selected card index and executes it.
   *   - [[InputMode.WaitingRoom]]: Dismisses the privacy screen transition and restores normal
   *     gameplay input mode for the active player.
   */
  private def confirmAction(): Unit = state.inputMode match
    case InputMode.ActionMenu =>
      if currentModelActions.nonEmpty then
        currentModelActions(state.selectedAction) match
          case Action.ChooseDiscard(-1) | Action.ChooseReplace(-1) =>
            state = syncState(InputMode.SelectCardOnBoard, 0, 0)
          case action =>
            executeAction(action)

    case InputMode.SelectCardOnBoard =>
      val cardIndex = state.selectedCardOnBoard
      val targetAction = turn.actions.collectFirst {
        case Action.ChooseReplace(_) => Action.ChooseReplace(cardIndex)
        case Action.ChooseDiscard(_) => Action.ChooseDiscard(cardIndex)
      }
      targetAction.foreach(executeAction)

    case InputMode.WaitingRoom =>
      state = syncState(determineNextInputMode(), selectedAction = 0, selectedCardOnBoard = 0)

  /** Executes an [[Action]] against the current turn logic and synchronizes state.
   *
   * Registers initial observation completion when executing a [[Action.Confirm]] action,
   * applies the action to update the underlying [[Turn]] model, and delegates turn completion
   * and state synchronization checks.
   *
   * @param action
   * the domain [[Action]] to be performed
   */
  private def executeAction(action: Action): Unit =
    if action == Action.Confirm then observedPlayers += currentPlayer

    turn = turn.act(action)
    checkTurnEndAndSync(action)

  /** Evaluates if the current player's turn has ended and updates the game controller's state.
   *
   * If the turn is complete, this method toggles the active player, initializes the appropriate
   * turn type (a initial observation turn or a standard turn), and transitions the UI to
   * [[InputMode.WaitingRoom]]. Otherwise, it advances the UI state using the next expected
   * [[InputMode]] while preserving board selection coordinates.
   *
   * @param action
   * the latest [[Action]] executed within the turn
   */
  private def checkTurnEndAndSync(action: Action): Unit =
    if turn.isOver || action == Action.EndTurn then
      currentPlayer = currentPlayer.other
      turn = if !observedPlayers.contains(currentPlayer) then Turns.FirstTurn(turn.board, currentPlayer)
      else Turns.SimpleTurn(turn.board, currentPlayer)

      state = syncState(InputMode.WaitingRoom, selectedAction = 0, selectedCardOnBoard = 0)
    else
      state = syncState(determineNextInputMode(), selectedAction = 0, selectedCardOnBoard = state.selectedCardOnBoard)

  /** Determines the appropriate [[InputMode]] for the upcoming turn state based on
   * the available model actions.
   *
   * Evaluates whether all pending actions require selecting a specific card on
   * the player's board (such as replacing or discarding a card).
   *
   * @return
   * [[InputMode.SelectCardOnBoard]] if all available actions require card selection;
   * [[InputMode.ActionMenu]] otherwise
   */
  private def determineNextInputMode(): InputMode =
    val requiresBoardSelection = turn.actions.nonEmpty && turn.actions.forall {
      case Action.ChooseReplace(_) | Action.ChooseDiscard(_) => true
      case _                                                           => false
    }
    if requiresBoardSelection then InputMode.SelectCardOnBoard else InputMode.ActionMenu

  /** Synchronizes the current internal state ([[Turn]] and [[Board]]) with the UI [[GameState]].
   *
   * Updates available model and view actions, resets or preserves selections,
   * and constructs a view snapshot containing player fields, hand contents, and deck information.
   *
   * @param inputMode
   *   the current [[InputMode]] controlling user input behavior
   * @param selectedAction
   *   the zero-based index of the currently highlighted action in the menu
   * @param selectedCardOnBoard
   *   the zero-based index of the currently highlighted card on the player's board
   * @return
   *   an updated [[GameState]] ready for rendering by the view
   */
  private def syncState(inputMode: InputMode, selectedAction: Int, selectedCardOnBoard: Int): GameState =
    val board = turn.board
    val (modelActions, viewActions) = prepareActions(turn.actions)
    currentModelActions = modelActions

    GameState(
      adversaryCard = List.fill(board.getField(currentPlayer.other).cardsList.size)(None),
      playerCard = List.fill(board.getField(currentPlayer).cardsList.size)(None),
      remainingCardInDeck = board.deck.cards.size,
      lastDiscardedCard = Option.when(board.discardPile.nonEmpty)(board.getTopDiscardStack),
      cardsInHand = if turn.hand.nonEmpty then turn.hand.map(Some(_)) else List(None),
      possibleAction = viewActions,
      inputMode = inputMode,
      selectedAction = selectedAction,
      selectedCardOnBoard = selectedCardOnBoard
    )

  /**
   * Grouping logic for board-selection actions:
   *   If replacement actions (`ChooseReplace`) are present, groups them into a single option.
   *   If matching discard actions (`ChooseDiscard`) are present, prepends a single option
   *   for board selection alongside the other available actions.
   *
   * @param actions
   *   the list of raw model actions available in the current turn step
   * @return
   *   a tuple containing the filtered/mapped model actions and their corresponding view actions
   */
  private def prepareActions(actions: List[Action]): (List[Action], List[ViewAction]) =
    val hasReplace = actions.exists { case Action.ChooseReplace(_) => true; case _ => false }
    val hasDiscard = actions.exists { case Action.ChooseDiscard(_) => true; case _ => false }

    if hasReplace then
      (
        List(Action.ChooseReplace(-1)),
        List(ViewAction("select_replace", "Select a card from the board to swap"))
      )
    else if hasDiscard then
      val otherActions = actions.filterNot { case Action.ChooseDiscard(_) => true; case _ => false }
      val modelList = Action.ChooseDiscard(-1) :: otherActions
      val viewList = ViewAction(
        "select_discard",
        "Discard a card matching the rank of the top discard"
      ) :: otherActions.map(mapSingleAction)
      (modelList, viewList)
    else (actions, actions.map(mapSingleAction))

  /** Maps an individual model action ([[Action]]) to its corresponding
   * UI view representation ([[ViewAction]]) with a user-friendly display label.
   *
   * @param action
   * the [[Action]] to be translated into a view component
   * @return
   * the resulting [[ViewAction]] containing the action identifier and string label
   */
  private def mapSingleAction(action: Action): ViewAction = action match
    case Action.Observe          => ViewAction("observe", "Peek at the first two cards")
    case Action.Confirm          => ViewAction("confirm", "Confirm and cover")
    case Action.Draw             => ViewAction("draw", "Draw from deck")
    case Action.DrawKing         => ViewAction("draw_king", "Take King from discard pile")
    case Action.Activate         => ViewAction("activate", "Swap drawn card with a board card")
    case Action.Cactus           => ViewAction("cactus", "Call Cactus!")
    case Action.EndTurn          => ViewAction("end_turn", "End turn")
    case Action.ChooseDiscard(i) => ViewAction(s"discard_$i", s"Discard card $i")
    case Action.ChooseReplace(i) => ViewAction(s"replace_$i", s"Replace card $i")
