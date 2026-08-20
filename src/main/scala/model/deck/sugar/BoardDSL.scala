package org.pps.functus
package model.deck.sugar

import model.board.{Board, BoardFactory, BoardImpl, Player}
import model.deck.{Deck, DeckFactory, DeckImpl}
import model.deck.card.Card
import model.deck.sugar.DeckDSL.deck.CardBuilder
import model.field.{Field, FieldImpl}

/** DSL for building a [[Board]].
  *
  * Integrates with [[CardDSL]], [[DeckDSL]], and [[FieldDSL]].
  *
  * Entry points:
  *   - `default board` → fully dealt game board (4 cards per player), immediately returns a [[Board]]
  *   - `board from default` → same, but chainable with `withCustom`
  *   - `lockedBoard from default` → assigned cards are removed from the deck
  *
  * In locked mode, cards assigned to players (either via `withCustom` or dealt automatically) are removed from the
  * remaining deck. In unlocked mode (default), the deck is left untouched.
  *
  * {{{
  *   import CardDSL.*
  *   import DeckDSL.*
  *   import FieldDSL.*
  *   import BoardDSL.*
  *
  *   val c1 = ace of Cups
  *   val c2 = two of Swords
  *
  *   // Shorthand: immediately returns a fully dealt Board
  *   val b1 = default board
  *
  *   // Builder: override player1's field (unlocked — c1 and c2 stay in the deck)
  *   val b2 = (board from default) withCustom playerOne(c1 and c2)
  *
  *   // Locked: c1 and c2 are removed from the deck
  *   val b3 = (lockedBoard from default) withCustom playerOne(c1 and c2)
  *
  *   // Full customisation
  *   val b4 = (board from default)
  *     .withCustom(playerOne(c1 and c2))
  *     .withCustom(playerTwo(c3 and c4))
  *     .withCustom(deck(myDeck))
  *     .withCustom(discardPile(c5 and c6))
  * }}}
  */
object BoardDSL:

  /** Entry point for a default [[Board]]: `default board`.
    *
    * Immediately returns a fully dealt [[Board]] (4 cards per player).
    */
  object default:
    infix def board: Board = BoardFactory.BoardWithPopulatedFields()

  /** Entry point for an unlocked [[BoardBuilder]]: `board from default`. */
  object board:
    infix def from(d: default.type): BoardBuilder = new BoardBuilder(Unlocked)

  /** Entry point for a locked [[BoardBuilder]]: `lockedBoard from default`.
    *
    * In locked mode, all cards assigned to players are removed from the remaining deck.
    */
  object lockedBoard:
    infix def from(d: default.type): BoardBuilder = new BoardBuilder(Locked)

  sealed trait Customisation
  case class PlayerOneCards(field: Field) extends Customisation
  case class PlayerTwoCards(field: Field) extends Customisation
  case class CustomDeck(deck: Deck) extends Customisation
  case class CustomDiscard(cards: List[Card]) extends Customisation

  infix def playerOne(field: Field): PlayerOneCards = PlayerOneCards(field)
  infix def playerOne(card: Card): PlayerOneCards = PlayerOneCards(FieldImpl(Vector(card)))
  infix def playerTwo(field: Field): PlayerTwoCards = PlayerTwoCards(field)
  infix def playerTwo(card: Card): PlayerTwoCards = PlayerTwoCards(FieldImpl(Vector(card)))

  infix def deck(d: Deck): CustomDeck = CustomDeck(d)
  infix def discardPile(builder: CardBuilder): CustomDiscard =
    CustomDiscard(builder.cards.toList)

  private sealed trait LockMode
  private case object Locked extends LockMode
  private case object Unlocked extends LockMode

  /** Builder for a [[Board]], obtained via `board from default` or `lockedBoard from default`.
    *
    * Customisations are accumulated with `withCustom` and applied on `build`. A [[given Conversion]] allows a
    * [[BoardBuilder]] to be used wherever a [[Board]] is expected.
    */
  final class BoardBuilder private[BoardDSL] (
      lockMode: LockMode,
      customisations: List[Customisation] = Nil
  ):
    /** Adds a [[Customisation]] and returns a new [[BoardBuilder]]. */
    infix def withCustom(c: Customisation): BoardBuilder =
      new BoardBuilder(lockMode, customisations :+ c)

    def build: Board =
      val field1Ovr: Option[Field] = customisations.collectFirst { case PlayerOneCards(f) => f }
      val field2Ovr: Option[Field] = customisations.collectFirst { case PlayerTwoCards(f) => f }
      val deckOvr: Option[Deck] = customisations.collectFirst { case CustomDeck(d) => d }
      val discOvr: Option[List[Card]] = customisations.collectFirst { case CustomDiscard(cs) => cs }

      val assignedCards: List[Card] =
        field1Ovr.map(_.cardsList).getOrElse(Nil) ++
          field2Ovr.map(_.cardsList).getOrElse(Nil) ++
          discOvr.getOrElse(Nil)

      val freshDeck: Deck = DeckFactory().shuffle()
      val finalDeck: Deck = deckOvr.getOrElse {
        lockMode match
          case Locked   => removeCards(freshDeck, assignedCards)
          case Unlocked => freshDeck
      }

      val (deckAfterField1, field1) = resolveField(field1Ovr, finalDeck, 4)
      val (deckAfterField2, field2) = resolveField(field2Ovr, deckAfterField1, 4)

      val actualDeck: Deck = lockMode match
        case Locked   => deckAfterField2
        case Unlocked => finalDeck

      BoardImpl(
        deck = actualDeck,
        discardPile = discOvr.getOrElse(Nil),
        players = Map(
          Player.Player1 -> field1,
          Player.Player2 -> field2
        )
      )

  /** Allows a [[BoardBuilder]] to be used wherever a [[Board]] is expected. */
  given Conversion[BoardBuilder, Board] = _.build

  private def removeCards(deck: Deck, toRemove: List[Card]): Deck =
    val remaining = toRemove.foldLeft(deck.cards.toList) { (cards, target) =>
      val idx = cards.indexWhere(c => c.value == target.value && c.suit == target.suit)
      if idx >= 0 then cards.patch(idx, Nil, 1) else cards
    }
    DeckImpl(remaining.toVector)

  private def dealFieldWithDeck(deck: Deck, n: Int): (Deck, Field) =
    (0 until n)
      .foldLeft((deck, FieldImpl(): Field)) { case ((d, f), _) =>
        d.draw() match
          case Some((card, remaining)) => (remaining, f.addCard(card))
          case None                    => (d, f)
      }

  private def resolveField(ovr: Option[Field], deck: Deck, n: Int): (Deck, Field) =
    ovr match
      case Some(f) => (deck, f)
      case None    => dealFieldWithDeck(deck, n)
