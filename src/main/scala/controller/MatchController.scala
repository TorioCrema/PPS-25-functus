package org.pps.functus
package controller

import model.playable.game.Match

import model.board.Player
import model.board.Player.{Player1, Player2}
import view.CLIView
import view.utils.{Key, Utils}

class MatchController(private var matchRecord: Match = Match(50)):

  private val view: CLIView = CLIView()

  def start(): Unit =
    while !matchRecord.isOver do

      val gameController = GameController(matchRecord)
      gameController.start()

      matchRecord = gameController.getPlayable

      if !matchRecord.isOver then
        view.renderMatchStatus(matchRecord.scores, matchRecord.maxScore)
        waitForEnter()
        matchRecord = matchRecord.nextGame

    val winner = determineWinner(matchRecord.scores)
    view.renderMatchEnd(matchRecord.scores, winner, matchRecord.maxScore)
    waitForEnter()

  private def determineWinner(scores: Map[Player, Int]): Option[Player] =
    val p1Score = scores(Player1)
    val p2Score = scores(Player2)
    if p1Score < p2Score then Some(Player1)
    else if p2Score < p1Score then Some(Player2)
    else None

  private def waitForEnter(): Unit =
    var pressedEnter = false
    while !pressedEnter do
      Utils.readInput() match
        case Key.ENTER | Key.ESCAPE => pressedEnter = true
        case _                      => ()
