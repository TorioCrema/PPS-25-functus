package org.pps.functus

import view.CLIView
import controller.GameController

object Main:
  def main(args: Array[String]): Unit =
    val view = CLIView()
    val controller = GameController(view)
    controller.start()

