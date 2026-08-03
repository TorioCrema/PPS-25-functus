package org.pps.functus
package model

import model.turn.Action.*

import org.pps.functus.model.deck.DeckFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ActionTest extends AnyFlatSpec with Matchers:
  "Observe action" should "have Confirm as next action" in:
    Observe.next should be(Confirm :: Nil)

  "Confirm action" should "have EndTurn as next action" in:
    Confirm.next should be(EndTurn :: Nil)

  "Draw action" should "have Activate as next actions" in:
    Draw.next should be(Activate :: Nil)

  "Cactus action" should "have EndTurn as next action" in:
    Cactus.next should be(EndTurn :: Nil)

  "EndTurn action" should "have no next actions" in:
    EndTurn.next should be(Nil)

  "DrawKing" should "have Activate as next action" in:
    DrawKing.next should be(Activate :: Nil)

  "ChooseDiscard" should "have Discard has next action" in:
    for i <- 0 to DeckFactory().cards.length do ChooseDiscard(i).next should be(Discard(i) :: Nil)

  "Discard" should "have Draw as next action" in:
    for i <- 0 to DeckFactory().cards.length do Discard(i).next should be(Draw :: Nil)

  "ChooseReplace" should "have Cactus and EndTurn as next actions" in:
    for i <- 0 to DeckFactory().cards.length do ChooseReplace(i).next should be(Cactus :: EndTurn :: Nil)
