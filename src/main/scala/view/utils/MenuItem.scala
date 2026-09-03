package org.pps.functus
package view.utils

enum MenuItem(val label: String):
  case SingleGame extends MenuItem("Play a single game ( 2 Player )")
  case SinglePlayerGame extends MenuItem("Play a single game ( vs IA )")
  case Match extends MenuItem("Play a Match with score limit ( 2 Player )")
  case SinglePlayerMatch extends MenuItem("Play a Match with score limit  ( vs IA )")

enum TargetScoreOption(val score: Int, val label: String):
  case Score50 extends TargetScoreOption(50, "50 Points")
  case Score100 extends TargetScoreOption(100, "100 Points")
  case Score150 extends TargetScoreOption(150, "150 Points")
  case Score200 extends TargetScoreOption(200, "200 Points")