package org.pps.functus
package model.deck

import model.deck.card.Card

import scala.annotation.tailrec

/** Represents a deck of cards. */
sealed trait Deck:
  /** The cards currently in the deck. */
  val cards: Vector[Card]

  /** Draws the top card from the deck.
    *
    * @return
    *   a tuple of the drawn card and the updated deck
    * @throws NoSuchElementException
    *   if the deck is empty
    */
  def draw(): (Card, Deck)

  /** Returns a new deck with the same cards in a randomly shuffled order.
    *
    * @return
    *   a new shuffled deck
    */
  def shuffle(): Deck

/** A concrete implementation of [[Deck]] backed by a [[Vector]] of cards.
  *
  * @param cards
  *   the cards in this deck, ordered from top (index 0) to bottom
  */
final case class DeckImpl(cards: Vector[Card]) extends Deck:

  override def draw(): (Card, Deck) =
    val card = this.cards.head
    (card, DeckImpl(cards.tail))

  override def shuffle(): Deck =
    @tailrec
    def shuffleOn(cards: Vector[Card], shuffledCards: Vector[Card]): Vector[Card] = cards match
      case Vector() => shuffledCards
      case _        =>
        val index = scala.util.Random.nextInt(cards.size)
        val randomCard = cards(index)
        shuffleOn(cards.patch(index, Nil, 1), shuffledCards.appended(randomCard))
    DeckImpl(shuffleOn(this.cards, Vector.empty))
