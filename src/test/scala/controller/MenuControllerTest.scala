package org.pps.functus
package controller

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import view.{CLIMenu, Key}

import controller.MenuController

/** CLIMenu fake that logs all indexes passed to render().
  */
class FakeCLIMenu extends CLIMenu:
  var renderedIndices: List[Int] = List.empty

  override def render(selectedIndex: Int): Unit =
    renderedIndices = renderedIndices :+ selectedIndex

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
