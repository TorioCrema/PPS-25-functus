package org.pps.functus
package view.utils

import view.utils.MenuItem

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class MenuItemTest extends AnyFunSpec with Matchers:

  describe("MenuItem") {

    describe("Enum values and labels") {

      it("should contain all defined enum cases") {
        MenuItem.values should contain theSameElementsAs Array(
          MenuItem.SingleGame,
          MenuItem.SinglePlayerGame,
          MenuItem.Match,
          MenuItem.SinglePlayerMatch

        )
      }

      it("should associate the correct label to SingleGame") {
        MenuItem.SingleGame.label shouldBe "Play a single game ( 2 Player )"
      }

      it("should associate the correct label to Match") {
        MenuItem.Match.label shouldBe "Play a Match with score limit ( 2 Player )"
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
        MenuItem.fromOrdinal(1) shouldBe MenuItem.SinglePlayerGame
        MenuItem.fromOrdinal(2) shouldBe MenuItem.Match
        MenuItem.fromOrdinal(3) shouldBe MenuItem.SinglePlayerMatch
      }
    }

    describe("TargetScoreOption") {

      describe("Enum values, scores and labels") {

        it("should contain all defined target score cases in order") {
          TargetScoreOption.values should contain theSameElementsInOrderAs Array(
            TargetScoreOption.Score50,
            TargetScoreOption.Score100,
            TargetScoreOption.Score150,
            TargetScoreOption.Score200
          )
        }

        it("should associate the correct score and label to each option") {
          TargetScoreOption.Score50.score shouldBe 50
          TargetScoreOption.Score50.label shouldBe "50 Points"

          TargetScoreOption.Score100.score shouldBe 100
          TargetScoreOption.Score100.label shouldBe "100 Points"

          TargetScoreOption.Score150.score shouldBe 150
          TargetScoreOption.Score150.label shouldBe "150 Points"

          TargetScoreOption.Score200.score shouldBe 200
          TargetScoreOption.Score200.label shouldBe "200 Points"
        }
      }

      describe("Value retrieval and parsing") {

        it("should support valueOf by case name") {
          TargetScoreOption.valueOf("Score50") shouldBe TargetScoreOption.Score50
          TargetScoreOption.valueOf("Score100") shouldBe TargetScoreOption.Score100
          TargetScoreOption.valueOf("Score150") shouldBe TargetScoreOption.Score150
          TargetScoreOption.valueOf("Score200") shouldBe TargetScoreOption.Score200
        }

        it("should throw IllegalArgumentException for invalid case names") {
          an[IllegalArgumentException] should be thrownBy {
            TargetScoreOption.valueOf("InvalidScore")
          }
        }

        it("should correctly find cases using ordinal positioning") {
          TargetScoreOption.fromOrdinal(0) shouldBe TargetScoreOption.Score50
          TargetScoreOption.fromOrdinal(1) shouldBe TargetScoreOption.Score100
          TargetScoreOption.fromOrdinal(2) shouldBe TargetScoreOption.Score150
          TargetScoreOption.fromOrdinal(3) shouldBe TargetScoreOption.Score200
        }
      }
    }
  }
