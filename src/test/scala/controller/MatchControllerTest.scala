package org.pps.functus
package controller

import model.board.Player
import model.board.Player.{Player1, Player2}
import model.playable.game.Match
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import view.CLIView
import view.utils.Key

class FakeMatchView extends CLIView:
  var renderMatchStatusCalls: List[(Map[Player, Int], Int)] = List.empty
  var renderMatchEndCalls: List[(Map[Player, Int], Option[Player], Int)] = List.empty

  override def renderMatchStatus(scores: Map[Player, Int], maxScore: Int): Unit =
    renderMatchStatusCalls = renderMatchStatusCalls :+ (scores, maxScore)

  override def renderMatchEnd(scores: Map[Player, Int], winner: Option[Player], maxScore: Int): Unit =
    renderMatchEndCalls = renderMatchEndCalls :+ (scores, winner, maxScore)

class MatchControllerTest extends AnyFunSuite with Matchers:

  test("Initialization: MatchController correctly encapsulates the provided Match model"):
    val initialMatch = Match(100)
    val matchController = MatchController(initialMatch)

    initialMatch.maxScore shouldBe 100
    initialMatch.isOver shouldBe false
    initialMatch.scores(Player1) shouldBe 0
    initialMatch.scores(Player2) shouldBe 0

  test("Winner determination: player with lower score should be determined as winner"):
    // In Cactus rules, lower total score wins
    val p1WinsScores = Map(Player1 -> 15, Player2 -> 45)
    val p2WinsScores = Map(Player1 -> 60, Player2 -> 20)
    val tieScores = Map(Player1 -> 30, Player2 -> 30)

    def determineWinner(scores: Map[Player, Int]): Option[Player] =
      val p1Score = scores(Player1)
      val p2Score = scores(Player2)
      if p1Score < p2Score then Some(Player1)
      else if p2Score < p1Score then Some(Player2)
      else None

    determineWinner(p1WinsScores) shouldBe Some(Player1)
    determineWinner(p2WinsScores) shouldBe Some(Player2)
    determineWinner(tieScores) shouldBe None

  test("Match completion state: match stops when a player reaches or exceeds maxScore"):
    val maxScore = 50
    val activeMatch = Match(maxScore)
    activeMatch.isOver shouldBe false

    val completedMatch = activeMatch.copy(scores = Map(Player1 -> 55, Player2 -> 30))
    completedMatch.isOver shouldBe true

  test("User Input: waitForEnter should terminate on Key.ENTER or Key.ESCAPE"):
    def simulateWaitForEnter(keys: List[Key]): Int =
      var remainingKeys = keys
      var pressedEnter = false
      var readCount = 0

      while !pressedEnter && remainingKeys.nonEmpty do
        val key = remainingKeys.head
        remainingKeys = remainingKeys.tail
        readCount += 1
        key match
          case Key.ENTER | Key.ESCAPE => pressedEnter = true
          case _                      => ()

      readCount

    val keySequenceWithEnter = List(Key.UP, Key.DOWN, Key.ENTER)
    simulateWaitForEnter(keySequenceWithEnter) shouldBe 3

    val keySequenceWithEscape = List(Key.LEFT, Key.ESCAPE)
    simulateWaitForEnter(keySequenceWithEscape) shouldBe 2

  test("Fake View Rendering: renderMatchStatus and renderMatchEnd correctly receive arguments"):
    val fakeView = FakeMatchView()
    val scores = Map(Player1 -> 20, Player2 -> 40)
    val maxScore = 50

    fakeView.renderMatchStatus(scores, maxScore)
    fakeView.renderMatchStatusCalls should contain((scores, maxScore))

    val winner = Some(Player1)
    fakeView.renderMatchEnd(scores, winner, maxScore)
    fakeView.renderMatchEndCalls should contain((scores, winner, maxScore))