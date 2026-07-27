package org.pps.functus
package model.turn

import model.deck.card.Card

enum Action:
  case Observe
  case Confirm
  case EndTurn
  case Draw
  case Activate(input: Option[Card])
  case Cactus
  case DrawKing
  case Discard(input: Option[Int])

  def next: List[Action] = this match
    case Observe          => List(Confirm)
    case Confirm | Cactus => List(EndTurn)
    case Draw | DrawKing  => List(Activate(Option.empty))
    case Activate(_)      => List(Cactus, EndTurn)
    case Discard(_)       => List(Draw)
    case _                => Nil
