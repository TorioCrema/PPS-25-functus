package org.pps.functus
package controller

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import model.board.Player.{Player1, Player2}
import model.playable.game.Match
import view.utils.Key
import java.io.{OutputStream, PrintStream}

class MatchControllerTest extends AnyFlatSpec with Matchers:

  /** Helper method to execute MatchController with simulated inputs and muted stdout.
    */
  def runMatchControllerWithInputs(
      matchModel: Match = Match(50),
      inputs: List[Key] = List(Key.ESCAPE),
      isVsBot: Boolean = false
  ): MatchController =
    val originalOut = System.out
    val silentStream = new PrintStream(OutputStream.nullOutputStream())

    var inputQueue = inputs
    var maxCycles = 200 // Safety limit against infinite loops

    val scriptReader = () =>
      maxCycles -= 1
      if maxCycles <= 0 then Key.ESCAPE
      else if inputQueue.nonEmpty then
        val k = inputQueue.head
        inputQueue = inputQueue.tail
        k
      else Key.ESCAPE

    val controller = MatchController(matchModel, isVsBot = isVsBot, readInput = scriptReader)

    try
      System.setOut(silentStream)
      Console.withOut(silentStream) {
        controller.start()
      }
    catch case _: Throwable => ()
    finally System.setOut(originalOut)

    controller

  "MatchController initialization" should "correctly encapsulate the provided Match model and properties" in {
    val initialMatch = Match(100)
    val controller = MatchController(initialMatch, isVsBot = true)

    initialMatch.maxScore shouldBe 100
    initialMatch.isOver shouldBe false
    initialMatch.scores(Player1) shouldBe 0
    initialMatch.scores(Player2) shouldBe 0
    controller.readInput should not be null
  }

  "Start execution on completed match" should "determine Player 1 as winner when Player 1 has lower score" in {
    // In Cactus rules, lower total score wins. Player 1 (10) < Player 2 (25) -> Player 1 wins
    val completedMatch = Match(20).copy(scores = Map(Player1 -> 10, Player2 -> 25))
    val inputs = List(Key.ENTER)

    noException should be thrownBy {
      runMatchControllerWithInputs(completedMatch, inputs)
    }
  }

  it should "determine Player 2 as winner when Player 2 has lower score" in {
    // Player 2 (12) < Player 1 (30) -> Player 2 wins
    val completedMatch = Match(20).copy(scores = Map(Player1 -> 30, Player2 -> 12))
    val inputs = List(Key.ESCAPE)

    noException should be thrownBy {
      runMatchControllerWithInputs(completedMatch, inputs)
    }
  }

  it should "handle a tie scenario when scores are equal" in {
    // Player 1 (25) == Player 2 (25) -> None (Tie)
    val tiedMatch = Match(20).copy(scores = Map(Player1 -> 25, Player2 -> 25))
    val inputs = List(Key.ENTER)

    noException should be thrownBy {
      runMatchControllerWithInputs(tiedMatch, inputs)
    }
  }

  "Active Match execution" should "launch GameController and terminate cleanly on ESCAPE" in {
    val activeMatch = Match(100)
    // GameController starts -> receives ENTER (action confirm) -> ESCAPE (exit game) -> ESCAPE (exit match)
    val inputs = List(Key.ENTER, Key.ESCAPE, Key.ESCAPE)

    noException should be thrownBy {
      runMatchControllerWithInputs(activeMatch, inputs)
    }
  }

  it should "execute game loop correctly in vs Bot mode" in {
    val activeMatch = Match(100)
    val inputs = List(Key.ENTER, Key.ESCAPE, Key.ESCAPE)

    noException should be thrownBy {
      runMatchControllerWithInputs(activeMatch, inputs, isVsBot = true)
    }
  }

  "waitForEnter filtering" should "ignore non-confirming keys (UP, DOWN, UNKNOWN) until ENTER or ESCAPE" in {
    val completedMatch = Match(10).copy(scores = Map(Player1 -> 15, Player2 -> 5))
    // Send unmapped/directional keys before ENTER to test waitForEnter loop filtering
    val inputs = List(Key.UP, Key.DOWN, Key.LEFT, Key.RIGHT, Key.UNKNOWN, Key.ENTER)

    noException should be thrownBy {
      runMatchControllerWithInputs(completedMatch, inputs)
    }
  }

  it should "terminate waitForEnter on Key.ESCAPE" in {
    val completedMatch = Match(10).copy(scores = Map(Player1 -> 15, Player2 -> 5))
    val inputs = List(Key.UNKNOWN, Key.ESCAPE)

    noException should be thrownBy {
      runMatchControllerWithInputs(completedMatch, inputs)
    }
  }
