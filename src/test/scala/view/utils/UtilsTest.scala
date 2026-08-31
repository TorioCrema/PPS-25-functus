package org.pps.functus
package view.utils

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import view.utils.Utils.terminalWidth

class UtilsTest extends AnyFunSpec with Matchers:

  describe("Utils") {

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
        Utils.SEPARATOR_CHAR.length * terminalWidth shouldBe terminalWidth
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

    describe("Text layout and centering helpers") {

      it("should pad text correctly using centerText") {
        val text = "TEST"
        val centered = Utils.centerText(text, targetLength = 20)

        // Visual length is 4, target length is 20 -> (20 - 4) / 2 = 8 spaces padding
        centered should startWith(" " * 8)
        centered should include(text)
      }

      it("should append a vertically centered text block using renderCenteredBlock") {
        given sb: StringBuilder = Utils.viewBuilder

        val block = "LINE 1\nLINE 2\nLINE 3"

        Utils.renderCenteredBlock(block)

        val output = sb.toString()
        output should include("LINE 1")
        output should include("LINE 2")
        output should include("LINE 3")
      }

      it("should append ASCII header art when drawHeader is invoked") {
        given sb: StringBuilder = Utils.viewBuilder

        Utils.drawHeader

        val output = sb.toString()
        output should not be empty
        output should include(Utils.ANSI_GREEN_BOLD)
        output should include(Utils.ANSI_RESET)
      }
    }

    describe("Safe terminal dimensions") {

      it("should return a positive terminal height") {
        Utils.terminalHeight should be > 0
      }
    }
  }
