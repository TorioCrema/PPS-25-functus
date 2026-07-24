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

  def next: List[Action] = this match
    case Observe          => List(Confirm)
    case Confirm | Cactus => List(EndTurn)
    case Draw             => List(Activate(Option.empty))
    case Activate(_)      => List(Cactus, EndTurn)
    case _                => Nil
