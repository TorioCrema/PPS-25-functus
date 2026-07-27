package org.pps.functus
package model

import model.turn.Action.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ActionTest extends AnyFlatSpec with Matchers:
  "Observe action" should "have Confirm as next action" in:
    Observe.next should be(List(Confirm))

  "Confirm action" should "have EndTurn as next action" in:
    Confirm.next should be(List(EndTurn))

  "Draw action" should "have Activate as next actions" in:
    Draw.next should be(List(Activate))

  "Cactus action" should "have EndTurn as next action" in:
    Cactus.next should be(List(EndTurn))

  "EndTurn action" should "have no next actions" in:
    EndTurn.next should be(Nil)

  "DrawKing" should "have Activate as next action" in:
    DrawKing.next should be(List(Activate))

  "Discard" should "have Draw as next action" in:
    Discard.next should be(List(Draw))

  "ChooseDiscard" should "have Discard has next action" in:
    ChooseDiscard(0).next should be(List(Discard))

  "ChooseReplace" should "have Cactus and EndTurn as next actions" in:
    ChooseReplace(0).next should be(List(Cactus, EndTurn))
