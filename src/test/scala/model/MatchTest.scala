package org.pps.functus
package model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import model.board.Player.*
import model.deck.card.Suit.*
import model.deck.sugar.CardDSL.{*, given}
import model.deck.sugar.BoardDSL.*
import model.deck.sugar.DeckDSL
import model.deck.sugar.DeckDSL.deck.*
import model.deck.sugar.FieldDSL.{*, given}
import model.board.Board
import model.deck.Deck
import model.playable.game.{GamePhase, Match}
import model.playable.turn.Action.*

class MatchTest extends AnyFlatSpec with Matchers:
  private val threshold = 10

  private def deckForFirstPlayerWin = DeckDSL.deck from ((jack of Cups) | (knight of Cups))
  private def deckForSecondPlayerWin = DeckDSL.deck from ((knight of Cups) | (jack of Cups))
  private def deckForFirstPlayerScoreReset = DeckDSL.deck from ((ace of Wands) | (jack of Swords))
  private def deckForSecondGame = DeckDSL.deck from ((ace of Wands) | (ace of Pentacles))

  private def boardFromDeck(deck: Deck): Board =
    val p1Field = (1 of Cups) and (2 of Cups) and (3 of Cups) and (4 of Cups)
    val p2Field = (1 of Swords) and (2 of Swords) and (3 of Swords) and (4 of Swords)
    (lockedBoard from default)
      .withCustom(playerOne(p1Field))
      .withCustom(playerTwo(p2Field))
      .withCustom(discardPile((5 of Cups) | (6 of Cups)))
      .withCustom(customDeck(deck))

  private def playFirstTurn(game: Match): Match =
    game.actAll(Observe :: Confirm :: EndTurn :: Nil)

  private def playBothFirstTurns(game: Match): Match =
    playFirstTurn(playFirstTurn(game))

  /** Plays one SimpleTurn without calling Cactus. */
  private def playSimpleTurn(game: Match): Match =
    game.actAll(Draw :: Activate :: ChooseReplace(0) :: EndTurn :: Nil)

  /** Plays one SimpleTurn and calls Cactus at the end. */
  private def playSimpleTurnWithCactus(game: Match): Match =
    game.actAll(Draw :: Activate :: ChooseReplace(0) :: Cactus :: EndTurn :: Nil)

  private def gameInPlaying(board: Board): Match = playBothFirstTurns(Match(threshold, board))

  private def gameInLastTurn(board: Board): Match = playSimpleTurnWithCactus(gameInPlaying(board))

  private def gameOver(startBoard: Board): Match = playSimpleTurn(gameInLastTurn(startBoard))

  "Match" should "start with given threshold and player scores at 0" in:
    val startingMatch = Match(threshold, board from default)
    startingMatch.maxScore should be(threshold)
    startingMatch.scores(Player1) should be(0)
    startingMatch.scores(Player2) should be(0)

  it should "start a new game when created" in:
    val startingMatch = Match(threshold, board from default)
    startingMatch.isOver should be(false)
    startingMatch.game.phase should be(GamePhase.FirstTurns)

  it should "accumulate player scores and start new game after a game ends" in:
    val over = gameOver(boardFromDeck(deckForFirstPlayerWin))
    over.game.isOver should be(true)
    over.isOver should be(true)
    over.scores(Player1) should be(
      over.game.currentTurn.board.players(Player1).cardsList.foldLeft(0)((acc, card) => acc + card.value)
    )
    over.scores(Player2) should be(
      over.game.currentTurn.board.players(Player2).cardsList.foldLeft(0)((acc, card) => acc + card.value)
    )

  it should "cut a player's score in half if their score is equal to the threshold" in:
    val over = gameOver(boardFromDeck(deckForFirstPlayerScoreReset))
    over.scores(Player1) should be(threshold / 2)

  it should "start a new game and maintain scores" in:
    val firstGameOver = gameOver(boardFromDeck(deckForSecondGame))
    firstGameOver.game.isOver should be(true)
    firstGameOver.isOver should be(false)
    val secondGame = firstGameOver.nextGame
    secondGame.scores should be(firstGameOver.scores)
    secondGame.game.isOver should be(false)

  it should "throw IllegalStateException when attempting to start a new match" in:
    val unfinished = Match(threshold, boardFromDeck(deckForFirstPlayerWin))
    an[IllegalStateException] should be thrownBy unfinished.nextGame
