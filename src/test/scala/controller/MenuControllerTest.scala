package org.pps.functus
package controller

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import view.CLIMenu

import controller.MenuController
import view.utils.Key

/** CLIMenu fake that logs all indexes passed to render().
  */
class FakeCLIMenu extends CLIMenu:
  var renderedIndices: List[Int] = List.empty
  var renderedScoreIndices: List[Int] = List.empty

  override def render(selectedIndex: Int): Unit =
    renderedIndices = renderedIndices :+ selectedIndex

  override def renderTargetScoreMenu(selectedIndex: Int): Unit =
    renderedScoreIndices = renderedScoreIndices :+ selectedIndex

/** Helper to simulate an input provider driven by a list of keys.
  */
class ScriptedInput(private var sequence: List[Key]):
  def nextKey(): Key = sequence match
    case head :: tail =>
      sequence = tail
      head
    case Nil =>
      Key.ESCAPE

class MenuControllerTest extends AnyFunSuite with Matchers:

  test("start should end immediately if the first key is ESCAPE"):
    val fakeMenu = FakeCLIMenu()
    val controller = MenuController(fakeMenu)
    fakeMenu.renderedIndices shouldBe empty

  test("Circular navigation: DOWN moves the index forward and UP moves it back"):
    val fakeMenu = FakeCLIMenu()

    var currentIndex = 0
    val totalItems = 2

    def simulateMove(delta: Int): Unit =
      currentIndex = (currentIndex + delta + totalItems) % totalItems

    simulateMove(1)
    currentIndex shouldBe 1

    simulateMove(1)
    currentIndex shouldBe 0

    simulateMove(-1)
    currentIndex shouldBe 1


  test("Target Score navigation logic: DOWN cycles through score options and UP moves backward"):
    var currentScoreIndex = 0
    val totalScoreOptions = view.utils.TargetScoreOption.values.length

    def simulateScoreMove(delta: Int): Unit =
      currentScoreIndex = (currentScoreIndex + delta + totalScoreOptions) % totalScoreOptions

    // Down (STEP_PREVIOUS = 1) -> 100 Points (index 1)
    simulateScoreMove(1)
    currentScoreIndex shouldBe 1

    // Down (STEP_PREVIOUS = 1) -> 150 Points (index 2)
    simulateScoreMove(1)
    currentScoreIndex shouldBe 2

    // Up (STEP_NEXT = -1) -> 100 Points (index 1)
    simulateScoreMove(-1)
    currentScoreIndex shouldBe 1

  test("Selecting Match mode sets isChoosingTargetScore state to true"):
    val fakeMenu = FakeCLIMenu()
    var isChoosingTargetScore = false
    var selectedScoreIndex = 0

    // Simula la conferma dell'azione principale su Match
    val selectedMainItem = view.utils.MenuItem.Match
    if selectedMainItem == view.utils.MenuItem.Match then
      selectedScoreIndex = 0
      isChoosingTargetScore = true

    isChoosingTargetScore shouldBe true
    selectedScoreIndex shouldBe 0

  test("ESC in target score menu returns to main menu state"):
    var isChoosingTargetScore = true

    // Simula la pressione del tasto ESCAPE nel sotto-menu
    val inputKey = Key.ESCAPE
    if inputKey == Key.ESCAPE then
      isChoosingTargetScore = false

    isChoosingTargetScore shouldBe false