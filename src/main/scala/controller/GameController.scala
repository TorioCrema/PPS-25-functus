package org.pps.functus
package controller

import model.board.Player
import view.CLIView
import model.playable.turn.{Action, Turn}
import model.playable.game.{Game, GamePhase, Match}

import utils.InputMode.*
import model.board.Player.*
import model.playable.Playable
import model.opponent.Opponent

import utils.{GameState, InputMode, Key, Utils, mapSingleAction, prepareActions}

class GameController[P <: Playable[P]](
    private var playable: P,
    private val isVsBot: Boolean = false,
    private val view: CLIView = CLIView(),
    private val inputReader: () => Key = () => Utils.readInput()
):

  private def currentGame: Game = playable match
    case m: Match => m.game
    case g: Game  => g

  private var game: Game = currentGame
  private var turn: Turn = game.currentTurn
  private var observedPlayers: Set[Player] = Set.empty

  private var currentModelActions: List[Action] = Nil
  private var actionHistory: List[Action] = Nil
  private var state: GameState = syncState(InputMode.ActionMenu)
  private var pendingOpponentSwapIdx: Option[Int] = None
  private var selectedMacroAction: Option[Action] = None

  private val STEP_NEXT = -1
  private val STEP_PREVIOUS = 1
  private var running = true

  private val opponentBot: Opponent = Opponent()

  /** Starts the main game loop, initializing the view and processing user input.
    */
  def start(): Unit =
    while running do
      if isVsBot && turn.player == Player2 && running && state != WaitingRoom && !game.isOver then botTurn()
      else
        view.render(state)
        inputReader() match
          case Key.UP | Key.LEFT    => moveSelection(delta = STEP_NEXT)
          case Key.DOWN | Key.RIGHT => moveSelection(delta = STEP_PREVIOUS)
          case Key.ENTER            => confirmAction()
          case Key.ESCAPE           => running = false
          case _                    => ()

  /** Handles the user's arrow input (e.g., pressing UP, DOWN) based on the current [[InputMode]].
    *
    * Behavior per mode: [[InputMode.ActionMenu]]: Switching current selection between the possible action
    * [[InputMode.SelectCardOnBoard]] and [[InputMode.SelectAdversaryCardOnBoard]]: navigate between card on the player
    * or adversary field [[InputMode.WaitingRoom]]: No behavior expected
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
        state = state.copy(selectedCardOnBoard = newIndex)

    case InputMode.SelectAdversaryCardOnBoard =>
      val total = state.adversaryCard.length
      if total > 0 then
        val newIndex = (state.selectedCardOnBoard + delta + total) % total
        state = state.copy(selectedCardOnBoard = newIndex)

    case _ => ()

  /** Handles the user's confirmation input (e.g., pressing ENTER) based on the current [[InputMode]].
    *
    * Behavior per mode:
    *   - [[InputMode.ActionMenu]]: Triggers the currently highlighted menu action. If the action requires board card
    *     targeting (represented by placeholder index -1), it switches the UI to [[InputMode.SelectCardOnBoard]].
    *   - [[InputMode.SelectCardOnBoard]] and [[InputMode.SelectAdversaryCardOnBoard]]: Finds the corresponding
    *     board-targeting action for the selected card index and executes it.
    *   - [[InputMode.WaitingRoom]]: Dismisses the privacy screen transition and restores normal gameplay input mode for
    *     the active player.
    */
  private def confirmAction(): Unit = state.inputMode match
    case InputMode.ActionMenu =>
      if currentModelActions.nonEmpty then
        val chosenAction = currentModelActions(state.selectedAction)
        selectedMacroAction = Some(chosenAction)

        chosenAction match
          case Action.ObservePlayer(_) | Action.ChooseReplace(_) |
              Action.ChooseDiscard(_) =>
            state = syncState(InputMode.SelectCardOnBoard)

          case Action.ObserveOpponent(_) | Action.GiveBack(_) =>
            state = syncState(InputMode.SelectAdversaryCardOnBoard)

          case Action.Swap(_, _) =>
            pendingOpponentSwapIdx = None
            state = syncState(InputMode.SelectAdversaryCardOnBoard)

          case action =>
            selectedMacroAction = None
            executeAction(action)

    case InputMode.SelectCardOnBoard =>
      val cardIndex = state.selectedCardOnBoard

      pendingOpponentSwapIdx match
        case Some(oppIdx) =>
          val swapAction = turn.actions.collectFirst { case Action.Swap(`cardIndex`, `oppIdx`) =>
            Action.Swap(cardIndex, oppIdx)
          }
          pendingOpponentSwapIdx = None
          selectedMacroAction = None
          swapAction.foreach(executeAction)

        case None =>
          val targetAction = selectedMacroAction match
            case Some(Action.ObservePlayer(_)) =>
              turn.actions.collectFirst { case Action.ObservePlayer(_) => Action.ObservePlayer(cardIndex) }

            case Some(Action.ChooseReplace(_)) =>
              turn.actions.collectFirst { case Action.ChooseReplace(_) => Action.ChooseReplace(cardIndex) }

            case Some(Action.ChooseDiscard(_)) =>
              turn.actions.collectFirst { case Action.ChooseDiscard(_) => Action.ChooseDiscard(cardIndex) }

            case _ =>
              turn.actions.collectFirst {
                case Action.ObservePlayer(_) => Action.ObservePlayer(cardIndex)
                case Action.ChooseReplace(_) => Action.ChooseReplace(cardIndex)
                case Action.ReturnToField(_) => Action.ReturnToField(cardIndex)
              }

          selectedMacroAction = None
          targetAction.foreach(executeAction)

    case InputMode.SelectAdversaryCardOnBoard =>
      val cardIndex = state.selectedCardOnBoard
      val isSwapPhase = turn.actions.exists { case Action.Swap(_, _) => true; case _ => false }

      if isSwapPhase then
        pendingOpponentSwapIdx = Some(cardIndex)
        state = syncState(InputMode.SelectCardOnBoard)
      else
        val targetAction = turn.actions.collectFirst {
          case Action.ObserveOpponent(_) => Action.ObserveOpponent(cardIndex)
          case Action.GiveBack(_)        => Action.GiveBack(cardIndex)
        }
        targetAction.foreach(executeAction)

    case InputMode.WaitingRoom =>
      pendingOpponentSwapIdx = None
      actionHistory = Nil
      state = syncState(determineNextInputMode())

    case EndGame => running = false

  /** Executes an [[Action]] against the current turn logic and synchronizes state.
    *
    * Registers initial observation completion when executing a [[Action.Confirm]] action, applies the action to update
    * the underlying [[Turn]] model, and delegates turn completion and state synchronization checks.
    *
    * @param action
    *   the domain [[Action]] to be performed
    */
  private def executeAction(action: Action): Unit =
    if isVsBot && turn.player == Player1 then opponentBot.react(action, turn)

    if !isMandatoryOrRoutine(action) then actionHistory = actionHistory :+ action

    playable = playable.act(action)
    game = currentGame
    turn = game.currentTurn
    checkTurnEndAndSync(action)

  /** Evaluates if the current player's turn has ended and updates the game controller's state.
    *
    * If the turn is complete, this method toggles the active player, initializes the appropriate turn type (an initial
    * observation turn or a standard turn), and transitions the UI to [[InputMode.WaitingRoom]]. Otherwise, it advances
    * the UI state using the next expected [[InputMode]] while preserving board selection coordinates.
    *
    * @param action
    *   the latest [[Action]] executed within the turn
    */
  private def checkTurnEndAndSync(action: Action): Unit =

    if action.equals(Action.EndTurn) then
      if game.phase.equals(GamePhase.Over) then state = syncState(InputMode.EndGame)
      else if isVsBot then
        // If Player 2 (Bot) just ended its turn, show the WaitingRoom to Player 1
        if turn.player == Player1 then state = syncState(InputMode.WaitingRoom)
        else
          state = syncState(determineNextInputMode())
          actionHistory = Nil
      else
        // Local 2-Player mode: Always show WaitingRoom between turns
        state = syncState(InputMode.WaitingRoom)
    else state = syncState(determineNextInputMode(), selectedCardOnBoard = state.selectedCardOnBoard)

  /** Determines the appropriate [[InputMode]] for the upcoming turn state based on the available model actions.
    *
    * Evaluates whether all pending actions require selecting a specific card on the player's or adversary board (such
    * as replacing or discarding a card).
    *
    * @return
    *   [[InputMode.SelectCardOnBoard]] if all available actions or the selected one require card selection;
    *   [[InputMode.SelectAdversaryCardOnBoard]] if the selected action require adversary card selection;
    *   [[InputMode.ActionMenu]] otherwise
    */
  private def determineNextInputMode(): InputMode =
    val hasObservePlayer = turn.actions.exists { case Action.ObservePlayer(_) => true; case _ => false }
    val hasReplace = turn.actions.exists { case Action.ChooseReplace(_) => true; case _ => false }

    if hasObservePlayer && hasReplace then InputMode.ActionMenu
    else if turn.actions.nonEmpty && turn.actions.forall {
        case Action.ObserveOpponent(_) => true
        case _                         => false
      }
    then InputMode.SelectAdversaryCardOnBoard
    else if turn.actions.nonEmpty && turn.actions.forall {
        case Action.ChooseReplace(_) | Action.ChooseDiscard(_) => true
        case _                                                 => false
      }
    then InputMode.SelectCardOnBoard
    else InputMode.ActionMenu

  /** Synchronizes the current internal state ([[Turn]] and [[Board]]) with the UI [[GameState]].
    *
    * Updates available model and view actions, resets or preserves selections, and constructs a view snapshot
    * containing player fields, hand contents, and deck information.
    *
    * @param inputMode
    *   the current [[InputMode]] controlling user input behavior
    * @param selectedAction
    *   the zero-based index of the currently highlighted action in the menu (default: 0)
    * @param selectedCardOnBoard
    *   the zero-based index of the currently highlighted card on the player's board (default: 0)
    * @return
    *   an updated [[GameState]] ready for rendering by the view
    */
  private def syncState(inputMode: InputMode, selectedAction: Int = 0, selectedCardOnBoard: Int = 0): GameState =
    val board = turn.board
    val (modelActions, viewActions) = prepareActions(turn.actions)
    currentModelActions = modelActions
    val isEndgame = inputMode.equals(EndGame)

    val historyLabel = if actionHistory.nonEmpty then actionHistory.map(mapSingleAction(_).label).mkString(", ")
    else ""

    GameState(
      adversaryCard = if isEndgame then board.getField(turn.player.other).cardsList.map(Some(_))
      else List.fill(board.getField(turn.player.other).cardsList.size)(None),
      playerCard = if isEndgame then board.getField(turn.player).cardsList.map(Some(_))
      else List.fill(board.getField(turn.player).cardsList.size)(None),
      remainingCardInDeck = board.deck.cards.size,
      lastDiscardedCard = Option.when(board.discardPile.nonEmpty)(board.getTopDiscardStack),
      cardsInHand = if turn.hand.nonEmpty then turn.hand.map(Some(_)) else List(None),
      possibleAction = if isEndgame then List.empty else viewActions,
      inputMode = inputMode,
      selectedAction = selectedAction,
      selectedCardOnBoard = selectedCardOnBoard,
      winner = if isEndgame then getWinner else None,
      playerScore = if isEndgame then getPlayerScore else 0,
      adversaryScore = if isEndgame then getAdversaryScore else 0,
      actionHistory = historyLabel,
      isVsBot = isVsBot
    )

  
  def getWinner: Option[Player] =
    val scores = game.playerScore
    val p1Score = scores(Player1)
    val p2Score = scores(Player2)

    val winner =
      if p1Score > p2Score then Some(Player2)
      else if p2Score > p1Score then Some(Player1)
      else None // Handle a tie scenario
    winner

  private def getPlayerScore: Int =
    game.playerScore(Player1)

  private def getAdversaryScore: Int =
    game.playerScore(Player2)

  def playerScore(): Map[Player, Int] =
    game.playerScore

  def getGame: Game = game

  def getPlayable: P = playable
  
  /** Executes actions on behalf of the bot until its turn ends */
  private def botTurn(): Unit =
    if !game.isOver then
      val (nextTurn, chosenAction) = opponentBot.play(turn)
      executeAction(chosenAction)

  private def isMandatoryOrRoutine(action: Action): Boolean = action match
    case Action.Draw | Action.EndTurn | Action.Confirm  | Action.Activate | Action.ChooseDiscard => true
    case _ => false
