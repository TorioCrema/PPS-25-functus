package org.pps.functus
package view

import view.utils.Utils
import view.utils.Utils.{SEPARATOR_CHAR, terminalWidth, viewBuilder}

import scala.util.Try

class CLIMenu:
  val menuItem: List[MenuItem] = MenuItem.values.toList

  def render(selectedModeIndex: Int): Unit =
    given separator: String = SEPARATOR_CHAR * terminalWidth
    Utils.clearScreen()
    Utils.drawHeader
    printMenu(selectedModeIndex)
    print(viewBuilder)

  private def printMenu(using viewBuilder: StringBuilder, separator: String)(selectedModeIndex: Int) =
    // Menu / Action Zone
    val transitionBlock = StringBuilder()
    transitionBlock.append(s"$separator\n\n")
    transitionBlock.append(Utils.centerText(s"MAIN MENU \n"))
    transitionBlock.append(s"$separator\n\n")
    menuItem.zipWithIndex.foreach { case (item, i) =>
      val current = if i == selectedModeIndex then " -> " else "    "
      transitionBlock.append(Utils.centerText(s"$current${i + 1}. ${item.label}\n"))
    }
    transitionBlock.append(s"$separator\n\n")
    transitionBlock.append(s"\n (Press 'Q' to exit)\n")

    val blockString = transitionBlock.toString()
    val blockHeight = blockString.linesIterator.length
    val terminalHeight = Try(Utils.terminal.getRows).toOption.filter(_ > 0).getOrElse(24)
    val topPadding = Math.max(0, (terminalHeight - blockHeight) / 2) - 10
    viewBuilder.append("\n" * topPadding)
    viewBuilder.append(blockString)
