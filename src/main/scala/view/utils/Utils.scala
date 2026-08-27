package org.pps.functus
package view.utils

import org.jline.keymap.{BindingReader, KeyMap}
import org.jline.terminal.{Terminal, TerminalBuilder}
import org.jline.utils.InfoCmp.Capability
import view.Key

import scala.util.Try

object Utils:

  val terminal: Terminal = TerminalBuilder.builder().system(true).build()
  private val bindingReader = new BindingReader(terminal.reader())
  private val keyMap = new KeyMap[Key]()

  // ANSI Code for console color
  val ANSI_RESET = "\u001B[0m"
  val ANSI_GREEN_BOLD = "\u001B[1;32m"
  val SEPARATOR_CHAR = "_"

  given viewBuilder: StringBuilder = StringBuilder()

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

  // Recupero sicuro delle dimensioni del terminale con fallback per i test
  def terminalWidth: Int =
    Try(terminal.getColumns).toOption.filter(_ > 0).getOrElse(120)

  def terminalHeight: Int =
    Try(terminal.getRows).toOption.filter(_ > 0).getOrElse(120)

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
    clearScreen()
    terminal.puts(Capability.cursor_visible)
    terminal.puts(Capability.keypad_local)
    terminal.close()

  /** * bind the input received by the terminal with the known keys
    *
    * @return
    *   the key pressed or Key.UNKOWN if is not bind
    */
  def readInput(): Key =
    Try {
      val key = bindingReader.readBinding(keyMap)
      if key == null then Key.UNKNOWN else key
    }.getOrElse(Key.UNKNOWN)

  /** * center the given text on the terminal
    *
    * @param text
    *   the text to be centered
    * @return
    *   the padded string with enough blank spaces to be printed at the center of the terminal
    */
  def centerText(text: String, targetLength: Int = terminalWidth): String =
    val visualLen = text.replaceAll("\u001B\\[[;\\d]*m", "").length
    val space = Math.max(0, (targetLength - visualLen) / 2)
    " " * space + text

  def clearScreen(): Unit = {
    viewBuilder.clear()
    Try {
      if terminal != null then
        viewBuilder.clear()
        terminal.puts(Capability.clear_screen)
        terminal.flush()
    }.getOrElse {
      // Fallback ANSI clear screen if JLine terminal is disabled/close during test
      print("\u001b[H\u001b[2J")
    }
  }

  def drawHeader(using viewBuilder: StringBuilder): Unit =
    // Header
    viewBuilder.append("\n\n")
    HEADER_ART.foreach { line =>
      viewBuilder
        .append(ANSI_GREEN_BOLD)
        .append(centerText(line))
        .append(ANSI_RESET)
        .append("\n")
    }
