package org.pps.functus
package model

import model.deck.card.{Card, CardImpl}
import model.deck.card.Suit.Swords
import model.field.{Field, FieldImpl}

import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

class FieldTest extends AnyFlatSpec with Matchers:

  def card = CardImpl(0, Swords)
  def first = CardImpl(0, Swords)
  def second = CardImpl(1, Swords)
  val fieldTest: Field = FieldImpl()

  "A new Field" should "have 0 cards" in:
    val field: Field = FieldImpl()
    field.length should be(0)

  it should "throw an IndexOutOfBoundsException when replacing a card in an empty field" in:
    an[IndexOutOfBoundsException] should be thrownBy
      FieldImpl().replace(0, card)

  it should "throw an IndexOutOfBoundsException when getting a card from an empty field" in:
    an[IndexOutOfBoundsException] should be thrownBy
      FieldImpl().getCard(0)

  it should "contain one card after adding a card" in:
    val field = FieldImpl().addCard(card)
    field.length should be(1)
    field.getCard(0)._1 should be(card)

  it should "return an empty list for a new field" in:
    FieldImpl().cardsList should be(List.empty)

  it should "return a list with one card after adding a card" in:
    val field = FieldImpl().addCard(card)
    field.cardsList should be(List(card))

  it should "return cards in the same order they were added" in:
    val field = FieldImpl().addCard(first).addCard(second)
    field.cardsList should be(List(first, second))

  it should "reflect changes after replace in cardsList" in:
    val field = FieldImpl().addCard(first)
    val (_, updatedField) = field.replace(0, second)
    updatedField.cardsList should be(List(second))

  it should "maintain card order when adding multiple cards" in:
    val field = FieldImpl().addCard(first).addCard(second)
    field.length should be(2)
    field.getCard(0)._1 should be(first)
    field.getCard(1)._1 should be(second)

  it should "replace a card correctly and return the replaced card" in:
    val field = FieldImpl().addCard(first)
    val result: (Card, Field) = field.replace(0, second)
    result._1 should be(first)
    result._2.length should be(1)
    result._2.getCard(0)._1 should be(second)

  it should "throw an IndexOutOfBoundsException when replacing out of range" in:
    val field = FieldImpl().addCard(card)
    an[IndexOutOfBoundsException] should be thrownBy
      field.replace(5, card)

  it should "throw an IndexOutOfBoundsException when getting a card out of range" in:
    val field = FieldImpl().addCard(card)
    an[IndexOutOfBoundsException] should be thrownBy
      field.getCard(5)
