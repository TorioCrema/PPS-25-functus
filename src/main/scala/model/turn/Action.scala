package org.pps.functus
package model.turn

enum Action:
  case Observe
  case Confirm
  case EndTurn
  case Draw
  case Replace
  case Activate
  case Cactus

  def next: List[Action] = this match
    case Observe            => List(Confirm)
    case Confirm | Cactus   => List(EndTurn)
    case Draw               => List(Replace, Activate)
    case Replace | Activate => List(Cactus, EndTurn)
    case _                  => Nil
