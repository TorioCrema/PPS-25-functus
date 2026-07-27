package org.pps.functus
package model

import model.board.BoardFactory.CustomBoard
import model.deck.DeckImpl
import model.deck.sugar.CardDSL.*
import model.deck.sugar.FieldDSL.{*, given}
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
  private val board = BoardImpl(players = Map((Player1, player1Field), (Player2, player2Field)))

  "SimpleTurn" should "have draw as next action if top of discard stack is empty" in:
    val startingBoard = CustomBoard(List(player1Field, player2Field))
    SimpleTurn(startingBoard, Player1).actions should be(List(Draw))

  it should "have draw and draw king as next actions if top of discard stack is a king" in:
    val startingBoard = BoardImpl(discardPile = List(king of Swords))
    SimpleTurn(startingBoard, Player1).actions should be(List(Draw, DrawKing))

  it should "have draw and choose discard as next actions discard stack isn't empty and top isn't a king" in:
    val startingBoard =
      BoardImpl(discardPile = List(five of Wands), players = Map((Player1, player1Field), (Player2, player2Field)))
    val expectedActions = List(Draw) ++ (0 until player1Field.length).toList.map(index => ChooseDiscard(index))
    SimpleTurn(startingBoard, Player1).actions should be(expectedActions)

  it should "draw to hand when executing Draw" in:
    val startingBoard =
      BoardImpl(deck = DeckImpl(Vector(two of Wands)), players = Map((Player1, player1Field), (Player2, player2Field)))
    val afterDraw = SimpleTurn(startingBoard, Player1).act(Draw)
    afterDraw.hand.length should be(1)
    afterDraw.hand.head should be(two of Wands)

  it should "have choose replace as next actions" in:
    val startingBoard =
      BoardImpl(deck = DeckImpl(Vector(two of Wands)), players = Map((Player1, player1Field), (Player2, player2Field)))
    val afterDraw = SimpleTurn(startingBoard, Player1).act(Draw)
    afterDraw.act(Activate).actions should be((0 until player1Field.length).map(ChooseReplace(_)))

  it should "replace after picking index" in:
    val startingBoard =
      BoardImpl(deck = DeckImpl(Vector(two of Wands)), players = Map((Player1, player1Field), (Player2, player2Field)))
    val replacedField = (two of Wands) and twoOfSwords
    val expectedBoard =
      BoardImpl(DeckImpl(Vector()), List(threeOfCups), Map((Player1, replacedField), (Player2, player2Field)))
    val afterReplace = SimpleTurn(startingBoard, Player1).act(Draw).act(ChooseReplace(0))
    afterReplace.board should be(expectedBoard)
