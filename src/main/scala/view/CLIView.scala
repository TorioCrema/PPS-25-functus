package org.pps.functus
package view

import view.{GameState, InputMode}
import view.CardRenderExtensions.*
import view.utils.Utils
import view.utils.Utils.{ANSI_GREEN_BOLD, ANSI_RESET, SEPARATOR_CHAR, terminalWidth, viewBuilder}

import scala.util.Try

class CLIView:

  /** * render on the terminal the board in this order Header adversary field draw and discard pile player field hand
    * zone action list
    * @param state
    *   the actual gameState with all the information to be printed
    */
  def render(state: GameState): Unit =
    given gameState: GameState = state
    given separator: String = SEPARATOR_CHAR * terminalWidth

    Utils.viewBuilder.clear()
    Utils.clearScreen()
    Utils.drawHeader

    gameState.inputMode match
      case InputMode.EndGame =>
        drawAdversaryField
        drawDeckAndDiscard
        drawPlayerField
        drawEndGameMessage

      case InputMode.WaitingRoom =>
        drawWaitingRoom

      case _ =>
        drawAdversaryField
        drawDeckAndDiscard
        drawPlayerField
        drawHandZone
        drawActionMenu

    print(viewBuilder.toString())

  private def drawAdversaryField(using
      viewBuilder: StringBuilder,
      gameState: GameState,
      separator: String
  ) =
    // Adversary Card Zone
    viewBuilder.append(s"$separator\n\n")
    viewBuilder.append(Utils.centerText("ADVERSARY", terminalWidth)).append("\n\n")
    val selectedAdversaryIndex =
      if gameState.inputMode == InputMode.SelectAdversaryCardOnBoard then Some(gameState.selectedCardOnBoard)
      else None

    val adversaryLines = gameState.adversaryCard.toAsciiRows(
      terminalWidth = terminalWidth,
      selectedIdx = selectedAdversaryIndex
    )
    adversaryLines.foreach(line => viewBuilder.append(Utils.centerText(line)).append("\n"))
    viewBuilder.append("\n")

  private def drawDeckAndDiscard(using
      viewBuilder: StringBuilder,
      gameState: GameState,
      separator: String
  ) =
    // Central Zone: Deck and Discard Pile
    val deckLines =
      None.toAsciiLines(label = Some(s"░░░░$ANSI_GREEN_BOLD${gameState.remainingCardInDeck}$ANSI_RESET░░░░░"))
    val discardLines = gameState.lastDiscardedCard.toAsciiLines(
      label = if gameState.lastDiscardedCard.isEmpty then Some("EMPTY") else None
    )
    val centerLines = List(deckLines, discardLines).joinHorizontally(
      spacers = List(" DECK          DISCARD ")
    )
    centerLines.foreach(line => viewBuilder.append(Utils.centerText(line)).append("\n"))
    viewBuilder.append("\n")

  private def drawPlayerField(using viewBuilder: StringBuilder, gameState: GameState, separator: String) =
    // Player Card Zone
    val selectedIdx =
      if gameState.inputMode == InputMode.SelectCardOnBoard then Some(gameState.selectedCardOnBoard)
      else None

    val playerLines = gameState.playerCard.toAsciiRows(
      terminalWidth = terminalWidth,
      selectedIdx = selectedIdx
    )
    playerLines.foreach(line => viewBuilder.append(Utils.centerText(line)).append("\n"))
    viewBuilder.append(Utils.centerText("PLAYER")).append("\n")

    viewBuilder.append(s"$separator\n")

  private def drawEndGameMessage(using
      viewBuilder: StringBuilder,
      gameState: GameState,
      separator: String
  ) =
    val transitionBlock = StringBuilder()
    transitionBlock.append(s"$separator\n")
    transitionBlock.append(Utils.centerText("END GAME")).append("\n\n")
    if gameState.winner.isEmpty then
      transitionBlock.append(Utils.centerText(s"GAME IS ENDED IN A TIE ")).append("\n\n")
    else transitionBlock.append(Utils.centerText(s"GAME IS OVER ${gameState.winner.get} WIN ")).append("\n\n")
    transitionBlock
      .append(
        Utils.centerText(
          s"Player 1 has done ${gameState.playerScore} points | Player 2 has done ${gameState.adversaryScore} points "
        )
      )
      .append("\n\n")
    transitionBlock.append(Utils.centerText("[ Press Q or ENTER to return to main Menu ]")).append("\n")
    transitionBlock.append(s"$separator\n")

    viewBuilder.append(transitionBlock)

  private def drawWaitingRoom(using viewBuilder: StringBuilder, separator: String) =
    val transitionBlock = StringBuilder()
    transitionBlock.append(s"$separator\n")
    transitionBlock.append(Utils.centerText("PLAYER SWAP")).append("\n\n")
    transitionBlock.append(Utils.centerText("Make sure the other player isn't watching!")).append("\n\n")
    transitionBlock.append(Utils.centerText("[ Press ENTER to begin the turn ]")).append("\n")
    transitionBlock.append(s"$separator\n")

    // center vertically the changing player block
    val blockString = transitionBlock.toString()
    val blockHeight = blockString.linesIterator.length
    val terminalHeight = Try(Utils.terminal.getRows).toOption.filter(_ > 0).getOrElse(24)
    val topPadding = Math.max(0, (terminalHeight - blockHeight) / 2) - 10
    viewBuilder.append("\n" * topPadding)
    viewBuilder.append(blockString)

  private def drawHandZone(using
      viewBuilder: StringBuilder,
      gameState: GameState,
      separator: String
  ) = // Hand Zone
    viewBuilder.append(Utils.centerText("CARD IN HAND:")).append("\n")
    val handLines = if gameState.cardsInHand.flatten.isEmpty then List("[ No card drawn ]")
    else gameState.cardsInHand.toAsciiRows(terminalWidth = terminalWidth)

    handLines.foreach(line => viewBuilder.append(Utils.centerText(line)).append("\n"))

    viewBuilder.append(s"$separator\n")

  private def drawActionMenu(using viewBuilder: StringBuilder, gameState: GameState, separator: String) =
    // Menu / Action Zone
    gameState.inputMode match
      case InputMode.ActionMenu =>
        viewBuilder.append(" AVAILABLE ACTIONS (Use ↑/↓ and Press ENTER):\n")
        if gameState.possibleAction.isEmpty then viewBuilder.append(" (No possible action)\n")
        else
          gameState.possibleAction.zipWithIndex.foreach { case (action, i) =>
            val current = if i == gameState.selectedAction then " -> " else "    "
            viewBuilder.append(s"$current${i + 1}. ${action.label}\n")
          }
      case InputMode.SelectCardOnBoard =>
        viewBuilder.append(" SELECT A CARD ON THE BOARD (Use ←/→ and press ENTER to exchange):\n")
        viewBuilder.append(s" Selected Card: Position ${gameState.selectedCardOnBoard + 1}\n")
      case InputMode.SelectAdversaryCardOnBoard =>
        viewBuilder.append(" SELECT A CARD ON ADVERSARY BOARD (Use ←/→ and press ENTER to exchange):\n")
        viewBuilder.append(s" Selected Card: Position ${gameState.selectedCardOnBoard + 1}\n")
      case InputMode.WaitingRoom => ()
      case InputMode.EndGame     => ()

    viewBuilder.append(s"\n (Press 'Q' to return to main menu)\n")
