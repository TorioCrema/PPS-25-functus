package org.pps.functus
package view

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class KeyEnumTest extends AnyFunSpec with Matchers:

  describe("A Key Enum") {

    it("should contain all defined key values") {
      val expectedKeys = Array(
        Key.UP,
        Key.DOWN,
        Key.LEFT,
        Key.RIGHT,
        Key.ENTER,
        Key.ESCAPE,
        Key.UNKNOWN
      )

      Key.values should contain theSameElementsInOrderAs expectedKeys
    }

    it("should correctly resolve Key from string name using valueOf") {
      Key.valueOf("UP") shouldBe Key.UP
      Key.valueOf("DOWN") shouldBe Key.DOWN
      Key.valueOf("LEFT") shouldBe Key.LEFT
      Key.valueOf("RIGHT") shouldBe Key.RIGHT
      Key.valueOf("ENTER") shouldBe Key.ENTER
      Key.valueOf("ESCAPE") shouldBe Key.ESCAPE
      Key.valueOf("UNKNOWN") shouldBe Key.UNKNOWN
    }

    it("should throw an IllegalArgumentException when valueOf receives an invalid key name") {
      an[IllegalArgumentException] should be thrownBy {
        Key.valueOf("INVALID_KEY")
      }
    }

    it("should match pattern matching exhaustively") {
      def keyToString(key: Key): String = key match
        case Key.UP      => "Up Arrow"
        case Key.DOWN    => "Down Arrow"
        case Key.LEFT    => "Left Arrow"
        case Key.RIGHT   => "Right Arrow"
        case Key.ENTER   => "Enter Key"
        case Key.ESCAPE  => "Escape Key"
        case Key.UNKNOWN => "Unknown Key"

      keyToString(Key.UP) shouldBe "Up Arrow"
      keyToString(Key.ESCAPE) shouldBe "Escape Key"
      keyToString(Key.UNKNOWN) shouldBe "Unknown Key"
    }
  }
