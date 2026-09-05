package org.pps.functus
package controller

import view.CLIMenu
import view.utils.{Key, MenuItem, ShowCaseOption, TargetScoreOption, Utils}

import MenuItem.*
import MenuItem.Match as MenuMatch
import model.playable.game.{Game, Match}
import model.board.BoardFactory

import view.utils.ShowCaseOption.*

import scala.language.implicitConversions

class MenuController(private val menu: CLIMenu):

  private val STEP_NEXT = -1
  private val STEP_PREVIOUS = 1

  private trait Menu:
    def itemCount: Int
    def render(selectedIndex: Int): Unit
    def onConfirm(selectedIndex: Int): Unit
    def onBack(): Unit = currentMenu = MainMenu

  private object MainMenu extends Menu:
    private val menuItems: List[MenuItem] = MenuItem.values.toList
    override def itemCount: Int = menuItems.length
    override def render(selectedIndex: Int): Unit = menu.renderMainMenu(selectedIndex)

    override def onConfirm(selectedIndex: Int): Unit = menuItems(selectedIndex) match
      case SingleGame        => GameController(Game(BoardFactory.BoardWithPopulatedFields()), isVsBot = false).start()
      case MenuMatch         => openTargetScoreMenu(isVsBot = false)
      case SinglePlayerGame  => GameController(Game(BoardFactory.BoardWithPopulatedFields()), isVsBot = true).start()
      case SinglePlayerMatch => openTargetScoreMenu(isVsBot = true)
      case ShowCase          => openShowCaseMenu()
      case Rules             => ()

    override def onBack(): Unit = isExitChosen = true

  private class TargetScoreMenu(isVsBot: Boolean) extends Menu:
    private val scoreItems: List[TargetScoreOption] = TargetScoreOption.values.toList

    override def itemCount: Int = scoreItems.length

    override def render(selectedIndex: Int): Unit = menu.renderTargetScoreMenu(selectedIndex)

    override def onConfirm(selectedIndex: Int): Unit =
      val chosenTargetScore = scoreItems(selectedIndex).score
      currentMenu = MainMenu
      MatchController(Match(chosenTargetScore), isVsBot = isVsBot).start()

  private class ShowCaseMenu extends Menu:
    private val showCaseItems: List[ShowCaseOption] = ShowCaseOption.values.toList

    override def itemCount: Int = showCaseItems.length

    override def render(selectedIndex: Int): Unit = menu.renderShowCaseMenu(selectedIndex)

    override def onConfirm(selectedIndex: Int): Unit = showCaseItems(selectedIndex) match
      case DrawSix => ()
      case DrawSeven => ()
      case DrawEight => ()
      case DrawKing => ()


  private var currentMenu: Menu = MainMenu
  private var selectedIndex: Int = 0
  private var isExitChosen: Boolean = false

  private def openTargetScoreMenu(isVsBot: Boolean): Unit =
    selectedIndex = 0
    currentMenu = TargetScoreMenu(isVsBot)

  private def openShowCaseMenu(): Unit = {
    selectedIndex = 0
    currentMenu = ShowCaseMenu()
  }

  /** Starts the main execution loop for menu navigation and user interaction.
   *
   * Continuously renders the active [[MenuState]] and listens for keyboard input.
   * Directional key presses ([[view.utils.Key.UP]], [[view.utils.Key.DOWN]],
   * [[view.utils.Key.LEFT]], [[view.utils.Key.RIGHT]]) update option selection
   * cyclicly within the current menu bounds
   *
   * Action resolution is delegated to the active menu state:
   *  - [[view.utils.Key.ENTER]] confirms selection by triggering `currentMenu.onConfirm`.
   *  - [[view.utils.Key.ESCAPE]] handles backward navigation or exit via `currentMenu.onBack`.
   */
  def start(): Unit =
    while !isExitChosen do
      currentMenu.render(selectedIndex)
      Utils.readInput() match
        case Key.UP | Key.LEFT => moveSelection(delta = STEP_NEXT)
        case Key.DOWN | Key.RIGHT => moveSelection(delta = STEP_PREVIOUS)
        case Key.ENTER => currentMenu.onConfirm(selectedIndex)
        case Key.ESCAPE =>
          currentMenu.onBack()
          selectedIndex = 0
        case _ => ()

  private def moveSelection(delta: Int): Unit =
    val total = currentMenu.itemCount
    if total > 0 then
      selectedIndex = (selectedIndex + delta + total) % total