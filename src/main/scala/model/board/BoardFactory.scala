package org.pps.functus
package model.board

import model.deck.DeckFactory
import model.field.FieldImpl

object BoardFactory:
  def apply(): Board =
    BoardImpl(
      deck = DeckFactory(),
      players = Player.values.map(p => p -> FieldImpl()).toMap
    )
