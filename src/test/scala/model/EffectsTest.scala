package org.pps.functus
package model

import model.deck.sugar.CardDSL.{*, given}
import model.deck.card.Suit.*
import model.deck.sugar.FieldDSL.{*, given}
import model.board.*

import model.board.Player.*
import model.deck.DeckImpl
import model.turn.Action.*
import model.turn.Turns.SimpleTurn
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EffectsTest extends AnyFlatSpec with Matchers:
  private val threeOfCups = three of Cups
  private val twoOfSwords = two of Swords
  private val fiveOfWands = five of Wands
  private val aceOfPentacles = ace of Pentacles
  private val player1Field = threeOfCups and twoOfSwords
  private val player2Field = fiveOfWands and aceOfPentacles
  private val boardWithDrawableSix =
    BoardImpl(deck = DeckImpl(Vector(six of Swords)), players = Map((Player1, player1Field), (Player2, player2Field)))
  private val turnWithSixInHand = SimpleTurn(boardWithDrawableSix, Player1).act(Draw)
  private val boardWithDrawableSeven =
    BoardImpl(deck = DeckImpl(Vector(seven of Swords)), players = Map((Player1, player1Field), (Player2, player2Field)))
  private val turnWithSevenInHand = SimpleTurn(boardWithDrawableSeven, Player1).act(Draw)
  private val boardWithDrawableJack =
    BoardImpl(deck = DeckImpl(Vector(jack of Swords)), players = Map((Player1, player1Field), (Player2, player2Field)))
  private val turnWithJackInHand = SimpleTurn(boardWithDrawableJack, Player1).act(Draw)

  "Activating a six" should "discard it and allow to observe one of the opponent's cards" in:
    val afterActivatingSix = turnWithSixInHand.act(Activate)
    afterActivatingSix.hand should be(Nil)
    afterActivatingSix.board.getTopDiscardStack should be(six of Swords)
    afterActivatingSix.actions should be(
      ChooseReplace(0) :: ChooseReplace(1) :: ObserveOpponent(0) :: ObserveOpponent(1) :: Nil
    )

  "Observing an opponent's card" should "draw it to the player's hand" in:
    val afterObservingOpponent = turnWithSixInHand.act(Activate).act(ObserveOpponent(0))
    afterObservingOpponent.hand should be(fiveOfWands :: Nil)

  it should "have GiveBack as the next action" in:
    val afterObservingOpponent = turnWithSixInHand.act(Activate).act(ObserveOpponent(0))
    afterObservingOpponent.actions should be(GiveBack(0) :: Nil)

  "Giving back the observed card" should "return it to its original place" in:
    val afterReturningObservedCard = turnWithSixInHand.act(Activate).act(ObserveOpponent(0)).act(GiveBack(0))
    afterReturningObservedCard.board.getField(Player2) should be(player2Field)

  it should "have Cactus and EndTurn as next actions" in:
    val afterReturningObservedCard = turnWithSixInHand.act(Activate).act(ObserveOpponent(0)).act(GiveBack(0))
    afterReturningObservedCard.actions should be(Cactus :: EndTurn :: Nil)

  "Activating a seven" should "discard it and allow to observe one of the player's cards" in:
    val afterActivatingSeven = turnWithSevenInHand.act(Activate)
    afterActivatingSeven.hand should be(Nil)
    afterActivatingSeven.board.getTopDiscardStack should be(seven of Swords)
    afterActivatingSeven.actions should be(
      ChooseReplace(0) :: ChooseReplace(1) :: ObservePlayer(0) :: ObservePlayer(1) :: Nil
    )

  "Observing a player card" should "draw it to the player's hand" in:
    val afterObservePlayerCard = turnWithSevenInHand.act(Activate).act(ObservePlayer(0))
    afterObservePlayerCard.hand should be(threeOfCups :: Nil)

  it should "have ReturnToField as next action" in:
    val afterObservePlayerCard = turnWithSevenInHand.act(Activate).act(ObservePlayer(0))
    afterObservePlayerCard.actions should be(ReturnToField(0) :: Nil)

  "Returning to Field" should "return the card to the player's field" in:
    val afterReturningToField =
      SimpleTurn(boardWithDrawableSeven, Player1).act(Draw).act(Activate).act(ObservePlayer(0)).act(ReturnToField(0))
    afterReturningToField.board.getField(Player1) should be(player1Field)

  it should "have Cactus and EndTurn as next actions" in:
    val afterReturningToField = turnWithSevenInHand.act(Activate).act(ObservePlayer(0)).act(ReturnToField(0))
    afterReturningToField.actions should be(Cactus :: EndTurn :: Nil)

  "Activating a jack" should "discard it and allow the player to swap a card with the opponent" in:
    val afterActivatingJack = turnWithJackInHand.act(Activate)
    afterActivatingJack.hand should be(Nil)
    afterActivatingJack.board.getTopDiscardStack should be(jack of Swords)
    afterActivatingJack.actions should be(
      ChooseReplace(0) :: ChooseReplace(1) :: Swap(0, 0) :: Swap(0, 1) :: Swap(1, 0) :: Swap(1, 1) :: Nil
    )

  "Swapping" should "swap cards between player and opponent" in:
    val afterSwapping = turnWithJackInHand.act(Activate).act(Swap(0, 0))
    afterSwapping.board.getField(Player1) should be(fiveOfWands and twoOfSwords)
    afterSwapping.board.getField(Player2) should be(threeOfCups and aceOfPentacles)

  it should "have Cactus and EndTurn as next actions" in:
    val afterSwapping = turnWithJackInHand.act(Activate).act(Swap(0, 0))
    afterSwapping.actions should be(Cactus :: EndTurn :: Nil)
