package org.pps.functus
package model.turn

enum Action:
  /** Action used to observe cards on the player's field.
    */
  case Observe

  /** Action that is used to restore the board after [[Observe]].
    */
  case Confirm

  /** Ends the turn. */
  case EndTurn

  /** Draws from the deck to the player's hand. */
  case Draw

  /** Activates the card in the player's hand. */
  case Activate

  /** Calls Cactus */
  case Cactus

  /** Draws the king from the top of the discard pile. */
  case DrawKing

  /** Picks the card that will be drawn from the player's field and then discarded */
  case ChooseDiscard(index: Int)

  /** Discards the card from the player's hand, the index indicates where to place the card in case it wasn't the
    * correct value
    */
  case Discard(index: Int)

  /** Picks a card to replace with the card in the player's hand. */
  case ChooseReplace(index: Int)

  def next: List[Action] = this match
    case Observe              => List(Confirm)
    case Confirm | Cactus     => List(EndTurn)
    case Draw | DrawKing      => List(Activate)
    case ChooseReplace(_)     => List(Cactus, EndTurn)
    case ChooseDiscard(index) => List(Discard(index))
    case Discard(_)           => List(Draw)
    case _                    => Nil
