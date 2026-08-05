package org.pps.functus
package model

import model.deck.DeckImpl
import model.deck.sugar.CardDSL.*
import model.deck.sugar.FieldDSL.given
import model.deck.card.Suit.*
import model.turn.Action.{ChooseDiscard, ChooseReplace}
import model.turn.Turns.SimpleTurn
import model.board.BoardImpl
import model.board.Player.*
import model.turn.Action.*

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SimpleTurnTest extends AnyFlatSpec with Matchers:
  private val threeOfCups = three of Cups
  private val twoOfSwords = two of Swords
  private val fiveOfWands = five of Wands
  private val aceOfPentacles = ace of Pentacles
  private val player1Field = threeOfCups and twoOfSwords
  private val player2Field = fiveOfWands and aceOfPentacles
  private val startingBoard =
    BoardImpl(deck = DeckImpl(Vector(two of Wands)), players = Map((Player1, player1Field), (Player2, player2Field)))
  private val player = Player1
  private val simpleTurn = SimpleTurn(startingBoard, player)
  private val boardWithThreeInDiscard = startingBoard.discard(three of Pentacles)
  private val discardableTurn = SimpleTurn(boardWithThreeInDiscard, player)

  "SimpleTurn" should "have draw as next action if top of discard stack is empty" in:
    simpleTurn.actions should be(Draw :: Nil)

  it should "have draw and draw king as next actions if top of discard stack is a king" in:
    SimpleTurn(startingBoard.discard(king of Swords), player).actions should be(Draw :: DrawKing :: Nil)

  it should "have draw and choose discard as next actions if discard stack isn't empty and top isn't a king" in:
    val expectedActions = Draw :: (0 until player1Field.length).toList.map(index => ChooseDiscard(index))
    SimpleTurn(startingBoard.discard(five of Wands), player).actions should be(expectedActions)

  it should "draw to hand when executing Draw" in:
    val afterDraw = simpleTurn.act(Draw)
    afterDraw.hand.length should be(1)
    afterDraw.hand.head should be(two of Wands)

  it should "draw from discard pile when drawing king" in:
    val boardWithKingInDiscard = startingBoard.discard(king of Swords)
    val afterDrawKing = SimpleTurn(boardWithKingInDiscard, player).act(DrawKing)
    afterDrawKing.actions should be(Activate :: Nil)
    afterDrawKing.board.discardPile.isEmpty should be(true)
    afterDrawKing.hand should be((king of Swords) :: Nil)

  it should "throw an IllegalArgumentException when drawing without a king on the discard pile" in:
    an[IllegalArgumentException] should be thrownBy SimpleTurn(startingBoard.discard(threeOfCups), player).act(DrawKing)

  it should "have choose replace as next actions" in:
    val afterDraw = simpleTurn.act(Draw)
    afterDraw.act(Activate).actions should be((0 until player1Field.length).map(ChooseReplace(_)))

  it should "replace card in player field and discard after picking index" in:
    val afterDraw = simpleTurn.act(Draw)
    for index <- 0 until player1Field.length
    do
      val replacedCard = afterDraw.board.getField(player).getCard(index)._1
      val afterReplace = afterDraw.act(Activate).act(ChooseReplace(index))
      afterReplace.hand.isEmpty should be(true)
      afterReplace.board.discardPile.head should be(replacedCard)
      val replacedField = afterReplace.board.getField(player)
      replacedField.getCard(index)._1 should be(two of Wands)
      replacedField.length should be(player1Field.length)

  it should "have cactus and end turn as next actions after replacing" in:
    val afterReplace = simpleTurn.act(Draw).act(Activate).act(ChooseReplace(0))
    afterReplace.actions should be(Cactus :: EndTurn :: Nil)

  it should "end with cactus" in:
    val afterCactus = simpleTurn.act(Draw).act(Activate).act(ChooseReplace(0)).act(Cactus)
    afterCactus.cactus should be(true)
    afterCactus.actions should be(EndTurn :: Nil)

  it should "be over after end turn action" in:
    val afterEndTurn = simpleTurn.act(Draw).act(Activate).act(ChooseReplace(0)).act(EndTurn)
    afterEndTurn.isOver should be(true)

  it should "draw the chosen card to the player's hand" in:
    for i <- 0 until player1Field.length
    do
      val afterChooseDiscard = discardableTurn.act(ChooseDiscard(i))
      afterChooseDiscard.hand should be(discardableTurn.board.getField(player).getCard(i)._1 :: Nil)

  it should "discard without penalty when discarding the correct value" in:
    val afterCorrectDiscard = discardableTurn.act(ChooseDiscard(0)).act(Discard(0))
    afterCorrectDiscard.board.getField(player).length should be(player1Field.length - 1)
    afterCorrectDiscard.board.discardPile.length should be(discardableTurn.board.discardPile.length + 1)
    afterCorrectDiscard.board.discardPile.head should be(threeOfCups)

  it should "apply penalty when discarding the wrong value" in:
    val afterWrongDiscard = discardableTurn.act(ChooseDiscard(1)).act(Discard(1))
    afterWrongDiscard.board.getField(player).length should be(player1Field.length + 1)
    afterWrongDiscard.board.getTopDiscardStack should be(three of Pentacles)

  it should "have draw as next actions after choosing discard" in:
    for discardIndex <- 0 until player1Field.length
    do
      SimpleTurn(boardWithThreeInDiscard, player)
        .act(ChooseDiscard(discardIndex))
        .act(Discard(discardIndex))
        .actions should be(Draw :: Nil)

  it should "throw an IllegalArgumentException when discarding beyond the player's field length" in:
    an[IllegalArgumentException] should be thrownBy discardableTurn.act(ChooseDiscard(player1Field.length))
    an[IllegalArgumentException] should be thrownBy discardableTurn.act(ChooseDiscard(-1))
