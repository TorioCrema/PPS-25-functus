package org.pps.functus
package model.playable.turn

import model.deck.card.Card
import model.board.Player
import Action.{ChooseReplace, ObserveOpponent, ObservePlayer, Swap}
import model.deck.sugar.CardDSL.*

object Effects:
  extension (card: Card)
    /** Activates the card's effect on the given [[Turn]], returns the resulting [[Turn]].
      * @param on
      *   the [[Turn]] to activate the effect on
      * @return
      *   the resulting [[Turn]]
      */
    def effect(on: Turn): List[Action] =
      val getFieldLength: Player => Int = on.board.getField(_).length
      val replaceActions = (for i <- 0 until getFieldLength(on.player) yield ChooseReplace(i)).toList

      def actionsFromFieldLength(fieldLength: Int)(action: Int => Action) =
        replaceActions.appendedAll(for i <- 0 until fieldLength yield action(i))

      card.value match
        case `six`   => actionsFromFieldLength(getFieldLength(on.player.other))(ObserveOpponent(_))
        case `seven` => actionsFromFieldLength(getFieldLength(on.player))(ObservePlayer(_))
        case `jack`  =>
          val swapActions =
            for
              playerIndex <- 0 until getFieldLength(on.player)
              opponentIndex <- 0 until getFieldLength(on.player)
            yield Swap(playerIndex, opponentIndex)
          replaceActions.appendedAll(swapActions)
        case _ => replaceActions
