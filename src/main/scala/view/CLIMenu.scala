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
  private val RULES_TITLE = "GAME RULES"

  /** Renders the main menu displaying the available game modes.
    *
    * Clears the terminal screen and draws the header before printing the list of [[view.utils.MenuItem]] options.
    *
    * @param selectedModeIndex
    *   the index of the mode currently highlighted by the user
    */
  def renderMainMenu(selectedModeIndex: Int): Unit =
    printMenu(selectedModeIndex, MAIN_MENU_TITLE, menuItem, true)

  /** Renders the target score selection menu for match mode.
    *
    * Clears the terminal screen and draws the header before printing the list of [[view.utils.TargetScoreOption]]
    * options.
    *
    * @param selectedScoreIndex
    *   the index of the target score option currently highlighted
    */
  def renderTargetScoreMenu(selectedScoreIndex: Int): Unit =
    printMenu(selectedScoreIndex, MATCH_SCORE_MENU_TITLE, scoreOption)

  /** Renders the showcase selection menu for testing card interactions.
    *
    * Clears the terminal screen and draws the header before printing the list of [[view.utils.ShowCaseOption]] options.
    *
    * @param selectedIndex
    *   the index of the showcase option currently highlighted
    */
  def renderShowCaseMenu(selectedIndex: Int): Unit =
    printMenu(selectedIndex, SHOWCASE_MENU_TITLE, showCaseOption)

  /** Renders the game rules page with instructions and controls.
    *
    * Clears the terminal screen and draws the header before appending the formatted rules text block.
    */
  def renderRules(): Unit =
    Utils.clearScreen()
    Utils.drawHeader

    val transitionBlock = StringBuilder()
    transitionBlock.append(s"$separator\n\n")
    transitionBlock.append(Utils.centerText(s"$RULES_TITLE\n"))
    transitionBlock.append(s"$separator\n\n")

    val rulesText =
      """  * Objective: Have the lowest cumulative score. Exceeding the match target score loses the game.
        |  * Setup: Each player receives 4 face-down cards and can peek at 2 of them on Turn 1.
        |  * Turn Actions:
        |      - Draw a card from the deck and replace one of your field cards, discarding the replaced card face-up.
        |      - Alternatively, discard specific drawn cards to activate special effects:
        |          * 6: Peek at 1 of your opponent's cards.
        |          * 7: Peek at 1 of your own cards.
        |          * 8: Blindly swap 1 of your cards with 1 of your opponent's cards.
        |  * Discard Matching (Optional):
        |      - Discard a card matching the top discard card value. If incorrect, keep the card and draw 1 penalty card.
        |  * Ending a Game:
        |      - Call 'Cactus' at turn end to trigger the final turn for your opponent.
        |      - Reveal all cards and sum their values (Ace = 1, Numbers = 2-9, King = 0).
        |  * Target Score Rule: Hitting the exact match target score cuts your total score in half!""".stripMargin

    transitionBlock.append(s"$rulesText\n\n")
    transitionBlock.append(s"$separator\n\n")
    transitionBlock.append(s" (Press 'Q' to return to main menu)\n")

    Utils.viewBuilder.append(transitionBlock)
    print(Utils.viewBuilder)

  private def printMenu(using
      viewBuilder: StringBuilder,
      separator: String
  )(selectedIndex: Int, title: String, menuItem: List[SelectableMenuItem], isMainMenu: Boolean = false): Unit =
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
    transitionBlock.append(if isMainMenu then s"\n (Press 'Q' to Exit)\n"
    else s"\n (Press 'Q' to return to main menu)\n")

    Utils.renderCenteredBlock(transitionBlock.toString())
    print(viewBuilder)
