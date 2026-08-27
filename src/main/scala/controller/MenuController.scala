package org.pps.functus
package controller

import view.{CLIMenu, CLIView, Key, MenuItem}
import view.utils.Utils
import view.MenuItem.{Match, SingleGame}

class MenuController(private val menu: CLIMenu):

  private val TO_BE_SELECTED = -1
  private val STEP_NEXT = -1
  private val STEP_PREVIOUS = 1
  private val menuItem: List[MenuItem] = MenuItem.values.toList
  private var selectedModeIndex = 0

  def start(): Unit =
    var isModeChosen = false
    while !isModeChosen do
      menu.render(selectedModeIndex)
      Utils.readInput() match
        case Key.UP | Key.LEFT    => moveSelection(delta = STEP_NEXT)
        case Key.DOWN | Key.RIGHT => moveSelection(delta = STEP_PREVIOUS)
        case Key.ENTER            => confirmAction()
        case Key.ESCAPE           => isModeChosen = true
        case _                    => ()

  private def moveSelection(delta: Int): Unit =
    val total = menuItem.length
    if total > 0 then
      val newIndex = (selectedModeIndex + delta + total) % total
      selectedModeIndex = newIndex

  private def confirmAction(): Unit = menuItem(selectedModeIndex) match
    case SingleGame =>
      GameController(CLIView()).start()
    case Match => ()
