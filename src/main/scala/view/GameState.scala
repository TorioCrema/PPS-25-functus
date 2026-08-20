package org.pps.functus
package view

import model.deck.card.Card
import view.InputMode.ActionMenu

/** * a case class containing all the information needed by the view representing the actual game state
  * @param adversaryCard
  *   The list of Cards of the opponent
  * @param playerCard
  *   The list of Cards of the player
  * @param remainingCardInDeck
  *   the number of card remaining on the deck
  * @param lastDiscardedCard
  *   the last discarded Card
  * @param cardsInHand
  *   the List of card actually in the hand of the active player
  * @param possibleAction
  *   the List of possible action the player can decide to execute
  * @param inputMode
  *   the modality in which the game actually is,
  * @param selectedAction
  *   the index of the action the player is hovering
  * @param selectedCardOnBoard
  *   the index of the card the player is overing
 *  @param lastChangedPlayerCard
 *    the Index of the last card exchanged on the board by the player 
  */
case class GameState(
                      adversaryCard: List[Option[Card]],
                      playerCard: List[Option[Card]],
                      remainingCardInDeck: Int,
                      lastDiscardedCard: Option[Card],
                      cardsInHand: List[Option[Card]],
                      possibleAction: List[ViewAction],
                      inputMode: InputMode = ActionMenu,
                      selectedAction: Int = 0,
                      selectedCardOnBoard: Int = 0,
                      lastChangedPlayerCard: Option[Int] = None
)
