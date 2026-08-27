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

  var length: Int = terminal.getColumns

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

  var readInputProvider: () => Key = () => {
    Key.UNKNOWN
  }

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
    val key = bindingReader.readBinding(keyMap)
    if key == null then Key.UNKNOWN else key

  /** * center the given text on the terminal
    *
    * @param text
    *   the text to be centered
    * @param length
    *   the width of the terminal
    * @return
    *   the padded string with enough blank spaces to be printed at the center of the terminal
    */
  def centerText(text: String): String =
    length = terminal.getColumns
    // regex to search and delete all ANSI color commands to ensure correct visual length measurement
    val visualLen = text.replaceAll("\u001B\\[[;\\d]*m", "").length
    val space = Math.max(0, (length - visualLen) / 2)
    " " * space + text

  def clearScreen(): Unit =
    Try {
      if (terminal != null) {
        viewBuilder.clear()
        terminal.puts(Capability.clear_screen)
        terminal.flush()
      }
    }.getOrElse {
      // Fallback ANSI clear screen se JLine è disabilitato/chiuso durante i test
      print("\u001b[H\u001b[2J")
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
