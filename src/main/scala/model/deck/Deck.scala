package model.deck

import model.deck.card.Card

trait Deck:
  val cards: Vector[Card]

  def draw(): (Card, Deck)
  def shuffle(): Deck
