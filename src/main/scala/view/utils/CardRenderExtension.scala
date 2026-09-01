package org.pps.functus
package view.utils

import model.deck.card.Suit.{Cups, Pentacles, Swords, Wands}
import model.deck.card.{Card, Suit}

/** Represents the border highlight mode for a card */
enum BorderStyle:
  case Normal, Selected, LastChanged

object CardRenderExtensions:

  // ANSI Colors for Suits and Borders
  private val ANSI_RESET = "\u001B[0m"
  private val ANSI_CYAN_BOLD = "\u001B[1;36m"
  private val ANSI_YELLOW_BOLD = "\u001B[1;33m"

  private val ANSI_SWORD = "\u001B[1;36m" // Bold Cyan for Swords
  private val ANSI_CUP = "\u001B[1;31m" // Bold Red for Cups
  private val ANSI_CLUB = "\u001B[1;32m" // Bold Green for Clubs / Wands
  private val ANSI_COIN = "\u001B[1;33m" // Bold Yellow for Coins / Pentacles

  extension (cardOpt: Option[Card])

    /** Converts an optional card into a 7-line ASCII visual block for terminal rendering.
      *
      * Generates a styled 13x7 box representation containing colored rank and suit text for face-up cards
      * (`Some(card)`), or a shaded back side (`None`) with an optional label centered inside it.
      *
      * @param borderStyle
      *   The highlight mode determining the card's border color (Normal, Selected, or LastChanged).
      * @param label
      *   An optional text string (e.g., "EMPTY" or "12") to display inside a hidden card or card back.
      * @return
      *   A list of 7 strings, where each element represents one horizontal row of the rendered card box.
      */
    def toAsciiLines(borderStyle: BorderStyle = BorderStyle.Normal, label: Option[String] = None): List[String] =
      val bColor = borderStyle match
        case BorderStyle.Selected    => ANSI_CYAN_BOLD
        case BorderStyle.LastChanged => ANSI_YELLOW_BOLD
        case BorderStyle.Normal      => ""

      val reset = ANSI_RESET

      // Fixed Inner Width = 11 characters (Total width = 13)
      val top = s"$bColor┌───────────┐$reset"
      val bottom = s"$bColor└───────────┘$reset"
      val blank = s"$bColor│$reset           $bColor│$reset"

      cardOpt match
        case Some(card) =>
          val (value, suit) = extractCardDetails(card)
          val sColor = suitColor(card.suit)

          val leftRank = padRight(value, 2)
          val rightRank = padLeft(value, 2)
          val centeredSuit = padCenter(suit, 11)

          List(
            top,
            s"$bColor│$reset$sColor$leftRank$reset         $bColor│$reset",
            blank,
            s"$bColor│$reset$sColor$centeredSuit$reset$bColor│$reset",
            blank,
            s"$bColor│$reset         $sColor$rightRank$reset$bColor│$reset",
            bottom
          )

        case None =>
          // Hidden card / Deck Back side (7 lines total)
          val shadeLine = s"$bColor│$reset░░░░░░░░░░░$bColor│$reset"
          val innerText = label.getOrElse("░░░░░░░░░░░")
          val paddedText = padCenter(innerText, 11)

          List(
            top,
            shadeLine,
            shadeLine,
            s"$bColor│$reset$paddedText$bColor│$reset",
            shadeLine,
            shadeLine,
            bottom
          )

  extension (cards: List[Option[Card]])

    /** Converts a list of card slots into multiple horizontal ASCII rows if the total width exceeds the terminal window
      * width
      *
      * @param terminalWidth
      *   The current width of the terminal window (e.g., terminal.getColumns)
      * @param selectedIdx
      *   Optional index of the card to highlight with the selection border
      * @param lastChangedIdx
      *   Optional index of the card to highlight with the last-modified border (relative to the global list)
      * @param spacing
      *   Horizontal whitespace gap inserted between adjacent cards on the same row. Defaults to " "
      * @return
      *   A list of strings representing the cards formatted across one or more multi-line ASCII rows
      */
    def toAsciiRows(
        terminalWidth: Int,
        selectedIdx: Option[Int] = None,
        lastChangedIdx: Option[Int] = None,
        spacing: String = " "
    ): List[String] =
      if cards.isEmpty then return List("[ No Cards ]")

      val cardWidth = 13 // fixed length of a single card
      val gapWidth = visualLength(spacing)

      // Calculate how many card enter in a single row of the terminal
      val maxCardsPerRow = Math.max(1, (terminalWidth + gapWidth) / (cardWidth + gapWidth))

      val indexedCards = cards.zipWithIndex
      val rowsOfCards = indexedCards.grouped(maxCardsPerRow).toList

      rowsOfCards.flatMap { cardGroup =>
        val cardBlocks = cardGroup.map { case (cardOpt, originalIdx) =>
          val style =
            if selectedIdx.contains(originalIdx) then BorderStyle.Selected
            else if lastChangedIdx.contains(originalIdx) then BorderStyle.LastChanged
            else BorderStyle.Normal

          cardOpt.toAsciiLines(borderStyle = style)
        }

        joinCardsHorizontally(cardBlocks, spacing = spacing) :+ ""
      }

  // Helper Methods
  /** Safely extracts value and suit string from the Card model */
  private def extractCardDetails(card: Card): (String, String) =
    val valueString = card.value match
      case 1 => "A"
      case 0 => "K"
      case v => v.toString
    (valueString, card.suit.toString)

  /** Returns suit-specific ANSI color string */
  private def suitColor(suit: Suit): String =
    suit match
      case Swords     => ANSI_SWORD
      case Cups       => ANSI_CUP
      case Wands      => ANSI_CLUB
      case Pentacles  => ANSI_COIN
      case null       => ANSI_RESET


  /** Merges multiple multi-line card ASCII blocks into a single horizontal list of printable lines.
    *
    * Concatenates each row of the given card blocks line-by-line to render them side-by-side. If spacer labels are
    * provided, their text is rendered exclusively on the middle line (index 3), while all other lines are padded with
    * equivalent whitespace to preserve box alignment.
    *
    * @param cardBlocks
    *   A list where each element is a list of strings representing a card's vertical lines.
    * @param spacers
    *   Optional list of text labels to place between adjacent card blocks on the middle row.
    * @param spacing
    *   The fixed horizontal space string inserted between neighboring cards. Defaults to " ".
    * @return
    *   A list of strings representing the fully aligned, horizontally joined lines of all card blocks.
    */
  private def joinCardsHorizontally(
      cardBlocks: List[List[String]],
      spacers: List[String] = Nil,
      spacing: String = " "
  ): List[String] =
    if cardBlocks.isEmpty then return Nil
    val height = cardBlocks.head.length
    val middleLineIdx = height / 2 // Line index 3 for 7-line cards

    (0 until height).toList.map { lineIdx =>
      cardBlocks.zipWithIndex
        .map { case (block, blockIdx) =>
          val cardLine = block(lineIdx)

          if blockIdx < cardBlocks.length - 1 && spacers.lift(blockIdx).isDefined then
            val spacerText = spacers(blockIdx)
            val currentSpacer = if lineIdx == middleLineIdx then spacerText else " " * visualLength(spacerText)
            s"$cardLine $currentSpacer "
          else cardLine
        }
        .mkString(spacing)
    }

  // Padding Utilities

  /** Aligns a string to the right within a fixed-width container by prepending leading spaces.
    *
    * @param s
    *   The target string to pad.
    * @param len
    *   The desired minimum visual width of the resulting string (The terminal width).
    * @return
    *   The right-aligned string padded with spaces, or the original string if its width already exceeds `len`.
    */
  private def padLeft(s: String, len: Int): String =
    " " * Math.max(0, len - visualLength(s)) + s

  /** Aligns a string to the left within a fixed-width container by prepending leading spaces.
    *
    * @param s
    *   The target string to pad.
    * @param len
    *   The desired minimum visual width of the resulting string (The terminal width).
    * @return
    *   The left-aligned string padded with spaces, or the original string if its width already exceeds `len`.
    */
  private def padRight(s: String, len: Int): String =
    s + " " * Math.max(0, len - visualLength(s))

  /** Centers a string within a fixed-width container by adding equal padding on both sides. Any leftover space from
    * odd-length padding is distributed to the right side.
    *
    * @param s
    *   The target string to center.
    * @param len
    *   The desired minimum visual width of the resulting string.
    * @return
    *   The centered string padded with spaces, or the original string if its width already exceeds `len`.
    */
  private def padCenter(s: String, len: Int): String =
    val total = Math.max(0, len - visualLength(s))
    val left = total / 2
    (" " * left) + s + (" " * (total - left))

  /** Strips ANSI escape codes to ensure correct visual length measurement */
  private def visualLength(str: String): Int =
    str.replaceAll("\u001B\\[[;\\d]*m", "").length

  extension (cardBlocks: List[List[String]])

    /** Joins pre-rendered ASCII card blocks horizontally. */
    def joinHorizontally(spacers: List[String] = Nil, spacing: String = " "): List[String] =
      joinCardsHorizontally(cardBlocks, spacers, spacing)
