package org.pps.functus
package model.playable.game

import model.board.{Board, BoardFactory, Player}
import model.board.Player.*
import model.deck.sugar.BoardDSL.{board as newBoard, *}
import model.playable.Playable
import model.playable.turn.{Action, Turn}

/** Represent a match composed of multiple games, ends when at least one player reaches a score above the maximum.
  * @param maxScore
  *   maximum point threshold
  * @param game
  *   the current [[Game]] being played
  * @param scores
  *   the current accumulated player scores
  */
case class Match(maxScore: Int, game: Game, scores: Map[Player, Int]) extends Playable[Match]:

  export game.{act as _, isOver as isGameOver, *}

  /** Returns true if the match is over. */
  override def isOver: Boolean = scores.values.max > maxScore

  /** Acts upon the current [[Turn]] and calculates end scores if the game is over.
    * @param action
    *   the [[Action]] to execute.
    * @return
    *   the [[Match]] with updated [[Game]].
    */
  override def act(action: Action): Match =
    val nextGame = game.act(action)
    if nextGame.isOver then
      Match(
        maxScore,
        nextGame,
        scores.map((player, score) =>
          (
            player,
            score + nextGame.playerScore(player) match
              case `maxScore` => maxScore / 2
              case score      => score
          )
        )
      )
    else copy(game = nextGame)

  /** Returns a [[Match]] with a new [[Game]] if the current [[Game]] is over
    * @throws java.lang.IllegalStateException
    *   if the current [[Game]] isn't over.
    */
  def nextGame: Match =
    if !game.isOver then throw new IllegalStateException("Cannot start new game while current game has not ended.")
    copy(game = Game(newBoard from default))

object Match:
  /** Creates a new [[Match]] with a given max score and [[Board]], and player scores at zero.
    * @param maxScore
    *   the score threshold that determines the end of the [[Match]]
    * @param board
    *   the [[Board]] to start with, defaults to the default board
    * @return
    *   a new [[Match]]
    */
  def apply(maxScore: Int, board: Board = BoardFactory.BoardWithPopulatedFields()): Match =
    Match(maxScore, Game(board), Map((Player1, 0), (Player2, 0)))
