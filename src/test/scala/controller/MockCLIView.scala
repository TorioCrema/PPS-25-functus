package org.pps.functus
package controller

import view.{CLIView, GameState, Key}
import scala.collection.mutable

/** Mock / Stub della CLIView da utilizzare esclusivamente nei test.
 */
class MockCLIView extends CLIView:
  private val inputQueue = mutable.Queue[Key]()
  var lastRenderedState: Option[GameState] = None
  var initCalled: Boolean = false
  var restoreCalled: Boolean = false

  // Inserisce i tasti da simulare durante la partita
  def enqueueInputs(keys: Key*): Unit =
    inputQueue.enqueueAll(keys)

  override def init(): Unit =
    initCalled = true

  override def restore(): Unit =
    restoreCalled = true

  override def render(state: GameState): Unit =
    lastRenderedState = Some(state)

  override def readInput(): Key =
    if inputQueue.nonEmpty then inputQueue.dequeue()
    else Key.ESCAPE // Se finiscono gli input simulati, invia ESCAPE per uscire dal loop del controller