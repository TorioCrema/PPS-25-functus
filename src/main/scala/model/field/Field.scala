package org.pps.functus
package model.field

import model.deck.card.Card

trait Field():

  def length: Int

  def cardsList: List[Card]

  def replace(index: Int, card: Card): (Card, Field)

  def addCard(card: Card): Field

  def getCard(index: Int): (Card, Field)
