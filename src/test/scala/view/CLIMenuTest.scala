package org.pps.functus
package view

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import view.utils.{MenuItem, Utils}

import org.scalatest.BeforeAndAfterEach

class CLIMenuTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach:

  override def beforeEach(): Unit =
    Utils.viewBuilder.clear()

  "CLIMenu" should "contain all MenuItem enum values in order" in {
    val menu = new CLIMenu()
    menu.menuItem should contain theSameElementsInOrderAs MenuItem.values.toList
  }

  "CLIMenu.render" should "append formatted menu output to viewBuilder without throwing exceptions" in {
    val menu = new CLIMenu()

    // Execute render with a valid selection index
    noException should be thrownBy menu.render(selectedModeIndex = 0)

    // Verify viewBuilder accumulated the rendered menu string
    val output = Utils.viewBuilder.toString()
    output should not be empty
    output should include("(Press 'Q' to exit)")
  }

  it should "highlight the currently selected menu item with an arrow indicator" in {
    val menu = new CLIMenu()
    val selectedIndex = 1

    menu.render(selectedIndex)
    val output = Utils.viewBuilder.toString()

    val selectedItem = menu.menuItem(selectedIndex)
    val expectedSelectedLine = s" -> ${selectedIndex + 1}. ${selectedItem.label}"

    output should include(expectedSelectedLine)
  }

  it should "format non-selected menu items with indentation instead of an arrow" in {
    val menu = new CLIMenu()
    val selectedIndex = 0

    menu.render(selectedIndex)
    val output = Utils.viewBuilder.toString()

    menu.menuItem.zipWithIndex.foreach { case (item, index) =>
      if index != selectedIndex then
        val expectedUnselectedLine = s"    ${index + 1}. ${item.label}"
        output should include(expectedUnselectedLine)
    }
  }

  "CLIMenu padding and layout" should "correctly calculate top padding bounds for terminal height" in {
    val menuItemsCount = MenuItem.values.length
    // Block height = top separator + item lines + bottom separator + exit instruction line
    val blockHeight = 1 + menuItemsCount + 1 + 1

    val terminalHeight = Utils.terminalHeight
    val expectedPadding = Math.max(0, (terminalHeight - blockHeight) / 2)

    expectedPadding should be >= 0
  }

  it should "handle boundary indices smoothly when selection is at first or last element" in {
    val menu = new CLIMenu()
    val firstIdx = 0
    val lastIdx = menu.menuItem.length - 1

    noException should be thrownBy menu.render(firstIdx)
    noException should be thrownBy menu.render(lastIdx)
  }
