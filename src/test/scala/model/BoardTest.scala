package org.pps.functus
package model

import board.{BoardFactory, BoardImpl}
import board.Player.{Player1, Player2}
import model.deck.DeckImpl
import model.deck.card.CardImpl
import model.deck.card.Suit.Swords
import model.field.FieldImpl
import model.board.BoardFactory.CustomBoard

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BoardTest extends AnyFlatSpec with Matchers:

  val card1 = CardImpl(1, Swords)
  val card2 = CardImpl(2, Swords)
  val card3 = CardImpl(3, Swords)
  val board = BoardImpl(deck = DeckImpl(Vector(card1, card2)))

  "A new Board" should "have players with empty fields" in:
    val board = BoardFactory()
    board.getField(Player1).length should be(0)
    board.getField(Player2).length should be(0)

  it should "throw IllegalStateException when drawing from empty deck and discard pile" in:
    val emptyBoard = BoardImpl(deck = DeckImpl(Vector.empty), discardPile = Nil)
    an[IllegalStateException] should be thrownBy emptyBoard.draw

  "A board" should "let you draw a card" in:
    val (card, _) = board.draw
    card shouldBe card1

  it should "reduce the deck by one card after draw" in:
    val (_, newBoard) = board.draw
    newBoard.deck.cards.size shouldBe 1

  it should "return different cards on consecutive draws" in:
    val (firstCard, boardAfterFirst) = board.draw
    val (secondCard, _) = boardAfterFirst.draw
    firstCard should not be secondCard

  it should "add a card to the discard pile" in:
    val newBoard = board.discard(card1)
    newBoard.discardPile should contain(card1)

  it should "have the most recently discarded card on top" in:
    val boardAfterDiscard = board.discard(card1).discard(card2)
    boardAfterDiscard.getTopDiscardStack shouldBe card2

  it should "replace a card in a player field and add the old card to discard pile" in:
    val field = FieldImpl(Vector(card1, card2))
    val boardWithPlayer = BoardImpl(
      deck = DeckImpl(Vector(card1, card2)),
      players = Map(Player1 -> field)
    )
    val newBoard = boardWithPlayer.replace(Player1, 0, card3)
    newBoard.getField(Player1).getCard(0)._1 shouldBe card3
    newBoard.discardPile should contain(card1)

  it should "let you take a card from a player field" in:
    val field = FieldImpl(Vector(card1, card2))
    val boardWithPlayer = BoardImpl(
      deck = DeckImpl(Vector(card1, card2)),
      players = Map(Player1 -> field)
    )
    val newBoard = boardWithPlayer.drawPlayerCard(Player1, 0)
    newBoard._1 should be(card1)
    newBoard._2.getField(Player1).length should be(1)

  "A board with empty deck" should "shuffle the discard pile" in:
    val boardEmptyDeck = BoardImpl(
      deck = DeckImpl(Vector.empty),
      discardPile = List(card1, card2, card3)
    )
    val (_, newBoard) = boardEmptyDeck.draw
    val newBoardImpl = newBoard
    newBoardImpl.discardPile shouldBe Nil
    newBoardImpl.deck.cards.size shouldBe 2

  it should "draw a card from the reshuffled discard pile" in:
    val boardEmptyDeck = BoardImpl(
      deck = DeckImpl(Vector.empty),
      discardPile = List(card1, card2, card3)
    )
    val (card, _) = boardEmptyDeck.draw
    List(card1, card2, card3) should contain(card)

  "A board with a king on top of discard pile" should "let you take the king and replace a card" in:
    val king = CardImpl(0, Swords)
    val field = FieldImpl(Vector(card1, card2))
    val boardWithKing = BoardImpl(
      deck = DeckImpl(Vector(card1, card2)),
      discardPile = List(king),
      players = Map(Player1 -> field)
    )
    val newBoard = boardWithKing.kingTopDiscardStack(Player1, 0)
    newBoard.getField(Player1).getCard(0)._1 shouldBe king
    newBoard.discardPile should contain(card1)
    newBoard.discardPile should not contain king

  it should "remove only the top king from the discard pile" in:
    val king = CardImpl(0, Swords)
    val field = FieldImpl(Vector(card1, card2))
    val boardWithKing = BoardImpl(
      deck = DeckImpl(Vector(card1, card2)),
      discardPile = List(king, card3),
      players = Map(Player1 -> field)
    )
    val newBoard = boardWithKing.kingTopDiscardStack(Player1, 0)
    newBoard.discardPile should contain(card3)

  it should "throw IllegalStateException when top of discard pile is not a king" in:
    val field = FieldImpl(Vector(card1, card2))
    val boardNoKing = BoardImpl(
      deck = DeckImpl(Vector(card1, card2)),
      discardPile = List(card1),
      players = Map(Player1 -> field)
    )
    an[IllegalStateException] should be thrownBy boardNoKing.kingTopDiscardStack(Player1, 0)

  it should "place a card in a player field" in:
    val boardWithPlayer = CustomBoard(List(FieldImpl(), FieldImpl()))
    val newBoard = boardWithPlayer.placeCardInField(card1, Player1, 0)
    newBoard.getField(Player1).cardsList should contain(card1)

  it should "not affect other players field when placing a card" in:
    val boardWithPlayer = CustomBoard(List(FieldImpl(), FieldImpl()))
    val newBoard = boardWithPlayer.placeCardInField(card1, Player1, 0)
    newBoard.getField(Player2).cardsList shouldBe List.empty

  it should "not modify the original board after placing a card" in:
    val boardWithPlayer = CustomBoard(List(FieldImpl(), FieldImpl()))
    boardWithPlayer.placeCardInField(card1, Player1, 0)
    boardWithPlayer.getField(Player1).cardsList shouldBe List.empty

  "A CustomBoard" should "create a board with the given fields" in:
    val field1 = FieldImpl(Vector(card1, card2))
    val field2 = FieldImpl(Vector(card3))
    val board = CustomBoard(List(field1, field2))
    board.getField(Player1) shouldBe field1
    board.getField(Player2) shouldBe field2

  it should "throw IllegalArgumentException when players list has wrong size" in:
    val field1 = FieldImpl(Vector(card1))
    an[IllegalArgumentException] should be thrownBy CustomBoard(List(field1))
