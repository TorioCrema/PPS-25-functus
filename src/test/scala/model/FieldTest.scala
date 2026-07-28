package org.pps.functus
package model

import model.field.{Field, FieldImpl}
import model.deck.sugar.CardDSL.*
import model.deck.card.Suit.*
import model.deck.card.Card

import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

class FieldTest extends AnyFlatSpec with Matchers:

  val outOfRangeIndex: Int = 5
  val negativeIndex: Int = -1

  def threeOfSwords: Card = three of Swords
  def twoOfCups: Card = two of Cups
  def aceOfSwords: Card = ace of Swords
  val emptyField: Field = FieldImpl()

  "A new Field" should "have 0 cards" in:
    emptyField.length should be(0)

  it should "return an empty list" in:
    emptyField.cardsList should be(List.empty)

  it should "contain one card after addCard" in:
    val field = emptyField.addCard(threeOfSwords)
    field.length should be(1)
    field.cardsList should be(List(threeOfSwords))

  it should "preserve insertion order after multiple addCard calls" in:
    val field = emptyField.addCard(threeOfSwords).addCard(twoOfCups).addCard(aceOfSwords)
    field.length should be(3)
    field.cardsList should be(List(threeOfSwords, twoOfCups, aceOfSwords))

  "A non-empty Field" should "return the correct card from getCard" in:
    val field = emptyField.addCard(threeOfSwords).addCard(twoOfCups)
    field.getCard(0)._1 should be(threeOfSwords)
    field.getCard(1)._1 should be(twoOfCups)

  it should "remove only the card at the given index after getCard" in:
    val field = emptyField.addCard(threeOfSwords).addCard(twoOfCups).addCard(aceOfSwords)
    val (card, updatedField) = field.getCard(1)
    card should be(twoOfCups)
    updatedField.cardsList should be(List(threeOfSwords, aceOfSwords))

  it should "return the replaced card and update the field after replace" in:
    val field = emptyField.addCard(threeOfSwords)
    val (replaced, updatedField) = field.replace(0, twoOfCups)
    replaced should be(threeOfSwords)
    updatedField.length should be(1)
    updatedField.cardsList should be(List(twoOfCups))

  it should "insert a card at index 0 shifting all others right" in:
    val field = emptyField.addCard(threeOfSwords).addCard(twoOfCups)
    val updatedField = field.addCardAtIndex(aceOfSwords, 0)
    updatedField.cardsList should be(List(aceOfSwords, threeOfSwords, twoOfCups))

  it should "insert a card at a middle index shifting subsequent cards right" in:
    val field = emptyField.addCard(threeOfSwords).addCard(twoOfCups)
    val updatedField = field.addCardAtIndex(aceOfSwords, 1)
    updatedField.length should be(3)
    updatedField.cardsList should be(List(threeOfSwords, aceOfSwords, twoOfCups))

  it should "insert a card at the last valid index" in:
    val field = emptyField.addCard(threeOfSwords).addCard(twoOfCups)
    val updatedField = field.addCardAtIndex(aceOfSwords, 1)
    updatedField.cardsList should be(List(threeOfSwords, aceOfSwords, twoOfCups))

  "A Field" should "throw IndexOutOfBoundsException when replacing in an empty field" in:
    an[IndexOutOfBoundsException] should be thrownBy emptyField.replace(0, threeOfSwords)

  it should "throw IndexOutOfBoundsException when getting a card from an empty field" in:
    an[IndexOutOfBoundsException] should be thrownBy emptyField.getCard(0)

  it should "throw IndexOutOfBoundsException when addCardAtIndex on an empty field" in:
    an[IndexOutOfBoundsException] should be thrownBy emptyField.addCardAtIndex(threeOfSwords, 0)

  it should "throw IndexOutOfBoundsException when replacing at an out-of-range index" in:
    val field = emptyField.addCard(threeOfSwords)
    an[IndexOutOfBoundsException] should be thrownBy field.replace(outOfRangeIndex, twoOfCups)

  it should "throw IndexOutOfBoundsException when replacing at a negative index" in:
    val field = emptyField.addCard(threeOfSwords)
    an[IndexOutOfBoundsException] should be thrownBy field.replace(negativeIndex, twoOfCups)

  it should "throw IndexOutOfBoundsException when getting a card at an out-of-range index" in:
    val field = emptyField.addCard(threeOfSwords)
    an[IndexOutOfBoundsException] should be thrownBy field.getCard(outOfRangeIndex)

  it should "throw IndexOutOfBoundsException when getting a card at a negative index" in:
    val field = emptyField.addCard(threeOfSwords)
    an[IndexOutOfBoundsException] should be thrownBy field.getCard(negativeIndex)

  it should "throw IndexOutOfBoundsException when addCardAtIndex at an out-of-range index" in:
    val field = emptyField.addCard(threeOfSwords)
    an[IndexOutOfBoundsException] should be thrownBy field.addCardAtIndex(twoOfCups, outOfRangeIndex)

  it should "throw IndexOutOfBoundsException when addCardAtIndex at a negative index" in:
    val field = emptyField.addCard(threeOfSwords)
    an[IndexOutOfBoundsException] should be thrownBy field.addCardAtIndex(twoOfCups, negativeIndex)
