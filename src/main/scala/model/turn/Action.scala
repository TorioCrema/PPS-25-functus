package org.pps.functus
package model.turn

enum Action:
  case Observe
  case Confirm
  case EndTurn
  case Draw
  case Activate
  case Cactus
  case DrawKing
  case ChooseDiscard(index: Int)
  case ChooseReplace(index: Int)
  case Discard

  def next: List[Action] = this match
    case Observe          => List(Confirm)
    case Confirm | Cactus => List(EndTurn)
    case Draw | DrawKing  => List(Activate)
    case ChooseReplace(_) => List(Cactus, EndTurn)
    case ChooseDiscard(_) => List(Discard)
    case Discard          => List(Draw)
    case _                => Nil
