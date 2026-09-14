package org.pps.functus
package model.playable.turn

import model.deck.card.Card
import model.board.{Board, Player}
import Action.*
import model.playable.Playable

/** Turn class that allows to play a turn from start to finish via the [[act]] method.
  * @param hand
  *   the player's hand
  * @param board
  *   the current game board
  * @param player
  *   the player playing this turn
  * @param actions
  *   the available actions
  * @param cactus
  *   whether cactus was called
  */
case class Turn(hand: List[Card], board: Board, player: Player, actions: List[Action], cactus: Boolean)
    extends Playable[Turn]:
  private val observableCards = 2

  /** Executes the given [[Action]] and returns the next phase of the turn
    *
    * @param action
    *   the [[Action]] to execute
    * @return
    *   the next phase of the [[Turn]]
    */
  override def act(action: Action): Turn =
    require(actions.contains(action))
    action.nextTurn(this)

  /** @return
    *   [[true]] if the [[Turn]] is over.
    */
  override def isOver: Boolean = this match
    case Turn(_, _, _, Nil, _) => true
    case _                     => false

/** Factory methods for the [[Turn]] class
  */
object Turns:

  extension (turn: Turn)
    /** Draws a card from the current player's field to the hand.
      * @param index
      *   the index of the card to draw.
      * @return
      *   a new [[Turn]]
      */
    def drawnFromField(index: Int): Turn =
      val (drawn, newBoard) = turn.board.drawPlayerCard(turn.player, index)
      turn.copy(turn.hand.appended(drawn), newBoard)

    /** Filters the given list of actions and removes the [[Cactus]] action if it was already called previously.
      * @param newActions
      *   the actions to filter
      * @return
      *   a new [[Turn]] with filtered actions
      */
    def filterActions(newActions: List[Action]): Turn =
      val filteredActions =
        if turn.cactus then for action <- newActions if action != Cactus yield action
        else newActions
      turn.copy(actions = filteredActions)

    /** Discards the first card in the hand to the discard pile without applying any penalty.
      * @return
      *   the new [[Turn]]
      */
    def discardWithoutPenalty: Turn = turn.copy(Nil, turn.board.discard(turn.hand.head))

    /** Draws a card from the given player's field and places it in the hand.
      * @param index
      *   the index of the card
      * @param from
      *   the [[Player]] to draw the card from
      * @return
      *   the new [[Turn]]
      */
    def drawFromPlayer(index: Int, from: Player): Turn =
      val (drawn, newBoard) = turn.board.drawPlayerCard(from, index)
      turn.copy(turn.hand.appended(drawn), newBoard)

    /** Places the last card in the hand into the given player's field.
      * @param fieldOwner
      *   the [[Player]] that will receive the card.
      * @param index
      *   the index where the card will be placed within the [[Field]]
      * @return
      *   the new [[Turn]]
      */
    def placeHandInField(fieldOwner: Player, index: Int): Turn =
      turn.copy(turn.hand.tail, turn.board.placeCardInField(turn.hand.head, fieldOwner, Some(index)))

    /** Draws a card from the deck to the hand.
      * @return
      *   the new [[Turn]]
      */
    def drawFromDeck: Turn =
      val (drawn, newBoard) =
        turn.board.draw().getOrElse(throw IllegalStateException("Deck is empty during draw action"))
      turn.copy(drawn :: Nil, newBoard)

    /** Draws the king from the top of the discard pile to the hand.
      * @return
      *   the new [[Turn]]
      */
    def drawFromPile: Turn =
      val (kingFromDiscard, newBoard) = turn.board.kingTopDiscardStack()
      turn.copy(kingFromDiscard :: Nil, newBoard)

    /** Places the hand into the current player's field and discards the replaced card.
      * @param index
      *   the index of the card to be replaced.
      * @return
      *   the new [[Turn]]
      */
    def replaceHandIntoField(index: Int): Turn =
      val newBoard = turn.board.replace(turn.player, index, turn.hand.head)
      turn.copy(Nil, newBoard)

    /** Attempts to discard the hand, if the value of the card matches the value of the top of the discard stack the
      * card will be discarded without penalty. Otherwise, the card will return to the player's field and a new card
      * will be added to said field.
      * @param index
      *   the index the card in hand will be returned to in case of penalty.
      * @return
      *   the new [[Turn]]
      */
    def discardHand(index: Int): Turn =
      val topOfDiscardStackValue = turn.board.getTopDiscardStack.value
      turn.hand.head.value match
        case `topOfDiscardStackValue` => discardWithoutPenalty
        case _                        =>
          val restoredBoard = turn.board.placeCardInField(turn.hand.head, turn.player, Some(index))
          val (drawn, boardAfterDraw) =
            restoredBoard.draw().getOrElse(throw IllegalStateException("Deck is empty during draw action"))
          turn.copy(Nil, boardAfterDraw.placeCardInField(drawn, turn.player), turn.player)

    /** Swaps a card in the current player's field to the other player's field.
      * @param playerIndex
      *   the index of the card belonging to the current [[Player]]
      * @param opponentIndex
      *   the index of the card belonging to the other [[Player]]
      * @return
      *   the new [[Turn]]
      */
    def swapWithOpponent(playerIndex: Int, opponentIndex: Int): Turn =
      turn.discardWithoutPenalty
        .drawFromPlayer(opponentIndex, turn.player.other)
        .drawFromPlayer(playerIndex, turn.player)
        .placeHandInField(turn.player, playerIndex)
        .placeHandInField(turn.player.other, opponentIndex)

    /** Calls cactus.
      * @return
      *   the new [[Turn]]
      */
    def callCactus: Turn = turn.copy(cactus = true)

    /** Returns the cards from the hand to the current player's field.
      * @return
      *   the new [[Turn]]
      */
    def returnObservedCards: Turn =
      val newBoard = turn.hand.foldRight(turn.board)((card, b) => b.placeCardInField(card, turn.player, Some(0)))
      turn.copy(Nil, newBoard)

  object FirstTurn:
    /** Creates a [[Turn]] that allows the [[Player]] to observe the first 2 cards on their field.
      */
    def apply(board: Board, player: Player): Turn = Turn(Nil, board, player, List(Observe), false)

  object SimpleTurn:
    /** Creates a [[Turn]]
      * @param board
      *   the [[Board]] at the start of the [[Turn]]
      * @param player
      *   the [[Player]] that plays the [[Turn]]
      * @return
      *   the [[Turn]]
      */
    def apply(board: Board, player: Player): Turn =
      board.draw() match
        case None => throw IllegalStateException("There are no cards in either the deck or the discard pile")
        case _    =>
          board.discardPile.length match
            case 0 => Turn(Nil, board, player, List(Draw), false)
            case _ =>
              if board.checkKingTopDiscardStack then Turn(Nil, board, player, List(Draw, DrawKing), false)
              else
                val nextActions =
                  Draw :: (0 until board.getField(player).length).map(index => ChooseDiscard(index)).toList
                Turn(Nil, board, player, nextActions, false)

  object LastTurn:
    /** Creates the last [[Turn]] within a [[Game]], functionally the same as a [[SimpleTurn]], but the [[Cactus]]
      * action is not present.
      * @param board
      *   the [[Board]] at the start of the [[Turn]]
      * @param player
      *   the [[Player]] that plays the [[Turn]]
      * @return
      *   the [[Turn]]
      */
    def apply(board: Board, player: Player): Turn =
      SimpleTurn(board, player).copy(cactus = true)
