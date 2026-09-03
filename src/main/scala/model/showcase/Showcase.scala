package org.pps.functus
package model.showcase

import model.board.Board
import model.playable.turn.Turn
import model.playable.turn.Turns.SimpleTurn
import model.board.Player.Player1
import model.showcase.Configurations.*

trait ShowcaseBoard:
  def apply(): Board

trait SixEffect extends ShowcaseBoard:
  override def apply(): Board = boardForSixEffect

trait SevenEffect extends ShowcaseBoard:
  override def apply(): Board = boardForSevenEffect

trait JackEffect extends ShowcaseBoard:
  override def apply(): Board = boardForJackEffect

trait KingDraw extends ShowcaseBoard:
  override def apply(): Board = boardForKingDraw

trait SuccessfulDiscard extends ShowcaseBoard:
  override def apply(): Board = boardForSuccessfulDiscard

trait FailedDiscard extends ShowcaseBoard:
  override def apply(): Board = boardForFailedDiscard

/** Trait that generates [[Turn]]s used to showcase certain mechanics of the game.
  */
trait Showcase:
  board: ShowcaseBoard =>

  /** Returns the [[Turn]] of the [[Showcase]].
    */
  def turn: Turn = SimpleTurn(board(), Player1)

/** [[Showcase]] for the effect of the six card.
  */
object SixShowcase extends Showcase with SixEffect

/** [[Showcase]] for the effect of the seven card.
  */
object SevenShowcase extends Showcase with SevenEffect

/** [[Showcase]] for the effect of the jack card.
  */
object JackShowcase extends Showcase with JackEffect

/** [[Showcase]] for the game mechanic of drawing the king card from the discard pile.
  */
object KingDrawShowcase extends Showcase with KingDraw

/** [[Showcase]] for the successful discard game mechanic.
  */
object SuccessfulDiscardShowcase extends Showcase with SuccessfulDiscard

/** [[Showcase]] for the failed discard game mechanic.
  */
object FailedDiscardShowcase extends Showcase with FailedDiscard
