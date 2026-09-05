package org.pps.functus
package view

import view.utils.{MenuItem, SelectableMenuItem, ShowCaseOption, TargetScoreOption, Utils}
import view.utils.Utils.{viewBuilder, separator}


class CLIMenu:
  val menuItem: List[MenuItem] = MenuItem.values.toList
  val scoreOption: List[TargetScoreOption] = TargetScoreOption.values.toList
  val showCaseOption: List[ShowCaseOption] = ShowCaseOption.values.toList

  private val MAIN_MENU_TITLE = "MAIN MENU"
  private val MATCH_SCORE_MENU_TITLE = "SELECT A TARGET SCORE FOR THE MATCH"
  private val SHOWCASE_MENU_TITLE = "SELECT A SHOWCASE TO BE PLAYED"

    /** Renders the main menu displaying the available game modes.
     *
     * Clears the terminal screen and draws the header before printing
     * the list of [[view.utils.MenuItem]] options.
     *
     * @param selectedModeIndex the index of the mode currently highlighted by the user
     */
  def renderMainMenu(selectedModeIndex: Int): Unit =
    printMenu(selectedModeIndex, MAIN_MENU_TITLE, menuItem, true)

  /** Renders the target score selection menu for match mode.
   *
   * Clears the terminal screen and draws the header before printing
   * the list of [[view.utils.TargetScoreOption]] options.
   *
   * @param selectedScoreIndex the index of the target score option currently highlighted
   */
  def renderTargetScoreMenu(selectedScoreIndex: Int): Unit =
    printMenu(selectedScoreIndex, MATCH_SCORE_MENU_TITLE, scoreOption)

  /** Renders the showcase selection menu for testing card interactions.
   *
   * Clears the terminal screen and draws the header before printing 
   * the list of [[view.utils.ShowCaseOption]] options.
   *
   * @param selectedIndex the index of the showcase option currently highlighted
   */
  def renderShowCaseMenu(selectedIndex: Int): Unit =
    printMenu(selectedIndex, SHOWCASE_MENU_TITLE, showCaseOption)

  private def printMenu(using viewBuilder: StringBuilder, separator: String)(selectedIndex: Int, title: String, menuItem: List[SelectableMenuItem],  isMainMenu: Boolean = false): Unit =
    Utils.clearScreen()
    Utils.drawHeader
    val transitionBlock = StringBuilder()
    transitionBlock.append(s"$separator\n\n")
    transitionBlock.append(Utils.centerText(s"$title \n"))
    transitionBlock.append(s"$separator\n\n")
    menuItem.zipWithIndex.foreach { case (item, i) =>
      val current = if i == selectedIndex then " -> " else "    "
      transitionBlock.append(Utils.centerText(s"$current ${item.label}\n"))
    }
    transitionBlock.append(s"$separator\n\n")
    transitionBlock.append(if isMainMenu then s"\n (Press 'Q' to Exit)\n" else s"\n (Press 'Q' to return to main menu)\n")

    Utils.renderCenteredBlock(transitionBlock.toString())
    print(viewBuilder)