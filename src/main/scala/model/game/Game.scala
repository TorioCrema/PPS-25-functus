package org.pps.functus
package model.game

import model.board.{Board, Player}
import model.board.Player.{Player1, Player2}
import model.turn.{Action, Turn}
import model.turn.Turns.{FirstTurn, SimpleTurn}
import model.deck.card.Card

/** Represents the phase of the game.
  */
enum GamePhase:
  /** Both players are playing their first turn (observing their initial cards). */
  case FirstTurns

  /** Normal game play: players alternate simple turns until cactus is called. */
  case Playing

  /** Cactus has been called; the other player gets one last turn. */
  case LastTurn

  /** The game is over; no more turns can be played. */
  case Over

case class Game(
    board: Board,
    phase: GamePhase,
    currentTurn: Turn,
    cactusCaller: Option[Player]
):
  def currentPlayer: Player = currentTurn.player

  def isOver: Boolean = phase == GamePhase.Over

  /** Delegates an [[Action]] to the current [[Turn]] and advances the game state.
    *
    * If the action completes the current turn (`turn.isOver`), the game transitions automatically to the next phase /
    * player.
    *
    * @param action
    *   the action to execute (must be in `currentTurn.actions`)
    * @return
    *   the updated [[Game]]
    * @throws IllegalStateException
    *   if called when the game is already [[GamePhase.Over]]
    */
  def act(action: Action): Game =
    if isOver then throw IllegalStateException("Game is already over, you cannot make any new actions.")
    val updatedTurn = currentTurn.act(action)
    if !updatedTurn.isOver then copy(currentTurn = updatedTurn)
    else advancePhase(updatedTurn)

  /** Returns the score of the cards on the field of each player at the end of the game.
    *
    * @return
    *   a [[Map]] from [[Player]] to their scores
    */
  def playerScore: Map[Player, Int] =
    Player.values.map(p => p -> board.getField(p).cardsList.map(_.value).sum).toMap

  private def advancePhase(finishedTurn: Turn): Game =
    phase match
      case GamePhase.FirstTurns =>
        finishedTurn.player match
          case Player1 =>
            val nextTurn = FirstTurn(finishedTurn.board, Player2)
            copy(board = finishedTurn.board, currentTurn = nextTurn)
          case Player2 =>
            val nextTurn = SimpleTurn(finishedTurn.board, Player1)
            copy(board = finishedTurn.board, phase = GamePhase.Playing, currentTurn = nextTurn)

      case GamePhase.Playing =>
        if finishedTurn.cactus then
          val opponent = finishedTurn.player.other
          val lastTurn = SimpleTurn(finishedTurn.board, opponent)
          copy(
            board = finishedTurn.board,
            phase = GamePhase.LastTurn,
            currentTurn = lastTurn,
            cactusCaller = Some(finishedTurn.player)
          )
        else
          val nextTurn = SimpleTurn(finishedTurn.board, finishedTurn.player.other)
          copy(board = finishedTurn.board, currentTurn = nextTurn)

      case GamePhase.LastTurn =>
        copy(board = finishedTurn.board, phase = GamePhase.Over)

      case GamePhase.Over => this

/** Factory for [[Game]] instances.
  */
object Game:

  /** Creates a new [[Game]] starting from the given [[Board]].
    *
    * The game begins in [[GamePhase.FirstTurns]] with Player1's first turn.
    *
    * @param board
    *   the initial [[Board]] (typically built via [[model.deck.sugar.BoardDSL]])
    * @return
    *   a fresh [[Game]] ready to be played
    */
  def apply(board: Board): Game =
    val firstTurn = FirstTurn(board, Player1)
    Game(board, GamePhase.FirstTurns, firstTurn, cactusCaller = None)
