package org.pps.functus
package view

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SimplePanelFactoryTest extends AnyFlatSpec with Matchers:

  // Replace each sequence of 2 or more spaces/tabs with a single space,
  // and removes trailing spaces this way I test the logic and not the spacing.
  private def normalizeSpacing(s: String): String =
    s.replaceAll("[ \t]{2,}", " ").replaceAll("(?m) +$", "")

  private def loadField(cardNumberField1: Int, cardNumberField2: Int): SimplePanel[String] =
    val field1 = Seq.fill(cardNumberField1)(1)
    val field2 = Seq.fill(cardNumberField2)(1)
    val panel = SimplePanels.multiColumnFieldsPanel(field1, field2)
    panel

  given stringDrawer: Drawable[String] = identity

  given cardDrawer: Drawable[Int] = _ => "[H]"

  "SimplePanels.multiColumnFieldsPanel" should "correctly align fields with 4 and 5 cards on two columns" in:
    val panel = loadField(4,5)

    // Just one space is enough to separate the columns in the test!
    val expected =
      """[H] [H] [H] [H]
        |[H] [H] [H] [H]
        | [H]
        |""".stripMargin

    normalizeSpacing(panel.draw) should be(normalizeSpacing(expected))

  it should "handle cases where Field 1 has more rows than Field 2" in :
    val panel = loadField(5, 2)

    // Just one space is enough to separate the columns in the test!
    val expected =
      """[H] [H] [H] [H]
        |[H] [H]
        |[H]
        |""".stripMargin

    normalizeSpacing(panel.draw) should be(expected)

  it should "return an empty panel when both fields are empty" in :
    val panel = loadField(0,0)
    normalizeSpacing(panel.draw) should be("")

  it should "handle a completely empty Field 1 with an active Field 2" in :
    val panel = loadField(0, 3)
    // Just one space is enough to separate the columns in the test!
    val expected =
      """ [H] [H]
        | [H]
        |""".stripMargin

    normalizeSpacing(panel.draw) should be(expected)

  it should "handle an active Field 1 with a completely empty Field 2" in :
    val panel = loadField(3,0)

    // Just one space is enough to separate the columns in the test!
    val expected =
      """[H] [H]
        |[H]
        |""".stripMargin

    normalizeSpacing(panel.draw) should be(expected)