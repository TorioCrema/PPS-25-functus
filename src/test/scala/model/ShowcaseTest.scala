package org.pps.functus
package model

import model.showcase.*
import model.board.Player.*
import model.playable.turn.Action.*
import model.playable.turn.Action
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ShowcaseTest extends AnyFlatSpec with Matchers:

  def simpleEffectTest(showcase: Showcase, fieldLength: Int, action: Int => Action): Unit =
    val activated = showcase.turn.actAll(Draw :: Activate :: Nil)
    val expectedActions = for i <- 0 until fieldLength yield action(i)
    for expected <- expectedActions do activated.actions.contains(expected) should be(true)

  "SixShowcase" should "allow to observe any Player2 card" in:
    simpleEffectTest(SixShowcase, SixShowcase.turn.board.players(Player2).length, ObserveOpponent(_))

  "SevenShowcase" should "allow to observe any Player1 card" in:
    simpleEffectTest(SevenShowcase, SevenShowcase.turn.board.players(Player1).length, ObservePlayer(_))

  "JackShowcase" should "allow to swap any Player1 card with any Player2 card" in:
    val turn = JackShowcase.turn.actAll(Draw :: Activate :: Nil)
    val expectedActions =
      for
        playerIndex <- 0 until turn.board.players(Player1).length
        opponentIndex <- 0 until turn.board.players(Player2).length
      yield Swap(playerIndex, opponentIndex)
    for expected <- expectedActions do turn.actions.contains(expected) should be(true)

  "KingDrawShowcase" should "allow to draw the king" in:
    KingDrawShowcase.turn.actions.contains(DrawKing) should be(true)

  "SuccesfulDiscardShowcase" should "allow to successfully discard the first Player1 card" in:
    val turn = SuccessfulDiscardShowcase.turn
    turn.board.players(Player1).getCard(0)._1.value == turn.board.discardPile.head.value should be(true)
    turn.actions.contains(ChooseDiscard(0)) should be(true)

  "FailedDiscardShowcase" should "not allow to successfully discard any Player1 card" in:
    val turn = FailedDiscardShowcase.turn
    turn.board.players(Player1).cardsList.forall(_.value != turn.board.discardPile.head.value) should be(true)
    val expectedActions = (0 until turn.board.players(Player1).length).map(ChooseDiscard(_))
    for expected <- expectedActions do turn.actions.contains(expected) should be(true)