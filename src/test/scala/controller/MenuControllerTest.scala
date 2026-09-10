package org.pps.functus
package controller

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import view.CLIMenu
import view.utils.Key

import java.io.{OutputStream, PrintStream}

class MenuControllerTest extends AnyFlatSpec with Matchers:

  class TestCLIMenu extends CLIMenu:
    var lastMainMenuIndex: Option[Int] = None
    var lastTargetScoreIndex: Option[Int] = None
    var lastShowCaseIndex: Option[Int] = None
    var rulesRenderedCount: Int = 0

    override def renderMainMenu(selectedIndex: Int): Unit =
      lastMainMenuIndex = Some(selectedIndex)

    override def renderTargetScoreMenu(selectedIndex: Int): Unit =
      lastTargetScoreIndex = Some(selectedIndex)

    override def renderShowCaseMenu(selectedIndex: Int): Unit =
      lastShowCaseIndex = Some(selectedIndex)

    override def renderRules(): Unit =
      rulesRenderedCount += 1

  def runControllerWithInputs(inputs: List[Key]): TestCLIMenu =
    val originalOut = System.out
    val silentStream = new PrintStream(OutputStream.nullOutputStream())

    var inputQueue = inputs
    var maxCycles = 100
    val mockMenu = new TestCLIMenu()

    val scriptReader = () =>
      maxCycles -= 1
      if maxCycles <= 0 then Key.ESCAPE
      else if inputQueue.nonEmpty then
        val k = inputQueue.head
        inputQueue = inputQueue.tail
        k
      else Key.ESCAPE

    val controller = MenuController(mockMenu, scriptReader)

    try
      System.setOut(silentStream)
      Console.withOut(silentStream) {
        controller.start()
      }
    catch case _: Throwable => ()
    finally System.setOut(originalOut)

    mockMenu

  "MenuController input loop" should "navigate options and exit on ESCAPE" in {
    val inputs = List(Key.DOWN, Key.UP, Key.LEFT, Key.RIGHT, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastMainMenuIndex.isDefined shouldBe true
  }

  "Directional Navigation" should "handle circular selection with UP, DOWN, LEFT, RIGHT" in {
    val inputs = List(Key.DOWN, Key.DOWN, Key.UP, Key.RIGHT, Key.LEFT, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastMainMenuIndex shouldBe Some(1)
  }

  it should "wrap around correctly when moving past upper bounds with UP" in {
    val inputs = List(Key.UP, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastMainMenuIndex shouldBe Some(5)
  }

  it should "wrap around correctly when moving past lower bounds with DOWN" in {
    val inputs = List(Key.DOWN, Key.DOWN, Key.DOWN, Key.DOWN, Key.DOWN, Key.DOWN, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastMainMenuIndex shouldBe Some(0)
  }

  "Unmapped Key Handling" should "ignore unknown keys and preserve current selection" in {
    val inputs = List(Key.UNKNOWN, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastMainMenuIndex shouldBe Some(0)
  }

  "MainMenu Confirmations" should "launch GameController for SingleGame mode" in {
    val inputs = List(Key.ENTER, Key.ESCAPE, Key.ESCAPE, Key.ESCAPE)
    noException should be thrownBy {
      runControllerWithInputs(inputs)
    }

  }

  it should "launch GameController in vs Bot mode for SinglePlayerGame" in {
    val inputs = List(Key.DOWN, Key.ENTER, Key.ESCAPE)
    noException should be thrownBy {
      runControllerWithInputs(inputs)
    }
  }

  it should "open TargetScoreMenu for PvP Match mode" in {
    val inputs = List(Key.DOWN, Key.DOWN, Key.ENTER, Key.ESCAPE, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastTargetScoreIndex shouldBe Some(0)
  }

  it should "open TargetScoreMenu for PvC SinglePlayerMatch mode" in {
    val inputs = List(Key.DOWN, Key.DOWN, Key.DOWN, Key.ENTER, Key.ESCAPE, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastTargetScoreIndex shouldBe Some(0)
  }

  "TargetScoreMenu navigation and confirmation" should "select a target score option and start MatchController for PvP" in {
    val inputs = List(Key.DOWN, Key.DOWN, Key.ENTER, Key.DOWN, Key.ENTER, Key.ESCAPE, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastTargetScoreIndex shouldBe Some(1)
  }

  it should "select a target score option and start MatchController for PvC" in {
    val inputs = List(Key.DOWN, Key.DOWN, Key.DOWN, Key.ENTER, Key.DOWN, Key.ENTER, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastTargetScoreIndex shouldBe Some(1)
  }

  it should "return to MainMenu on ESCAPE" in {
    val inputs = List(Key.DOWN, Key.DOWN, Key.ENTER, Key.ESCAPE, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastTargetScoreIndex shouldBe Some(0)
    mockMenu.lastMainMenuIndex shouldBe Some(0)
  }

  "ShowCaseMenu navigation and options" should "open ShowCaseMenu and render options" in {
    val inputs = List(Key.UP, Key.UP, Key.ENTER, Key.ESCAPE, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastShowCaseIndex shouldBe Some(0)
  }

  it should "launch GameController with DrawSix showcase turn" in {
    val inputs = List(Key.UP, Key.UP, Key.ENTER, Key.ENTER, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastShowCaseIndex shouldBe Some(0)
  }

  it should "launch GameController with DrawSeven showcase turn" in {
    val inputs = List(Key.UP, Key.UP, Key.ENTER, Key.DOWN, Key.ENTER, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastShowCaseIndex shouldBe Some(1)
  }

  it should "launch GameController with DrawEight showcase turn" in {
    val inputs = List(Key.UP, Key.UP, Key.ENTER, Key.DOWN, Key.DOWN, Key.ENTER, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastShowCaseIndex shouldBe Some(2)
  }

  it should "launch GameController with DrawKing showcase turn" in {
    val inputs = List(Key.UP, Key.UP, Key.ENTER, Key.DOWN, Key.DOWN, Key.DOWN, Key.ENTER, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastShowCaseIndex shouldBe Some(3)
  }

  it should "launch GameController with SuccessfulDiscard showcase turn" in {
    val inputs = List(Key.UP, Key.UP, Key.ENTER, Key.UP, Key.UP, Key.ENTER, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastShowCaseIndex shouldBe Some(4)
  }

  it should "launch GameController with FailDiscard showcase turn" in {
    val inputs = List(Key.UP, Key.UP, Key.ENTER, Key.UP, Key.ENTER, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.lastShowCaseIndex shouldBe Some(5)
  }

  "RulePage lifecycle" should "render rules page and return to MainMenu on ESCAPE" in {
    val inputs = List(Key.UP, Key.ENTER, Key.ESCAPE, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.rulesRenderedCount shouldBe >(0)
    mockMenu.lastMainMenuIndex shouldBe Some(0)
  }

  "RulePage lifecycle" should "render rules page and do nothing on ENTER" in {
    val inputs = List(Key.UP, Key.ENTER, Key.ENTER, Key.ESCAPE, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.rulesRenderedCount shouldBe <(3)
    mockMenu.lastMainMenuIndex shouldBe Some(0)
  }

  it should "safely handle directional navigation when itemCount is 0" in {
    val inputs = List(Key.UP, Key.ENTER, Key.DOWN, Key.UP, Key.LEFT, Key.RIGHT, Key.ESCAPE, Key.ESCAPE)
    val mockMenu = runControllerWithInputs(inputs)

    mockMenu.rulesRenderedCount shouldBe >(0)
  }
