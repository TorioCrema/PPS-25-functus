package org.pps.functus
package view.utils

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import view.Key

import org.pps.functus.view.utils.Utils.{ANSI_GREEN_BOLD, ANSI_RESET}

class UtilsTest extends AnyFunSpec with Matchers:

  describe("Utils") {

    describe("centerText") {

      it("should return the original text if length is equal to or smaller than visual length") {
        val text = "Hello"
        Utils.centerText(text, 5) shouldBe "Hello"
        Utils.centerText(text, 3) shouldBe "Hello"
      }

      it("should pad text with spaces to center it horizontally") {
        val text = "Test"
        val totalLength = 10
        Utils.centerText(text, totalLength) shouldBe "   Test"
      }

      it("should strip ANSI color codes when calculating visual length for padding") {
        val coloredText = s"${Utils.ANSI_GREEN_BOLD}Test${Utils.ANSI_RESET}"
        val totalLength = 10
        Utils.centerText(coloredText, totalLength) shouldBe s"   $coloredText"
      }

      it("should correctly handle empty strings") {
        val totalLength = 8
        Utils.centerText("", totalLength) shouldBe "    "
      }
    }

    describe("viewBuilder and string rendering helpers") {

      it("should clear the viewBuilder and flush terminal on clearScreen") {
        given sb: StringBuilder = Utils.viewBuilder
        sb.append("Some leftover content")

        Utils.clearScreen()

        sb.toString() shouldBe empty
      }

    }

    describe("Constants and Given instances") {

      it("should expose valid separator string matching terminal columns") {
        Utils.separator.length shouldBe Utils.length
      }

      it("should define valid non-empty ANSI color codes") {
        Utils.ANSI_RESET should not be empty
        Utils.ANSI_GREEN_BOLD should not be empty
      }
    }

    describe("Terminal lifecycle and keybindings") {

      it("should execute init without throwing exceptions") {
        noException should be thrownBy Utils.init()
      }

      it("should execute restore without throwing exceptions") {
        noException should be thrownBy Utils.restore()
      }
    }


  }