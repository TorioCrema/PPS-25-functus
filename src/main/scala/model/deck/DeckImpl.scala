package org.pps.functus
package model.deck

import model.deck.card.Card

import scala.annotation.tailrec

case class DeckImpl(cards: Vector[Card]) extends Deck:
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
