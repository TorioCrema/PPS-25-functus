package org.pps.functus
package view

import view.{GameState, InputMode, Key}
import model.deck.card.Card
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
    "   /$$$$$$                        /$$                           ",
    " / $$__  $$                      | $$                         ",
    "| $$  \\__/  /$$$$$$   /$$$$$$$ /$$$$$$   /$$   /$$  /$$$$$$$",
    "| $$       |____  $$ /$$_____/|_  $$_/  | $$  | $$ /$$_____/",
    "| $$        /$$$$$$$| $$        | $$    | $$  | $$|  $$$$$$",
    "| $$    $$ /$$__  $$| $$        | $$ /$$| $$  | $$ \\____  $$",
    "|  $$$$$$/|  $$$$$$$|  $$$$$$$  |  $$$$/|  $$$$$$/ /$$$$$$$/",
    "\\______/  \\_______/ \\_______/   \\___/   \\______/ |_______/"
  )

  /** * init the terminal to be ready to print the game board and bind the keyboard keys to the to Key enum
    */
  def init(): Unit =
    terminal.enterRawMode()
    terminal.puts(Capability.cursor_invisible)
    terminal.puts(Capability.keypad_xmit) // for Linux and macOS terminal

    // Key binding for     Linux/macOS      ZSH              WINDOWS
    keyMap.bind(Key.UP, "\u001b[A", "\u001bOA", KeyMap.key(terminal, Capability.key_up))
    keyMap.bind(Key.DOWN, "\u001b[B", "\u001bOB", KeyMap.key(terminal, Capability.key_down))
    keyMap.bind(Key.RIGHT, "\u001b[C", "\u001bOC", KeyMap.key(terminal, Capability.key_right))
    keyMap.bind(Key.LEFT, "\u001b[D", "\u001bOD", KeyMap.key(terminal, Capability.key_left))

    keyMap.bind(Key.ESCAPE, "q", "Q")
    keyMap.bind(Key.ENTER, "\r", "\n")

    keyMap.setAmbiguousTimeout(100)

  /** * restore the terminal to default value
    */
  def restore(): Unit =
    terminal.puts(Capability.cursor_visible)
    terminal.puts(Capability.keypad_local) // restore terminal
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

    HEADER_ART.foreach { line =>
      viewBuilder
        .append(ANSI_GREEN_BOLD)
        .append(centerText(line, length))
        .append(ANSI_RESET)
        .append("\n")
    }

    // Adversary Card Zone

    viewBuilder.append(s"$separator\n")
    viewBuilder.append(centerText("ADVERSARY", length)).append("\n")
    viewBuilder.append(centerText(renderCard(gameState.adversaryCard), length)).append("\n\n")

    // Central Zone: Deck and Discard Pile
    val strDeck = s"[H (${gameState.remainingCardInDeck})]"
    val strDiscard = gameState.lastDiscardedCard.fold("[Empty]")(card => s"[$card]")
    val centerOfTheBoard = s"DECK: $strDeck   │   DISCARD: $strDiscard"
    viewBuilder.append(centerText(centerOfTheBoard, length)).append("\n\n")

    // Player Card Zone
    val playerCardStr = renderPlayerCard(gameState)
    viewBuilder.append(centerText(playerCardStr, length)).append("\n")
    viewBuilder.append(centerText("PLAYER", length)).append("\n")

    viewBuilder.append(s"$separator\n")

    // Hand Zone
    val strHand = gameState.cardsInHand.collect { case Some(card) => s"[$card]" } match
      case Nil   => "No card drawn"
      case cards => cards.mkString(" ")

    viewBuilder.append(centerText(s"CARD IN HAND: $strHand", length)).append("\n")

    viewBuilder.append(s"$separator\n")

    // Menu Zone / Input Reactive
    if gameState.inputMode == InputMode.ActionMenu then
      viewBuilder.append(" AVAILABLE ACTIONS (Use ↑/↓ and Press ENTER):\n")
      if gameState.possibleAction.isEmpty then viewBuilder.append(" (No possible action)").append("\n")
      else
        gameState.possibleAction.zipWithIndex.foreach { case (action, i) =>
          val current = if i == gameState.selectedAction then " -> " else "   "
          viewBuilder.append(s"$current${i + 1}. ${action.label}\n")
        }
    else
      viewBuilder.append(" SELECT A CARD ON THE BOARD (Use ←/→ and press ENTER to exchange):\n")
      viewBuilder.append(s"   Selected Card: Position ${gameState.selectedCardOnBoard + 1}\n")

    viewBuilder.append(s"\n (Press 'Q' to exit)\n")

    print(viewBuilder.toString())

  /** * draw the adversary cards
    * @param cards
    *   List of cards of the opponent
    * @return
    *   the formatted string ready to be printed on the terminal
    */
  private def renderCard(cards: List[Option[Card]]): String =
    cards.map(_.fold("[H]")(card => s"[$card]")).mkString(" ")

  /** * draw the player cards and if the state is selectCardOnBoard drow a "< >" around the card for let the user know
    * which card is selecting
    * @param state
    *   the actual GameState containing all the information to be printed
    * @return
    *   the formatted string ready to be printed on the terminal
    */
  private def renderPlayerCard(state: GameState): String =
    state.playerCard.zipWithIndex
      .map { case (optCard, index) =>
        val cardStr = optCard.fold("H")(
          _.toString
        ) // only to demonstrate the working exchange of the cards in final version all card were be hidden = [H]
        val cardSelected = state.inputMode == InputMode.SelectCardOnBoard && index == state.selectedCardOnBoard
        if cardSelected then s"<[$cardStr]>" else s"[$cardStr]"

      }
      .mkString(" ")

  /** * center the given text on the terminal
    * @param text
    *   the text to be centered
    * @param length
    *   the width of the terminal
    * @return
    *   the padded string with enough blank spaces to be printed at the center of the terminal
    */
  private def centerText(text: String, length: Int): String =
    val space = Math.max(0, (length - text.length) / 2)
    " " * space + text
