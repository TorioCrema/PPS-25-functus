package org.pps.functus
package model

import model.deck.sugar.CardDSL.{*, given}
import model.deck.card.Suit.*
import model.deck.sugar.FieldDSL.{*, given}
import model.board.*
import model.board.Player.*
import model.deck.DeckImpl
import model.playable.turn.Turns.SimpleTurn
import model.playable.turn.Action.*
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

  "Activating a six" should "allow to observe one of the opponent's cards or replace" in:
    val afterActivatingSix = turnWithSixInHand.act(Activate)
    afterActivatingSix.hand should be((six of Swords) :: Nil)
    afterActivatingSix.actions should be(
      ChooseReplace(0) :: ChooseReplace(1) :: ObserveOpponent(0) :: ObserveOpponent(1) :: Nil
    )

  "Observing an opponent's card" should "draw it to the player's hand and allow to give it back" in:
    val afterObservingOpponent = turnWithSixInHand.actAll(Activate :: ObserveOpponent(0) :: Nil)
    afterObservingOpponent.hand should be(fiveOfWands :: Nil)
    afterObservingOpponent.actions should be(GiveBack(0) :: Nil)

  "Giving back the observed card" should "return it to its original place and allow to call cactus or end the turn" in:
    val afterReturningObservedCard = turnWithSixInHand.actAll(Activate :: ObserveOpponent(0) :: GiveBack(0) :: Nil)
    afterReturningObservedCard.board.getField(Player2) should be(player2Field)
    afterReturningObservedCard.actions should be(Cactus :: EndTurn :: Nil)

  "Activating a seven" should "allow to observe one of the player's cards or replace" in:
    val afterActivatingSeven = turnWithSevenInHand.act(Activate)
    afterActivatingSeven.hand should be((seven of Swords) :: Nil)
    afterActivatingSeven.actions should be(
      ChooseReplace(0) :: ChooseReplace(1) :: ObservePlayer(0) :: ObservePlayer(1) :: Nil
    )

  "Observing a player card" should "draw it to the player's hand and allow to return it" in:
    val afterObservePlayerCard = turnWithSevenInHand.actAll(Activate :: ObservePlayer(0) :: Nil)
    afterObservePlayerCard.hand should be(threeOfCups :: Nil)
    afterObservePlayerCard.actions should be(ReturnToField(0) :: Nil)

  "Returning to Field" should "return the card to the player's field and allow to call cactus or end the turn" in:
    val afterReturningToField =
      SimpleTurn(boardWithDrawableSeven, Player1).actAll(
        Draw :: Activate :: ObservePlayer(0) :: ReturnToField(0) :: Nil
      )
    afterReturningToField.board.getField(Player1) should be(player1Field)
    afterReturningToField.actions should be(Cactus :: EndTurn :: Nil)

  "Activating a jack" should "discard it and allow the player to swap a card with the opponent" in:
    val afterActivatingJack = turnWithJackInHand.act(Activate)
    afterActivatingJack.hand should be((jack of Swords) :: Nil)
    afterActivatingJack.actions should be(
      ChooseReplace(0) :: ChooseReplace(1) :: Swap(0, 0) :: Swap(0, 1) :: Swap(1, 0) :: Swap(1, 1) :: Nil
    )

  "Swapping" should "swap cards between player and opponent and allow to call cactus or end the turn" in:
    for
      playerIndex <- 0 until player1Field.length
      opponentIndex <- 0 until player2Field.length
    do
      val (playerCard, drawFromPlayer) = boardWithDrawableJack.drawPlayerCard(Player1, playerIndex)
      val (opponentCard, drawnBoth) = drawFromPlayer.drawPlayerCard(Player2, opponentIndex)
      val swappedPlayer = drawnBoth.placeCardInField(opponentCard, Player1, Some(playerIndex))
      val expectedBoard = swappedPlayer.placeCardInField(playerCard, Player2, Some(opponentIndex))
      val afterSwapping = turnWithJackInHand.actAll(Activate :: Swap(playerIndex, opponentIndex) :: Nil)
      for player <- Player.values do afterSwapping.board.getField(player) should be(expectedBoard.getField(player))
      afterSwapping.actions should be(Cactus :: EndTurn :: Nil)
