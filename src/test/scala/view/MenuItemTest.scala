package org.pps.functus
package view

import view.utils.MenuItem
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class MenuItemTest extends AnyFunSpec with Matchers:

  describe("MenuItem") {

    describe("Enum values and labels") {

      it("should contain all defined enum cases") {
        MenuItem.values should contain theSameElementsAs Array(
          MenuItem.SingleGame,
          MenuItem.Match
        )
      }

      it("should associate the correct label to SingleGame") {
        MenuItem.SingleGame.label shouldBe "Play a single game"
      }

      it("should associate the correct label to Match") {
        MenuItem.Match.label shouldBe "Play a Match with score limit"
      }
    }

    describe("Value retrieval and parsing") {

      it("should support valueOf by case name") {
        MenuItem.valueOf("SingleGame") shouldBe MenuItem.SingleGame
        MenuItem.valueOf("Match") shouldBe MenuItem.Match
      }

      it("should throw IllegalArgumentException for invalid case names") {
        an[IllegalArgumentException] should be thrownBy {
          MenuItem.valueOf("InvalidOption")
        }
      }

      it("should correctly find cases using ordinal positioning") {
        MenuItem.fromOrdinal(0) shouldBe MenuItem.SingleGame
        MenuItem.fromOrdinal(1) shouldBe MenuItem.Match
      }
    }
  }
