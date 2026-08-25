package org.pps.functus
package model

import model.deck.DeckFactory
import model.playable.turn.Action.*
import model.playable.turn.Action
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ActionTest extends AnyFlatSpec with Matchers:
  private def testForAllIndices(toTest: Int => Action)(expected: Int => List[Action]): Unit =
    for i <- DeckFactory().cards.indices
    do toTest(i).next should be(expected(i))

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
    testForAllIndices(ChooseDiscard(_))(Discard(_) :: Nil)

  "Discard" should "have Draw as next action" in:
    testForAllIndices(Discard(_))(_ => Draw :: Nil)

  "ChooseReplace" should "have Cactus and EndTurn as next actions" in:
    testForAllIndices(ChooseReplace(_))(_ => Cactus :: EndTurn :: Nil)

  "ObserveOpponent" should "have GiveBack as next action" in:
    testForAllIndices(ObserveOpponent(_))(GiveBack(_) :: Nil)

  "GiveBack" should "have Cactus and EndTurn as next actions" in:
    testForAllIndices(GiveBack(_))(_ => Cactus :: EndTurn :: Nil)

  "ObservePlayer" should "have ReturnToField as next action" in:
    testForAllIndices(ObservePlayer(_))(ReturnToField(_) :: Nil)

  "ReturnToField" should "have Cactus and EndTurn as next actions" in:
    testForAllIndices(ReturnToField(_))(_ => Cactus :: EndTurn :: Nil)
    
  "Swap" should "have Cactus and EndTurn as next actions" in:
    Swap(0, 0).next should be(Cactus :: EndTurn :: Nil)
