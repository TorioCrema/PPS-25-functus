package org.pps.functus
package model

import model.deck.sugar.CardDSL.*
import model.board.Player.{Player1, Player2}
import model.board.BoardFactory.CustomBoard
import model.board.{Board, BoardFactory}
import model.deck.card.{Card, CardImpl}
import model.deck.card.Suit.*
import model.field.FieldImpl
import model.deck.DeckImpl
import model.deck.sugar.DeckDSL.deck
import model.deck.sugar.DeckDSL.deck.|

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BoardTest extends AnyFlatSpec with Matchers:

  def threeOfSwords: Card = three of Swords
  def twoOfCups: Card = two of Cups
  def aceOfSwords: Card = ace of Swords

  val twoCardBoard: Board = CustomBoard(List(FieldImpl(), FieldImpl()), deck from threeOfSwords | twoOfCups)
  val emptyFieldBoard: Board = CustomBoard(List(FieldImpl(), FieldImpl()))
  val populatedFieldBoard: Board = CustomBoard(
    List(FieldImpl(Vector(threeOfSwords, twoOfCups)), FieldImpl())
  )

  "A new Board" should "have players with empty fields" in:
    val board = BoardFactory()
    board.getField(Player1).length should be(0)
    board.getField(Player2).length should be(0)

  it should "should return empty when drawing from empty deck and having a empty discard pile" in:
    val board = CustomBoard(List(FieldImpl(), FieldImpl()), DeckImpl(Vector.empty))
    board.draw() should be(Option.empty)

  "A board" should "let you draw a card" in:
    val (card, _) = twoCardBoard.draw().get
    card should be(threeOfSwords)

  it should "reduce the deck by one card after draw" in:
    val (_, newBoard) = twoCardBoard.draw().get
    newBoard.deck.cards.size should be(1)

  it should "return different cards on consecutive draws" in:
    val (firstCard, boardAfterFirst) = twoCardBoard.draw().get
    val (secondCard, _) = boardAfterFirst.draw().get
    firstCard should not be secondCard

  it should "add a card to the discard pile" in:
    val newBoard = twoCardBoard.discard(threeOfSwords)
    newBoard.discardPile should contain(threeOfSwords)

  it should "have the most recently discarded card on top" in:
    val boardAfterDiscard = twoCardBoard.discard(threeOfSwords).discard(twoOfCups)
    boardAfterDiscard.getTopDiscardStack should be(twoOfCups)

  it should "replace a card in a player field and add the old card to discard pile" in:
    val newBoard = populatedFieldBoard.replace(Player1, 0, aceOfSwords)
    newBoard.getField(Player1).getCard(0)._1 should be(aceOfSwords)
    newBoard.discardPile should contain(threeOfSwords)

  it should "let you take a card from a player field" in:
    val (card, newBoard) = populatedFieldBoard.drawPlayerCard(Player1, 0)
    card should be(threeOfSwords)
    newBoard.getField(Player1).length should be(1)

  "A board with empty deck" should "shuffle the discard pile" in:
    val board = CustomBoard(
      List(FieldImpl(), FieldImpl()),
      DeckImpl(Vector.empty)
    ).discard(threeOfSwords).discard(twoOfCups).discard(aceOfSwords)
    val (_, newBoard) = board.draw().get
    newBoard.discardPile should be(Nil)
    newBoard.deck.cards.size should be(2)

  it should "draw a card from the reshuffled discard pile" in:
    val board = CustomBoard(
      List(FieldImpl(), FieldImpl()),
      DeckImpl(Vector.empty)
    ).discard(threeOfSwords).discard(twoOfCups).discard(aceOfSwords)
    val (card, _) = board.draw().get
    List(threeOfSwords, twoOfCups, aceOfSwords) should contain(card)

  "A board with a king on top of discard pile" should "return the king and remove it from the discard pile" in:
    val king = CardImpl(0, Swords)
    val board = CustomBoard(
      List(FieldImpl(), FieldImpl()),
      deck from king | aceOfSwords | twoOfCups
    ).discard(king)

    val (returnedKing, newBoard) = board
      .kingTopDiscardStack()
      .getOrElse(
        throw IllegalStateException("Expected a king on top of discard pile")
      )
    returnedKing should be(king)
    newBoard.discardPile should not contain king

  it should "remove only the top king from the discard pile" in:
    val king = CardImpl(0, Swords)
    val board = CustomBoard(
      List(FieldImpl(), FieldImpl()),
      DeckImpl(Vector.empty)
    ).discard(aceOfSwords).discard(king)
    val (_, newBoard) = board
      .kingTopDiscardStack()
      .getOrElse(
        throw IllegalStateException("Expected a king on top of discard pile")
      )
    newBoard.discardPile should contain(aceOfSwords)

  it should "return Left when top of discard pile is not a king" in:
    val board = CustomBoard(
      List(FieldImpl(), FieldImpl()),
      DeckImpl(Vector.empty)
    ).discard(threeOfSwords)
    board.kingTopDiscardStack() shouldBe a[Left[?,?]]

  it should "place a card in a player field" in:
    val newBoard = emptyFieldBoard.placeCardInField(threeOfSwords, Player1)
    newBoard.getField(Player1).cardsList should contain(threeOfSwords)

  it should "not affect other players field when placing a card" in:
    val newBoard = emptyFieldBoard.placeCardInField(threeOfSwords, Player1)
    newBoard.getField(Player2).cardsList should be(List.empty)

  it should "place a card at a specific index between existing cards" in:
    val field = FieldImpl().addCard(threeOfSwords).addCard(twoOfCups).addCard(aceOfSwords)
    val board = CustomBoard(List(field, FieldImpl()))
    val newBoard = board.placeCardInField(CardImpl(4, Swords), Player1, Some(1))
    newBoard.getField(Player1).cardsList should be(List(threeOfSwords, CardImpl(4, Swords), twoOfCups, aceOfSwords))

  "A CustomBoard" should "create a board with the given fields" in:
    val field1 = FieldImpl(Vector(threeOfSwords, twoOfCups))
    val field2 = FieldImpl(Vector(aceOfSwords))
    val board = CustomBoard(List(field1, field2))
    board.getField(Player1) should be(field1)
    board.getField(Player2) should be(field2)

  it should "throw IllegalArgumentException when players list has wrong size" in:
    an[IllegalArgumentException] should be thrownBy CustomBoard(List(FieldImpl()))
