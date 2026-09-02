package org.pps.functus
package model.showcase

import model.board.Board
import model.playable.turn.Turn
import model.playable.turn.Turns.SimpleTurn
import model.board.Player.Player1
import model.showcase.Configurations.*

private trait ShowcaseBoard:
  def board: Board

private trait SixEffect extends ShowcaseBoard:
  override def board: Board = boardForSixEffect

private trait SevenEffect extends ShowcaseBoard:
  override def board: Board = boardForSevenEffect

private trait JackEffect extends ShowcaseBoard:
  override def board: Board = boardForJackEffect

private trait KingDraw extends ShowcaseBoard:
  override def board: Board = boardForKingDraw

private trait SuccessfulDiscard extends ShowcaseBoard:
  override def board: Board = boardForSuccessfulDiscard

private trait FailedDiscard extends ShowcaseBoard:
  override def board: Board = boardForFailedDiscard

/** Trait that generates [[Turn]]s used to showcase certain mechanics of the game.
  */
trait Showcase:
  /** Returns the [[Turn]] of the [[Showcase]].
    */
  def turn: Turn

abstract class AbstractShowcase extends Showcase, ShowcaseBoard:
  def turn: Turn = SimpleTurn(board, Player1)

/** [[Showcase]] for the effect of the six card.
  */
object SixShowcase extends AbstractShowcase with SixEffect

/** [[Showcase]] for the effect of the seven card.
  */
object SevenShowcase extends AbstractShowcase with SevenEffect

/** [[Showcase]] for the effect of the jack card.
  */
object JackShowcase extends AbstractShowcase with JackEffect

/** [[Showcase]] for the game mechanic of drawing the king card from the discard pile.
  */
object KingDrawShowcase extends AbstractShowcase with KingDraw

/** [[Showcase]] for the successful discard game mechanic.
  */
object SuccessfulDiscardShowcase extends AbstractShowcase with SuccessfulDiscard

/** [[Showcase]] for the failed discard game mechanic.
  */
object FailedDiscardShowcase extends AbstractShowcase with FailedDiscard
