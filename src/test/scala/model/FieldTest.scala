package org.pps.functus
package model

import model.deck.card.CardImpl
import model.deck.card.Suit.Swords
import model.field.{Field, FieldImpl}

import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

class FieldTest extends AnyFlatSpec with Matchers:

  val card = CardImpl(0, Swords)
  val fieldTest: Field = FieldImpl()

  "A new Field" should "have 0 cards" in:
    val field: Field = FieldImpl()
    field.length should be(0)

  it should "throw an IndexOutOfBoundsException when replacing a card in an empty field" in:
    val field: Field = FieldImpl()
    an[IndexOutOfBoundsException] should be thrownBy
      field.replace(0, card)

  it should "throw an IndexOutOfBoundsException when getting a card from an empty field" in:
    val field: Field = FieldImpl()
    an[IndexOutOfBoundsException] should be thrownBy
      field.getCard(0)

  it should "contain one card after adding a card" in:
    val field = FieldImpl()
    val updatedField = field.addCard(card)
    updatedField.length should be(1)
    updatedField.getCard(0) should be(card)

  it should "replace a card correctly" in:
    val first = CardImpl(0, Swords)
    val second = CardImpl(1, Swords)
    val field = FieldImpl().addCard(first)
    val replaced = field.replace(0, second)._2
    replaced.length should be(1)
    replaced.getCard(0) should be(second)
