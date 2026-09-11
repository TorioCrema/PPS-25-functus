package org.pps.functus

import org.scalatest.TestSuite
import java.io.{OutputStream, PrintStream}

trait SilentTest extends TestSuite:
  private val silentStream = new PrintStream(OutputStream.nullOutputStream())

  abstract override def withFixture(test: NoArgTest) =
    val originalSystemOut = System.out
    System.setOut(silentStream)
    try
      Console.withOut(silentStream) {
        super.withFixture(test)
      }
    finally
      System.setOut(originalSystemOut)