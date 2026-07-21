package org.pps.functus
package view

private val FIELD_COLUM_NUMBER = 2
private val MIN_PADDING_BETWEEN_FIELD = 0
private val MAX_PADDING_BETWEEN_FIELD = 15

sealed trait Card
case object HiddenCard extends Card:
  override def toString: String = "[H]"

// Special Card representing an empty space (padding)
case class EmptySpaceCard(width: Int = 1) extends Card:
  override def toString: String = " " * width

// A companion object for quick card generation
object Card:
  def hiddenLine(count: Int): Seq[Card] = Seq.fill(count)(HiddenCard)

given cardDrawer: Drawable[Card] with
  def draw(elem: Card): String = elem.toString

class SimplePanel[T](val elements: Seq[T] = Nil, val separator: String = " ") extends Panel[T]:
  require(elements != null)
  require(separator != null)

  override def add(elem: T): SimplePanel[T] = SimplePanel(elements :+ elem, separator)

  override def draw(using drawer: Drawable[T]): String =
    elements.foldLeft("")((acc, elem) => acc + drawer.draw(elem) + separator)

object SimplePanels:
  def rowPanel[T](rows: Seq[T] = Nil): SimplePanel[T] = SimplePanel[T](rows, "\n")
  def columnPanel[T](columns: Seq[T] = Nil): SimplePanel[T] = SimplePanel[T](columns, "\t")
  def separationPanel[T](panels: Seq[T] = Nil): SimplePanel[T] = SimplePanel[T](panels, "")

  def multiColumnFieldsPanel[T](
      field1CardNumber: Int,
      field2CardNumber: Int
  ): SimplePanel[SimplePanel[SimplePanel[SimplePanel[Card]]]] =

    val field1 = Card.hiddenLine(field1CardNumber)
    val field2 = Card.hiddenLine(field2CardNumber)

    val f1Rows = field1.grouped(FIELD_COLUM_NUMBER).toSeq
    val f2Rows = field2.grouped(FIELD_COLUM_NUMBER).toSeq

    val combinedRows: Seq[SimplePanel[SimplePanel[Card]]] =
      f1Rows.zipAll(f2Rows, Nil, Nil).map { case (r1, r2) =>

        // calculate the current length of the text in column 1 (e.g. 2 card = "[H]\t[H]" = 7 car)
        val rawCol1Text = SimplePanels.columnPanel(r1).draw

        // calculate how many spaces it takes to get to exactly to  MAX_PADDING_BETWEEN_FIELD characters
        val neededPadding = Math.max(MIN_PADDING_BETWEEN_FIELD, MAX_PADDING_BETWEEN_FIELD - rawCol1Text.length)

        // add to the first column the cards of r1 + a PaddingCard with the right spaces
        val col1Panel: SimplePanel[Card] =
          SimplePanels.columnPanel(r1 :+ EmptySpaceCard(neededPadding))

        val col2Panel: SimplePanel[Card] = SimplePanels.columnPanel(r2)
        SimplePanels.separationPanel(Seq(col1Panel, col2Panel))
      }
    SimplePanels.rowPanel(combinedRows)

    val containerRowPanel = Seq(SimplePanels.rowPanel(combinedRows))
    SimplePanels.rowPanel(containerRowPanel)
