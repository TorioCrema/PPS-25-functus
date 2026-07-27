package org.pps.functus
package model

import model.board.Player.*
import model.board.{BoardFactory, BoardImpl}
import model.deck.DeckImpl
import model.field.FieldImpl
import model.deck.card.CardImpl
import model.deck.card.Suit.Swords

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BoardFactoryTest extends AnyFlatSpec with Matchers:

  val card1 = CardImpl(1, Swords)
  val card2 = CardImpl(2, Swords)
  val card3 = CardImpl(3, Swords)

  "BoardFactory" should "create a board with empty fields" in:
    val board = BoardFactory()
    board.getField(Player1).length should be(0)
    board.getField(Player2).length should be(0)

  it should "create a board with a shuffled deck" in:
    val board = BoardFactory()
    board.asInstanceOf[BoardImpl].deck.cards.size should be(40)

  "BoardWithPopulatedFields" should "create a board with 4 cards per player" in:
    val board = BoardFactory.BoardWithPopulatedFields()
    board.getField(Player1).length should be(4)
    board.getField(Player2).length should be(4)

  it should "create a board with 32 cards remaining in the deck" in:
    val board = BoardFactory.BoardWithPopulatedFields()
    board.asInstanceOf[BoardImpl].deck.cards.size should be(32)

  it should "have different cards in each player field" in:
    val board = BoardFactory.BoardWithPopulatedFields()
    val player1Cards = board.getField(Player1).cardsList
    val player2Cards = board.getField(Player2).cardsList
    player1Cards.intersect(player2Cards) should be (List.empty)

  "CustomBoard" should "create a board with the given fields" in:
    val field1 = FieldImpl(Vector(card1, card2))
    val field2 = FieldImpl(Vector(card3))
    val board = BoardFactory.CustomBoard(List(field1, field2))
    board.getField(Player1).cardsList should be (List(card1, card2))
    board.getField(Player2).cardsList should be (List(card3))

  it should "create a board with the given deck" in:
    val deck = DeckImpl(Vector(card1, card2, card3))
    val board = BoardFactory.CustomBoard(List(FieldImpl(), FieldImpl()), deck)
    board.asInstanceOf[BoardImpl].deck.cards.size should be (3)

  it should "throw IllegalArgumentException when players list has wrong size" in:
    an[IllegalArgumentException] should be thrownBy
      BoardFactory.CustomBoard(List(FieldImpl()))
