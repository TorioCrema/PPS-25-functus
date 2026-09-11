package org.pps.functus

import view.CLIMenu
import controller.MenuController

import org.pps.functus.utils.Utils

object Main:
  def main(args: Array[String]): Unit =
    Utils.init()
    try
      MenuController(CLIMenu()).start()
    finally
      Utils.restore()
