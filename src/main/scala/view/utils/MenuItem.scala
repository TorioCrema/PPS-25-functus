package org.pps.functus
package view.utils

enum MenuItem(val label: String):
  case SingleGame extends MenuItem("Play a single game")
  case Match extends MenuItem("Play a Match with score limit")
