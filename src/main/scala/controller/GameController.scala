package org.pps.functus
package controller

import model.board.{BoardFactory, Player}
import view.{CLIMenu, CLIView, GameState, InputMode, Key, ViewAction}
import model.playable.turn.{Action, Turn}
import model.playable.game.{Game, GamePhase}
import view.InputMode.*
import model.board.Player.*

import view.utils.Utils

class GameController(
    private val view: CLIView,
    private var game: Game = Game(BoardFactory.BoardWithPopulatedFields())
):

  private var turn: Turn = game.currentTurn
  private var observedPlayers: Set[Player] = Set.empty

  private var currentModelActions: List[Action] = Nil
  private var state: GameState = syncState(InputMode.ActionMenu)
  private var pendingOpponentSwapIdx: Option[Int] = None
  private var selectedMacroAction: Option[Action] = None

  private val TO_BE_SELECTED = -1
  private val STEP_NEXT = -1
  private val STEP_PREVIOUS = 1

  /** Starts the main game loop, initializing the view and processing user input.
    */
  def start(): Unit =
    var running = true
    while running do
      view.render(state)
      Utils.readInput() match
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
          case Action.ObservePlayer(TO_BE_SELECTED) | Action.ChooseReplace(TO_BE_SELECTED) |
              Action.ChooseDiscard(TO_BE_SELECTED) =>
            state = syncState(InputMode.SelectCardOnBoard)

          case Action.ObserveOpponent(TO_BE_SELECTED) | Action.GiveBack(TO_BE_SELECTED) =>
            state = syncState(InputMode.SelectAdversaryCardOnBoard)

          case Action.Swap(TO_BE_SELECTED, TO_BE_SELECTED) =>
            pendingOpponentSwapIdx = None
            state = syncState(InputMode.SelectAdversaryCardOnBoard)

          case action =>
            selectedMacroAction = None
            executeAction(action)

    case InputMode.SelectCardOnBoard =>
      val cardIndex = state.selectedCardOnBoard

      pendingOpponentSwapIdx match
        case Some(oppIdx) =>
          val swapAction = turn.actions.collectFirst { case Action.Swap(pIdx, oIdx) =>
            Action.Swap(pIdx, oIdx)
          }
          pendingOpponentSwapIdx = None
          selectedMacroAction = None
          swapAction.foreach(executeAction)

        case None =>
          val targetAction = selectedMacroAction match
            case Some(Action.ObservePlayer(TO_BE_SELECTED)) =>
              turn.actions.collectFirst { case Action.ObservePlayer(_) => Action.ObservePlayer(cardIndex) }

            case Some(Action.ChooseReplace(TO_BE_SELECTED)) =>
              turn.actions.collectFirst { case Action.ChooseReplace(_) => Action.ChooseReplace(cardIndex) }

            case Some(Action.ChooseDiscard(TO_BE_SELECTED)) =>
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
      state = syncState(determineNextInputMode())

    case EndGame => MenuController(CLIMenu()).start()

  /** Executes an [[Action]] against the current turn logic and synchronizes state.
    *
    * Registers initial observation completion when executing a [[Action.Confirm]] action, applies the action to update
    * the underlying [[Turn]] model, and delegates turn completion and state synchronization checks.
    *
    * @param action
    *   the domain [[Action]] to be performed
    */
  private def executeAction(action: Action): Unit =
    game = game.act(action)
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
      else state = syncState(InputMode.WaitingRoom)
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
      adversaryScore = if isEndgame then getAdversaryScore else 0
    )

  /** Grouping logic for board-selection actions: If actions like [[Action.ChooseReplace]], [[Action.ChooseDiscard]],
    * [[Action.ObserveOpponent]], [[Action.ObservePlayer]], [[Action.Swap]] are present, groups them into a single
    * option. prepending them alongside the other available actions.
    *
    * @param actions
    *   the list of [[Action]] available in the current turn step
    * @return
    *   a tuple containing the filtered/mapped [[Action]] and their corresponding [[ViewAction]]
    */
  private def prepareActions(actions: List[Action]): (List[Action], List[ViewAction]) =
    val hasObserveOpponent = actions.exists { case Action.ObserveOpponent(_) => true; case _ => false }
    val hasObservePlayer = actions.exists { case Action.ObservePlayer(_) => true; case _ => false }
    val hasSwap = actions.exists { case Action.Swap(_, _) => true; case _ => false }
    val hasReplace = actions.exists { case Action.ChooseReplace(_) => true; case _ => false }
    val hasChooseDiscard = actions.exists { case Action.ChooseDiscard(_) => true; case _ => false }

    if hasObservePlayer then
      val otherActions = actions.filterNot { case Action.ObservePlayer(_) => true; case _ => false }
      val (otherModel, otherView) = prepareActions(otherActions)
      (
        Action.ObservePlayer(TO_BE_SELECTED) :: otherModel,
        ViewAction("use_effect_player", "Use card effect (Peek at your card)") :: otherView
      )
    else if hasObserveOpponent then
      val otherActions = actions.filterNot { case Action.ObserveOpponent(_) => true; case _ => false }
      val (otherModel, otherView) = prepareActions(otherActions)
      (
        Action.ObserveOpponent(TO_BE_SELECTED) :: otherModel,
        ViewAction("use_effect_opp", "Use card effect (Peek at opponent card)") :: otherView
      )
    else if hasSwap then
      val otherActions = actions.filterNot { case Action.Swap(_, _) => true; case _ => false }
      val (otherModel, otherView) = prepareActions(otherActions)
      (
        Action.Swap(TO_BE_SELECTED, TO_BE_SELECTED) :: otherModel,
        ViewAction("use_effect_swap", "Use card effect (Swap cards)") :: otherView
      )
    else if hasReplace then
      val otherActions = actions.filterNot { case Action.ChooseReplace(_) => true; case _ => false }
      val (otherModel, otherView) = prepareActions(otherActions)
      (
        Action.ChooseReplace(TO_BE_SELECTED) :: otherModel,
        ViewAction("select_replace", "Swap drawn card with a board card") :: otherView
      )
    else if hasChooseDiscard then
      val otherActions = actions.filterNot { case Action.ChooseDiscard(_) => true; case _ => false }
      val (otherModel, otherView) = prepareActions(otherActions)
      (
        Action.ChooseDiscard(TO_BE_SELECTED) :: otherModel,
        ViewAction("select_discard", "Discard matching card from board") :: otherView
      )
    else (actions, actions.map(mapSingleAction))

  /** Maps an individual model action ([[Action]]) to its corresponding UI view representation ([[ViewAction]]) with a
    * user-friendly display label.
    *
    * @param action
    *   the [[Action]] to be translated into a view component
    * @return
    *   the resulting [[ViewAction]] containing the action identifier and string label
    */
  private def mapSingleAction(action: Action): ViewAction = action match
    case Action.Observe  => ViewAction("observe", "Peek at the first two cards")
    case Action.Confirm  => ViewAction("confirm", "Confirm and cover")
    case Action.Draw     => ViewAction("draw", "Draw from deck")
    case Action.DrawKing => ViewAction("draw_king", "Take King from discard pile")
    case Action.Activate =>
      val hasSpecialEffect = turn.hand.headOption.exists(c => c.value == 6 || c.value == 7 || c.value == 8)
      if hasSpecialEffect then ViewAction("activate", "Use card effect or replace")
      else ViewAction("activate", "Swap drawn card with a board card")
    case Action.EndTurn            => ViewAction("end_turn", "End turn")
    case Action.Cactus             => ViewAction("cactus", "Call Cactus!")
    case Action.ChooseDiscard(i)   => ViewAction(s"discard_$i", s"Discard card")
    case Action.ChooseReplace(i)   => ViewAction(s"replace_$i", s"Replace card ")
    case Action.Discard(i)         => ViewAction(s"discard_$i", s"Discard card ")
    case Action.ObserveOpponent(i) => ViewAction(s"obs_opp_$i", s"Peek at opponent card")
    case Action.GiveBack(i)        => ViewAction(s"give_back_$i", s"Return card to opponent")
    case Action.ObservePlayer(i)   => ViewAction(s"obs_player_$i", s"Peek at your card")
    case Action.ReturnToField(i)   => ViewAction(s"return_$i", s"Return card to your field")
    case Action.Swap(pIdx, oIdx)   => ViewAction(s"swap_${pIdx}_$oIdx", s"Swap your card with opponent's")

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
