package org.pps.functus
package view

import view.utils.Utils
import view.utils.Utils.{length, separator, viewBuilder}

class CLIMenu:
  val menuItem: List[MenuItem] = MenuItem.values.toList

  def render(selectedModeIndex: Int): Unit =
    Utils.clearScreen()
    Utils.drawHeader
    printMenu(selectedModeIndex)
    print(viewBuilder)

  private def printMenu(using viewBuilder: StringBuilder, separator: String, length: Int)(selectedModeIndex: Int) =
    // Menu / Action Zone
    val transitionBlock = StringBuilder()
    transitionBlock.append(s"$separator\n")
    menuItem.zipWithIndex.foreach { case (item, i) =>
      val current = if i == selectedModeIndex then " -> " else "    "
      transitionBlock.append(Utils.centerText(s"$current${i + 1}. ${item.label}\n", length))
    }
    transitionBlock.append(s"$separator\n")
    transitionBlock.append(s"\n (Press 'Q' to exit)\n")

    val blockString = transitionBlock.toString()
    val blockHeight = blockString.linesIterator.length
    val terminalHeight = Option(Utils.terminal.getRows).filter(_ > 0).getOrElse(24)
    val topPadding = Math.max(0, (terminalHeight - blockHeight) / 2) - 10
    viewBuilder.append("\n" * topPadding)
    viewBuilder.append(blockString)
