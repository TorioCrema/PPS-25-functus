package org.pps.functus
package controller

import view.CLIMenu
import view.utils.{Key, MenuItem, TargetScoreOption, Utils}

import MenuItem.{Match as MenuMatch, SingleGame}
import model.deck.sugar.BoardDSL.{board, default}
import model.playable.game.{Game, Match}
import scala.language.implicitConversions

class MenuController(private val menu: CLIMenu):

  private val TO_BE_SELECTED = -1
  private val STEP_NEXT = -1
  private val STEP_PREVIOUS = 1
  private val menuItems: List[MenuItem] = MenuItem.values.toList
  private val targetScores: List[TargetScoreOption] = TargetScoreOption.values.toList
  private var selectedModeIndex = 0
  private var selectedScoreIndex = 0
  private var isChoosingTargetScore = false

  def start(): Unit =
    var isExitChosen = false
    while !isExitChosen do
      if isChoosingTargetScore then
        menu.renderTargetScoreMenu(selectedScoreIndex)
        Utils.readInput() match
          case Key.UP | Key.LEFT    => moveScoreSelection(delta = STEP_NEXT)
          case Key.DOWN | Key.RIGHT => moveScoreSelection(delta = STEP_PREVIOUS)
          case Key.ENTER            => confirmScoreSelection()
          case Key.ESCAPE           => isChoosingTargetScore = false
          case _                    => ()
      else
        menu.render(selectedModeIndex)
        Utils.readInput() match
          case Key.UP | Key.LEFT    => moveMainSelection(delta = STEP_NEXT)
          case Key.DOWN | Key.RIGHT => moveMainSelection(delta = STEP_PREVIOUS)
          case Key.ENTER            => confirmMainAction()
          case Key.ESCAPE           => isExitChosen = true
          case _                    => ()

  private def moveMainSelection(delta: Int): Unit =
    val total = menuItems.length
    if total > 0 then selectedModeIndex = (selectedModeIndex + delta + total) % total

  private def moveScoreSelection(delta: Int): Unit =
    val total = targetScores.length
    if total > 0 then selectedScoreIndex = (selectedScoreIndex + delta + total) % total

  private def confirmMainAction(): Unit = menuItems(selectedModeIndex) match
    case SingleGame =>
      GameController(Game(board from default)).start()
    case MenuMatch =>
      selectedScoreIndex = 0
      isChoosingTargetScore = true

  private def confirmScoreSelection(): Unit =
    val chosenTargetScore = targetScores(selectedScoreIndex).score
    isChoosingTargetScore = false
    MatchController(Match(chosenTargetScore)).start()
