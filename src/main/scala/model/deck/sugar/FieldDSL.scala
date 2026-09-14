package org.pps.functus
package model.deck.sugar

import model.deck.card.Card
import model.field.{Field, FieldImpl}

sealed trait FieldBuilderLike[T]:
  /** DSL helper to build fields by writing `<T> and <card>`.
    *
    * @param cardToAdd
    *   the card to add to the field
    * @return
    *   the field with the added card
    */
  infix def and(cardToAdd: Card): Field

/** Allows to create [[Field]]s and to take cards from them with sugar syntax.
  */
object FieldDSL:
  given Conversion[Card, FieldBuilderLike[Card]] = FieldBuilderFromCard(_)
  given Conversion[Field, FieldBuilderLike[Field]] = FieldBuilderFromField(_)

  private class FieldBuilderFromCard(card: Card) extends FieldBuilderLike[Card]:
    override infix def and(cardToAdd: Card): Field = FieldImpl(Vector(card, cardToAdd))

  private class FieldBuilderFromField(field: Field) extends FieldBuilderLike[Field]:
    override infix def and(cardToAdd: Card): Field = field.addCard(cardToAdd)
