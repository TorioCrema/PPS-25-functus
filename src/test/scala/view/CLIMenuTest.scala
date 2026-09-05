package org.pps.functus
package view

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import view.utils.{MenuItem, ShowCaseOption, TargetScoreOption, SelectableMenuItem, Utils}
import org.scalatest.BeforeAndAfterEach

class CLIMenuTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach:

  override def beforeEach(): Unit =
    Utils.viewBuilder.clear()

  "CLIMenu" should "contain all MenuItem enum values in order" in {
    val menu = new CLIMenu()
    menu.menuItem should contain theSameElementsInOrderAs MenuItem.values.toList
  }

  "CLIMenu" should "contain all TargetScoreOption enum values in order" in {
    val menu = new CLIMenu()
    menu.scoreOption should contain theSameElementsInOrderAs TargetScoreOption.values.toList
  }

  "CLIMenu" should "contain all ShowCaseOption enum values in order" in {
    val menu = new CLIMenu()
    menu.showCaseOption should contain theSameElementsInOrderAs ShowCaseOption.values.toList
  }

  "SelectableMenuItem enum implementations" should "correctly implement the label property" in {
    MenuItem.values.foreach { item =>
      item shouldBe a [SelectableMenuItem]
      item.label should not be empty
    }
    TargetScoreOption.values.foreach { option =>
      option shouldBe a [SelectableMenuItem]
      option.label should not be empty
    }
    ShowCaseOption.values.foreach { option =>
      option shouldBe a [SelectableMenuItem]
      option.label should not be empty
    }
  }

  "CLIMenu.renderMainMenu" should "append formatted main menu output with the exit footer" in {
    val menu = new CLIMenu()

    noException should be thrownBy menu.renderMainMenu(selectedModeIndex = 0)

    val output = Utils.viewBuilder.toString()
    output should not be empty
    output should include("(Press 'Q' to Exit)")
  }

  it should "highlight the currently selected menu item with an arrow indicator" in {
    val menu = new CLIMenu()
    val selectedIndex = 1

    menu.renderMainMenu(selectedIndex)
    val output = Utils.viewBuilder.toString()

    val selectedItem = menu.menuItem(selectedIndex)
    val expectedSelectedLine = s" ->  ${selectedItem.label}"

    output should include(expectedSelectedLine)
  }

  it should "format non-selected menu items with indentation instead of an arrow" in {
    val menu = new CLIMenu()
    val selectedIndex = 0

    menu.renderMainMenu(selectedIndex)
    val output = Utils.viewBuilder.toString()

    menu.menuItem.zipWithIndex.foreach { case (item, index) =>
      if index != selectedIndex then
        val expectedUnselectedLine = s"    ${item.label}"
        output should include(expectedUnselectedLine)
    }
  }

  "CLIMenu.renderTargetScoreMenu" should "append formatted target score menu with the return footer" in {
    val menu = new CLIMenu()

    noException should be thrownBy menu.renderTargetScoreMenu(selectedScoreIndex = 0)

    val output = Utils.viewBuilder.toString()
    output should not be empty
    output should include("SELECT A TARGET SCORE FOR THE MATCH")
    output should include("(Press 'Q' to return to main menu)")
  }

  it should "highlight the active target score selection with an arrow indicator" in {
    val menu = new CLIMenu()
    val selectedIndex = 1

    menu.renderTargetScoreMenu(selectedIndex)
    val output = Utils.viewBuilder.toString()

    val selectedOption = menu.scoreOption(selectedIndex)
    output should include(s" ->  ${selectedOption.label}")
  }

  "CLIMenu.renderShowCaseMenu" should "append formatted showcase menu with the return footer" in {
    val menu = new CLIMenu()

    noException should be thrownBy menu.renderShowCaseMenu(selectedIndex = 0)

    val output = Utils.viewBuilder.toString()
    output should not be empty
    output should include("SELECT A SHOWCASE TO BE PLAYED")
    output should include("(Press 'Q' to return to main menu)")
  }

  it should "highlight the selected showcase option with an arrow indicator" in {
    val menu = new CLIMenu()
    val selectedIndex = 2

    menu.renderShowCaseMenu(selectedIndex)
    val output = Utils.viewBuilder.toString()

    val selectedOption = menu.showCaseOption(selectedIndex)
    output should include(s" ->  ${selectedOption.label}")
  }

  it should "format non-selected showcase options with indentation" in {
    val menu = new CLIMenu()
    val selectedIndex = 0

    menu.renderShowCaseMenu(selectedIndex)
    val output = Utils.viewBuilder.toString()

    menu.showCaseOption.zipWithIndex.foreach { case (option, index) =>
      if index != selectedIndex then
        output should include(s"    ${option.label}")
    }
  }

  "CLIMenu layout bounds" should "handle boundary indices smoothly across all menu types" in {
    val menu = new CLIMenu()

    noException should be thrownBy menu.renderMainMenu(0)
    noException should be thrownBy menu.renderMainMenu(menu.menuItem.length - 1)

    noException should be thrownBy menu.renderTargetScoreMenu(0)
    noException should be thrownBy menu.renderTargetScoreMenu(menu.scoreOption.length - 1)

    noException should be thrownBy menu.renderShowCaseMenu(0)
    noException should be thrownBy menu.renderShowCaseMenu(menu.showCaseOption.length - 1)
  }