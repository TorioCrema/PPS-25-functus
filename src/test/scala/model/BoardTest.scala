package org.pps.functus
package model

import model.board.BoardFactory.CustomBoard
import model.board.Player.{Player1, Player2}
import model.board.{Board, BoardFactory}
import model.deck.card.Suit.Swords
import model.deck.card.CardImpl
import model.field.FieldImpl
import model.deck.DeckImpl

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BoardTest extends AnyFlatSpec with Matchers:

  val card1 = CardImpl(1, Swords)
  val card2 = CardImpl(2, Swords)
  val card3 = CardImpl(3, Swords)

  val twoCardBoard: Board = CustomBoard(List(FieldImpl(), FieldImpl()), DeckImpl(Vector(card1, card2)))
  val emptyFieldBoard: Board = CustomBoard(List(FieldImpl(), FieldImpl()))
  val populatedFieldBoard: Board = CustomBoard(
    List(FieldImpl(Vector(card1, card2)), FieldImpl())
  )

  "A new Board" should "have players with empty fields" in:
    val board = BoardFactory()
    board.getField(Player1).length should be(0)
    board.getField(Player2).length should be(0)

  it should "throw IllegalStateException when drawing from empty deck and discard pile" in:
    val board = CustomBoard(List(FieldImpl(), FieldImpl()), DeckImpl(Vector.empty))
    an[IllegalStateException] should be thrownBy board.draw

  "A board" should "let you draw a card" in:
    val (card, _) = twoCardBoard.draw
    card should be(card1)

  it should "reduce the deck by one card after draw" in:
    val (_, newBoard) = twoCardBoard.draw
    newBoard.deck.cards.size should be(1)

  it should "return different cards on consecutive draws" in:
    val (firstCard, boardAfterFirst) = twoCardBoard.draw
    val (secondCard, _) = boardAfterFirst.draw
    firstCard should not be secondCard

  it should "add a card to the discard pile" in:
    val newBoard = twoCardBoard.discard(card1)
    newBoard.discardPile should contain(card1)

  it should "have the most recently discarded card on top" in:
    val boardAfterDiscard = twoCardBoard.discard(card1).discard(card2)
    boardAfterDiscard.getTopDiscardStack should be(card2)

  it should "replace a card in a player field and add the old card to discard pile" in:
    val newBoard = populatedFieldBoard.replace(Player1, 0, card3)
    newBoard.getField(Player1).getCard(0)._1 should be(card3)
    newBoard.discardPile should contain(card1)

  it should "let you take a card from a player field" in:
    val (card, newBoard) = populatedFieldBoard.drawPlayerCard(Player1, 0)
    card should be(card1)
    newBoard.getField(Player1).length should be(1)

  "A board with empty deck" should "shuffle the discard pile" in:
    val board = CustomBoard(
      List(FieldImpl(), FieldImpl()),
      DeckImpl(Vector.empty)
    ).discard(card1).discard(card2).discard(card3)
    val (_, newBoard) = board.draw
    newBoard.discardPile should be(Nil)
    newBoard.deck.cards.size should be(2)

  it should "draw a card from the reshuffled discard pile" in:
    val board = CustomBoard(
      List(FieldImpl(), FieldImpl()),
      DeckImpl(Vector.empty)
    ).discard(card1).discard(card2).discard(card3)
    val (card, _) = board.draw
    List(card1, card2, card3) should contain(card)

  "A board with a king on top of discard pile" should "return the king and remove it from the discard pile" in:
    val king = CardImpl(0, Swords)
    val board = CustomBoard(
      List(FieldImpl(), FieldImpl()),
      DeckImpl(Vector.empty)
    ).discard(king)
    val (returnedKing, newBoard) = board.kingTopDiscardStack()
    returnedKing should be(king)
    newBoard.discardPile should not contain king

  it should "remove only the top king from the discard pile" in:
    val king = CardImpl(0, Swords)
    val board = CustomBoard(
      List(FieldImpl(), FieldImpl()),
      DeckImpl(Vector.empty)
    ).discard(card3).discard(king)
    val (_, newBoard) = board.kingTopDiscardStack()
    newBoard.discardPile should contain(card3)

  it should "throw IllegalStateException when top of discard pile is not a king" in:
    val board = CustomBoard(
      List(FieldImpl(), FieldImpl()),
      DeckImpl(Vector.empty)
    ).discard(card1)
    an[IllegalStateException] should be thrownBy board.kingTopDiscardStack()

  it should "place a card in a player field" in:
    val newBoard = emptyFieldBoard.placeCardInField(card1, Player1, Option.empty)
    newBoard.getField(Player1).cardsList should contain(card1)

  it should "not affect other players field when placing a card" in:
    val newBoard = emptyFieldBoard.placeCardInField(card1, Player1, Option.empty)
    newBoard.getField(Player2).cardsList should be(List.empty)

  it should "place a card at a specific index between existing cards" in:
    val field = FieldImpl().addCard(card1).addCard(card2).addCard(card3)
    val board = CustomBoard(List(field, FieldImpl()))
    val newBoard = board.placeCardInField(CardImpl(4, Swords), Player1, Option(1))
    newBoard.getField(Player1).cardsList should be(List(card1, CardImpl(4, Swords), card2, card3))

  "A CustomBoard" should "create a board with the given fields" in:
    val field1 = FieldImpl(Vector(card1, card2))
    val field2 = FieldImpl(Vector(card3))
    val board = CustomBoard(List(field1, field2))
    board.getField(Player1) should be(field1)
    board.getField(Player2) should be(field2)

  it should "throw IllegalArgumentException when players list has wrong size" in:
    an[IllegalArgumentException] should be thrownBy CustomBoard(List(FieldImpl()))
