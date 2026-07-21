package org.pps.functus.view

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SimplePanelFactoryTest extends AnyFlatSpec with Matchers:

  given stringDrawer: Drawable[String] = elem => elem

  given panelDrawer[T: Drawable]: Drawable[SimplePanel[T]] with
    def draw(elem: SimplePanel[T]): String = elem.draw

  // Represents a row of our grid divided into the two fields
  case class RowLayout(field1Cards: List[String], field2Cards: List[String])

  // Converts the text drawn from the panel into a RowLayout list,
  // separating the block of Field 1 (first 15 cars) from Field 2 (remaining).
  private def parseLayout(rendered: String): List[RowLayout] =
    if rendered.trim.isEmpty then Nil
    else
      rendered.replaceAll("\r\n", "\n")
        .split("\n")
        .toList
        .map { line =>
          // Cuts the first 15 character (Field 1) and the remaining (Field 2)
          val f1Zone = if line.length >= 15 then line.substring(0, 15) else line
          val f2Zone = if line.length > 15 then line.substring(15) else ""

          // extract only cards "[H]" ingoring spaces and tabulations
          val f1Cards = "\\[H\\]".r.findAllIn(f1Zone).toList
          val f2Cards = "\\[H\\]".r.findAllIn(f2Zone).toList

          RowLayout(f1Cards, f2Cards)
        }

  "multiColumnFieldsPanel" should "correctly align fields with 4 and 5 cards" in:
    val panel = SimplePanels.multiColumnFieldsPanel(4, 5)

    val expected = List(
      RowLayout(field1Cards = List("[H]", "[H]"), field2Cards = List("[H]", "[H]")),
      RowLayout(field1Cards = List("[H]", "[H]"), field2Cards = List("[H]", "[H]")),
      RowLayout(field1Cards = List(),            field2Cards = List("[H]"))
    )

    parseLayout(panel.draw) should be(expected)

  it should "handle cases where Field 1 has more rows than Field 2" in:
    val panel = SimplePanels.multiColumnFieldsPanel(5, 2)

    val expected = List(
      RowLayout(field1Cards = List("[H]", "[H]"), field2Cards = List("[H]", "[H]")),
      RowLayout(field1Cards = List("[H]", "[H]"), field2Cards = List()),
      RowLayout(field1Cards = List("[H]"),        field2Cards = List())
    )

    parseLayout(panel.draw) should be(expected)

  it should "return an empty layout when both fields are empty" in:
    val panel = SimplePanels.multiColumnFieldsPanel(0, 0)

    parseLayout(panel.draw) should be(Nil)

  it should "handle a completely empty Field 1 with an active Field 2" in:
    val panel = SimplePanels.multiColumnFieldsPanel(0, 3)

    val expected = List(
      RowLayout(field1Cards = List(), field2Cards = List("[H]", "[H]")),
      RowLayout(field1Cards = List(), field2Cards = List("[H]"))
    )

    parseLayout(panel.draw) should be(expected)

  it should "handle an active Field 1 with a completely empty Field 2" in:
    val panel = SimplePanels.multiColumnFieldsPanel(3, 0)

    val expected = List(
      RowLayout(field1Cards = List("[H]", "[H]"), field2Cards = List()),
      RowLayout(field1Cards = List("[H]"),        field2Cards = List())
    )

    parseLayout(panel.draw) should be(expected)