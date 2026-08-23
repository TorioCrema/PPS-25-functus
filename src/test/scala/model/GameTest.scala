package org.pps.functus
package model

import model.game.{Game, GamePhase}
import model.turn.Action.{Activate, Cactus, ChooseReplace, Confirm, Draw, EndTurn, Observe}
import model.board.Player.{Player1, Player2}
import model.game.GamePhase.*
import model.deck.card.Suit.*
import model.deck.sugar.CardDSL.*
import model.deck.sugar.BoardDSL.*
import model.deck.sugar.CardDSL.of
import model.deck.sugar.FieldDSL.given_Conversion_Card_FieldBuilderLike
import model.deck.sugar.DeckDSL.deck.|
import model.deck.sugar.DeckDSL.deck as deckDSL
import model.deck.sugar.FieldDSL.{*, given}

import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class GameTest extends AnyFlatSpec with Matchers:

  private def boardTest = default board

  /** Deterministic board for Playing / LastTurn / Over tests.
    *
    *   - Fixed cards (values 1–5, no special effects) on both fields.
    *   - Custom deck contains only cards with values 1–5 so Activate never triggers a special effect and always returns
    *     List(Cactus, EndTurn).
    *   - Discard pile has one non-king card → SimpleTurn always offers ChooseDiscard.
    *
    * Deterministic SimpleTurn flow: ChooseDiscard(0) → Discard(0) → Draw → Activate → EndTurn | Cactus + EndTurn
    */
  private def safeDeck = deckDSL from (
    (ace of Cups) | (two of Cups) | (three of Cups) | (four of Cups) | (five of Cups) |
      (ace of Swords) | (two of Swords) | (three of Swords) | (four of Swords) | (five of Swords) |
      (ace of Wands) | (two of Wands) | (three of Wands) | (four of Wands) | (five of Wands) |
      (ace of Pentacles) | (two of Pentacles) | (three of Pentacles) | (four of Pentacles) | (five of Pentacles)
  )

  private def deterministicBoard =
    val p1Field = (1 of Cups) and (2 of Cups) and (3 of Cups) and (4 of Cups)
    val p2Field = (1 of Swords) and (2 of Swords) and (3 of Swords) and (4 of Swords)
    (lockedBoard from default)
      .withCustom(playerOne(p1Field))
      .withCustom(playerTwo(p2Field))
      .withCustom(discardPile((5 of Cups) | (6 of Cups)))
      .withCustom(deck(safeDeck))

  private def playFirstTurn(game: Game): Game =
    game.act(Observe).get.act(Confirm).get.act(EndTurn).get

  private def playBothFirstTurns(game: Game): Game =
    playFirstTurn(playFirstTurn(game))

  /** Plays one SimpleTurn without calling Cactus. */
  private def playSimpleTurn(game: Game): Game =
    game
      .act(Draw)
      .get
      .act(Activate)
      .get
      .act(ChooseReplace(0))
      .get
      .act(EndTurn)
      .get

  /** Plays one SimpleTurn and calls Cactus at the end. */
  private def playSimpleTurnWithCactus(game: Game): Game =
    game
      .act(Draw)
      .get
      .act(Activate)
      .get
      .act(ChooseReplace(0))
      .get
      .act(Cactus)
      .get
      .act(EndTurn)
      .get

  private def gameInPlaying: Game = playBothFirstTurns(Game(deterministicBoard))

  private def gameInLastTurn: Game = playSimpleTurnWithCactus(gameInPlaying)

  private def gameOver: Game = playSimpleTurn(gameInLastTurn)

  "A new Game" should "start in the FirstTurns phase" in:
    Game(boardTest).phase should be(FirstTurns)

  it should "start with Player1 as the current player" in:
    Game(boardTest).currentPlayer should be(Player1)

  it should "expose Observe as the only available action at the beginning" in:
    Game(boardTest).currentTurn.actions should be(List(Observe))

  it should "not be over at the start" in:
    Game(boardTest).isOver should be(false)

  "A game during Player1 first turn" should "remain in FirstTurns after Observe" in:
    Game(boardTest).act(Observe).get.phase shouldBe FirstTurns

  it should "still be Player1's turn after Observe" in:
    Game(boardTest).act(Observe).get.currentPlayer shouldBe Player1

  it should "expose only Confirm after Observe" in:
    Game(boardTest).act(Observe).get.currentTurn.actions shouldBe List(Confirm)

  it should "expose only EndTurn after Confirm" in:
    Game(boardTest).act(Observe).get.act(Confirm).get.currentTurn.actions shouldBe List(EndTurn)

  "After Player1 first turn" should "switch to Player2" in:
    playFirstTurn(Game(boardTest)).currentPlayer shouldBe Player2

  it should "remain in FirstTurns" in:
    playFirstTurn(Game(boardTest)).phase shouldBe FirstTurns

  it should "give Player2 Observe as first action" in:
    playFirstTurn(Game(boardTest)).currentTurn.actions shouldBe List(Observe)

  "After both first turns" should "transition to Playing" in:
    playBothFirstTurns(Game(boardTest)).phase shouldBe Playing

  it should "have Player1 as current player" in:
    playBothFirstTurns(Game(boardTest)).currentPlayer shouldBe Player1

  it should "not be over" in:
    playBothFirstTurns(Game(boardTest)).isOver shouldBe false

  it should "leave each player with 4 cards" in:
    val game = playBothFirstTurns(Game(boardTest))
    game.board.getField(Player1).length shouldBe 4
    game.board.getField(Player2).length shouldBe 4

  it should "have no cactusCaller set" in:
    playBothFirstTurns(Game(boardTest)).cactusCaller shouldBe None

  "Playing phase" should "alternate to Player2 after Player1 plays" in:
    playSimpleTurn(gameInPlaying).currentPlayer shouldBe Player2

  it should "alternate back to Player1 after Player2 plays" in:
    playSimpleTurn(playSimpleTurn(gameInPlaying)).currentPlayer shouldBe Player1

  it should "remain in Playing if no cactus is called" in:
    playSimpleTurn(gameInPlaying).phase shouldBe Playing

  it should "transition to LastTurn when a player calls Cactus" in:
    gameInLastTurn.phase shouldBe LastTurn

  it should "record the correct cactusCaller" in:
    gameInLastTurn.cactusCaller shouldBe Some(Player1)

  it should "assign the last turn to the opponent of who called Cactus" in:
    gameInLastTurn.currentPlayer shouldBe Player2

  it should "return None on act when the game is over" in:
    Game(boardTest).copy(phase = Over).act(Observe) shouldBe None

  "LastTurn phase" should "transition to Over after the last turn is played" in:
    gameOver.phase shouldBe Over

  it should "be over after the last turn" in:
    gameOver.isOver shouldBe true

  "finalCards" should "return None before the game is over" in:
    Game(boardTest).finalCards shouldBe None

  it should "return Some with cards for both players when the game is over" in:
    gameOver.finalCards shouldBe defined

  it should "contain entries for both players" in:
    gameOver.finalCards.get.keySet shouldBe Set(Player1, Player2)

  it should "return 4 cards per player" in:
    val cards = gameOver.finalCards.get
    println(cards(Player1))
    println(cards(Player2))
    cards(Player1).length shouldBe 4
    cards(Player2).length shouldBe 4

  "playerScore" should "return a score for each player" in:
    gameOver.playerScore.keySet shouldBe Set(Player1, Player2)

  it should "be consistent with finalCards at game over" in:
    val cards = gameOver.finalCards.get
    val scores = gameOver.playerScore
    scores(Player1) shouldBe cards(Player1).map(_.value).sum
    scores(Player2) shouldBe cards(Player2).map(_.value).sum

  it should "be available before the game is over" in:
    gameInPlaying.playerScore.keySet shouldBe Set(Player1, Player2)
