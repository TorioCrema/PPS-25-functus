package org.pps.functus
package view

import view.{GameState, InputMode, Key}
import view.CardRenderExtensions.*
import org.jline.keymap.{BindingReader, KeyMap}
import org.jline.terminal.{Terminal, TerminalBuilder}
import org.jline.utils.InfoCmp.Capability

class CLIView:
  private val terminal: Terminal = TerminalBuilder.builder().system(true).build()
  private val bindingReader = new BindingReader(terminal.reader())
  private val keyMap = new KeyMap[Key]()

  // ANSI Code for console color
  private val ANSI_RESET = "\u001B[0m"
  private val ANSI_GREEN_BOLD = "\u001B[1;32m"

  private val HEADER_ART = List(
    "    /$$$$$$$$                              /$$                            ",
    "   | $$_____/                             | $$                           ",
    "   | $$    /$$   /$$ /$$$$$$$   /$$$$$$$ /$$$$$$   /$$   /$$  /$$$$$$$   ",
    "   | $$$$$| $$  | $$| $$__  $$ /$$_____/|_  $$_/  | $$  | $$ /$$_____/   ",
    "  | $$__/| $$  | $$| $$  \\ $$| $$        | $$    | $$  | $$|  $$$$$$   ",
    "  | $$   | $$  | $$| $$  | $$| $$        | $$ /$$| $$  | $$ \\____  $$  ",
    "   | $$   |  $$$$$$/| $$  | $$|  $$$$$$$  |  $$$$/|  $$$$$$/ /$$$$$$$/   ",
    " |__/    \\______/ |__/  |__/ \\_______/   \\___/   \\______/ |_______/   "
  )

  /** * init the terminal to be ready to print the game board and bind the keyboard keys to the to Key enum
    */
  def init(): Unit =
    terminal.enterRawMode()
    terminal.puts(Capability.cursor_invisible)
    terminal.puts(Capability.keypad_xmit)

    // Key binding for     Linux/macOS     ZSH              WINDOWS
    keyMap.bind(Key.UP, "   \u001b[A", "\u001bOA", KeyMap.key(terminal, Capability.key_up))
    keyMap.bind(Key.DOWN, " \u001b[B", "\u001bOB", KeyMap.key(terminal, Capability.key_down))
    keyMap.bind(Key.RIGHT, "\u001b[C", "\u001bOC", KeyMap.key(terminal, Capability.key_right))
    keyMap.bind(Key.LEFT, " \u001b[D", "\u001bOD", KeyMap.key(terminal, Capability.key_left))
    keyMap.bind(Key.ESCAPE, "q", "Q")
    keyMap.bind(Key.ENTER, "\r", "\n")

    keyMap.setAmbiguousTimeout(100)

  /** * restore the terminal to default value
    */
  def restore(): Unit =
    terminal.puts(Capability.cursor_visible)
    terminal.puts(Capability.keypad_local)
    terminal.close()

  /** * bind the input received by the terminal with the known keys
    * @return
    *   the key pressed or Key.UNKOWN if is not bind
    */
  def readInput(): Key =
    val key = bindingReader.readBinding(keyMap)
    if key == null then Key.UNKNOWN else key

  /** * render on the terminal the board in this order Header adversary field draw and discard pile player field hand
    * zone action list
    * @param gameState
    *   the actual gameState with all the information to be printed
    */
  def render(gameState: GameState): Unit =
    val length = terminal.getColumns
    terminal.puts(Capability.clear_screen)
    terminal.flush()
    val viewBuilder = StringBuilder()
    val separator = "_" * length

    // Header
    HEADER_ART.foreach { line =>
      viewBuilder
        .append(ANSI_GREEN_BOLD)
        .append(centerText(line, length))
        .append(ANSI_RESET)
        .append("\n")
    }

    gameState.inputMode match
      case InputMode.WaitingRoom =>
        terminal.puts(Capability.clear_screen)
        terminal.flush()
        val transitionBlock = StringBuilder()
        transitionBlock.append(s"$separator\n")
        transitionBlock.append(centerText("PLAYER SWAP", length)).append("\n\n")
        transitionBlock.append(centerText("Make sure the other player isn't watching!", length)).append("\n\n")
        transitionBlock.append(centerText("[ Press ENTER to begin the turn ]", length)).append("\n")
        transitionBlock.append(s"$separator\n")

        // center vertically the changing player block
        val blockString = transitionBlock.toString()
        val blockHeight = blockString.linesIterator.length
        val terminalHeight = Option(terminal.getRows).filter(_ > 0).getOrElse(24)
        val topPadding = Math.max(0, (terminalHeight - blockHeight) / 2) - 10
        viewBuilder.append("\n" * topPadding)
        viewBuilder.append(blockString)

      case _ =>
        // Adversary Card Zone
        viewBuilder.append(s"$separator\n")
        viewBuilder.append(centerText("ADVERSARY", length)).append("\n\n")
        val selectedAdversaryIndex =
          if gameState.inputMode == InputMode.SelectAdversaryCardOnBoard then Some(gameState.selectedCardOnBoard)
          else None

        val adversaryLines = gameState.adversaryCard.toAsciiRows(
          terminalWidth = length,
          selectedIdx = selectedAdversaryIndex
        )
        adversaryLines.foreach(line => viewBuilder.append(centerText(line, length)).append("\n"))
        viewBuilder.append("\n")

        // Central Zone: Deck and Discard Pile
        val deckLines =
          None.toAsciiLines(label = Some(s"░░░░$ANSI_GREEN_BOLD${gameState.remainingCardInDeck}$ANSI_RESET░░░░░"))
        val discardLines = gameState.lastDiscardedCard.toAsciiLines(
          label = if gameState.lastDiscardedCard.isEmpty then Some("EMPTY") else None
        )
        val centerLines = List(deckLines, discardLines).joinHorizontally(
          spacers = List(" DECK          DISCARD ")
        )
        centerLines.foreach(line => viewBuilder.append(centerText(line, length)).append("\n"))
        viewBuilder.append("\n")

        // Player Card Zone
        val selectedIdx =
          if gameState.inputMode == InputMode.SelectCardOnBoard then Some(gameState.selectedCardOnBoard)
          else None

        val playerLines = gameState.playerCard.toAsciiRows(
          terminalWidth = length,
          selectedIdx = selectedIdx,
          lastChangedIdx = gameState.lastChangedPlayerCard
        )
        playerLines.foreach(line => viewBuilder.append(centerText(line, length)).append("\n"))
        viewBuilder.append(centerText("PLAYER", length)).append("\n")

        viewBuilder.append(s"$separator\n")

        // Hand Zone
        viewBuilder.append(centerText("CARD IN HAND:", length)).append("\n")
        val handLines = if gameState.cardsInHand.flatten.isEmpty then List("[ No card drawn ]")
        else gameState.cardsInHand.toAsciiRows(terminalWidth = length)

        handLines.foreach(line => viewBuilder.append(centerText(line, length)).append("\n"))

        viewBuilder.append(s"$separator\n")

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

        viewBuilder.append(s"\n (Press 'Q' to exit)\n")

    print(viewBuilder.toString())

  /** * center the given text on the terminal
    * @param text
    *   the text to be centered
    * @param length
    *   the width of the terminal
    * @return
    *   the padded string with enough blank spaces to be printed at the center of the terminal
    */
  private def centerText(text: String, length: Int): String =
    val visualLen =
      // regex to search and delete all ANSI color commands to ensure correct visual length measurement
      text.replaceAll("\u001B\\[[;\\d]*m", "").length
    val space = Math.max(0, (length - visualLen) / 2)
    " " * space + text
