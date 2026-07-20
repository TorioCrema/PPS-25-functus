package org.pps.functus
package model.board

import model.deck.card.Card
import model.field.Field

/** Board interface
  */
trait Board:
  type Player

  /** Draws the card on top of the draw stack.
    * @return
    *   the updated board
    */
  def draw(): (Card, Board)

  /** Discards a card on top of the discard stack.
    * @param card
    *   the card to discard
    * @return
    *   the updated board
    */
  def discard(card: Card): Board

  /** Replaces a card on the player's field with a given card, then discards the replaced card.
    * @param player
    *   the player that is replacing the card
    * @param cardIndex
    *   index used to identify the card that will be replaced
    * @param card
    *   the card that will replace the old one
    * @return
    *   the updated board
    */
  def replace(player: Player, cardIndex: Int, card: Card): Board

  /** Getter for the card on top of the discard stack.
    * @return
    *   the card on top of the discard stack
    */
  def getTopDiscardStack: Card

  /** Getter for a player's field
    * @param player
    *   the player to which the field belongs to
    * @return
    *   the player's field
    */
  def getField(player: Player): Field
