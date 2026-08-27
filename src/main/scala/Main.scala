package org.pps.functus

import view.CLIMenu
import controller.MenuController

import view.utils.Utils

object Main:
  def main(args: Array[String]): Unit =
    Utils.init()
    try
      MenuController(CLIMenu()).start()
    finally
      Utils.restore()
