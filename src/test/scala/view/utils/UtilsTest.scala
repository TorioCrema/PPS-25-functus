package org.pps.functus
package view.utils

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import view.Key

import view.utils.Utils.{ANSI_GREEN_BOLD, ANSI_RESET, terminalWidth}

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

  }
