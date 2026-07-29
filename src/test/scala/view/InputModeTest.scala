package org.pps.functus
package view

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class InputModeTest extends AnyFunSpec with Matchers:

  describe("An InputMode Enum") {

    it("should contain all defined input modes") {
      val expectedModes = Array(
        InputMode.ActionMenu,
        InputMode.SelectCardOnBoard
      )

      InputMode.values should contain theSameElementsInOrderAs expectedModes
    }

    it("should correctly resolve InputMode from string name using valueOf") {
      InputMode.valueOf("ActionMenu") shouldBe InputMode.ActionMenu
      InputMode.valueOf("SelectCardOnBoard") shouldBe InputMode.SelectCardOnBoard
    }

    it("should throw an IllegalArgumentException when valueOf receives an invalid mode name") {
      an[IllegalArgumentException] should be thrownBy {
        InputMode.valueOf("NonExistentMode")
      }
    }

    it("should support pattern matching for state switching") {
      def getModeDescription(mode: InputMode): String = mode match
        case InputMode.ActionMenu        => "Navigating action menu"
        case InputMode.SelectCardOnBoard => "Selecting a card on the board"

      getModeDescription(InputMode.ActionMenu) shouldBe "Navigating action menu"
      getModeDescription(InputMode.SelectCardOnBoard) shouldBe "Selecting a card on the board"
    }
  }
