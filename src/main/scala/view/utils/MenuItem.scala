package org.pps.functus
package view.utils

trait SelectableMenuItem:
  def label: String

enum MenuItem(val label: String) extends SelectableMenuItem:
  case SingleGame extends MenuItem("Play a single game ( 2 Player )")
  case SinglePlayerGame extends MenuItem("Play a single game ( vs IA )")
  case Match extends MenuItem("Play a Match with score limit ( 2 Player )")
  case SinglePlayerMatch extends MenuItem("Play a Match with score limit  ( vs IA )")
  case ShowCase extends MenuItem("Test card's effect")
  case Rules extends MenuItem("Rules of the game")

enum TargetScoreOption(val score: Int, val label: String) extends SelectableMenuItem:
  case Score50 extends TargetScoreOption(50, "50 Points")
  case Score100 extends TargetScoreOption(100, "100 Points")
  case Score150 extends TargetScoreOption(150, "150 Points")
  case Score200 extends TargetScoreOption(200, "200 Points")
  
enum ShowCaseOption(val label: String) extends SelectableMenuItem:
  case DrawSix extends ShowCaseOption("Draw a six")
  case DrawSeven extends ShowCaseOption("Draw a seven")
  case DrawEight extends ShowCaseOption("Draw a eight")
  case DrawKing extends ShowCaseOption("King in discard pile")