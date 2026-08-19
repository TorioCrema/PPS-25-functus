package org.pps.functus
package model.deck.sugar

import model.board.{Board, BoardFactory, BoardImpl, Player}
import model.deck.{Deck, DeckFactory, DeckImpl}
import model.deck.card.Card
import model.deck.sugar.DeckDSL.deck.CardBuilder
import model.field.{Field, FieldImpl}

object BoardDSL:

  object default

  object board:
    infix def from(d: default.type): BoardBuilder = new BoardBuilder(Unlocked)

  object lockedBoard:
    infix def from(d: default.type): BoardBuilder = new BoardBuilder(Locked)

  extension (d: default.type) infix def board: Board = BoardFactory.BoardWithPopulatedFields()

  sealed trait Customisation
  case class PlayerOneCards(field: Field) extends Customisation
  case class PlayerTwoCards(field: Field) extends Customisation
  case class CustomDeck(deck: Deck) extends Customisation
  case class CustomDiscard(cards: List[Card]) extends Customisation

  infix def playerOne(field: Field): PlayerOneCards = PlayerOneCards(field)
  infix def playerTwo(field: Field): PlayerTwoCards = PlayerTwoCards(field)
  infix def deck(d: Deck): CustomDeck = CustomDeck(d)
  infix def discardPile(builder: CardBuilder): CustomDiscard =
    CustomDiscard(builder.cards.toList)

  private sealed trait LockMode
  private case object Locked extends LockMode
  private case object Unlocked extends LockMode

  final class BoardBuilder private[BoardDSL] (
      lockMode: LockMode,
      customisations: List[Customisation] = Nil
  ):
    infix def withCustom(c: Customisation): BoardBuilder =
      new BoardBuilder(lockMode, customisations :+ c)

    def build: Board =
      val field1Ovr: Option[Field] = customisations.collectFirst { case PlayerOneCards(f) => f }
      val field2Ovr: Option[Field] = customisations.collectFirst { case PlayerTwoCards(f) => f }
      val deckOvr: Option[Deck] = customisations.collectFirst { case CustomDeck(d) => d }
      val discOvr: Option[List[Card]] = customisations.collectFirst { case CustomDiscard(cs) => cs }

      val assignedCards: List[Card] =
        field1Ovr.map(_.cardsList).getOrElse(Nil) ++
          field2Ovr.map(_.cardsList).getOrElse(Nil)

      val freshDeck: Deck = DeckFactory().shuffle()
      val finalDeck: Deck = deckOvr.getOrElse {
        lockMode match
          case Locked   => removeCards(freshDeck, assignedCards)
          case Unlocked => freshDeck
      }

      val field1: Field = field1Ovr.getOrElse(dealField(finalDeck, 4))
      val field2: Field = field2Ovr.getOrElse(dealField(finalDeck, 4))

      BoardImpl(
        deck = finalDeck,
        discardPile = discOvr.getOrElse(Nil),
        players = Map(
          Player.Player1 -> field1,
          Player.Player2 -> field2
        )
      )

    given Conversion[BoardBuilder, Board] = _.build

  private def dealField(deck: Deck, n: Int): Field =
    (0 until n)
      .foldLeft((deck, FieldImpl(): Field)) { case ((d, f), _) =>
        d.draw() match
          case Some((card, remaining)) => (remaining, f.addCard(card))
          case None                    => (d, f)
      }
      ._2

  private def removeCards(deck: Deck, toRemove: List[Card]): Deck =
    val remaining = toRemove.foldLeft(deck.cards.toList) { (cards, target) =>
      val idx = cards.indexWhere(c => c.value == target.value && c.suit == target.suit)
      if idx >= 0 then cards.patch(idx, Nil, 1) else cards
    }
    DeckImpl(remaining.toVector)
