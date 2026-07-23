package org.pps.functus
package view

import view.{Drawable, SimplePanel}

import org.mockito.Mockito
import org.mockito.Mockito.verifyNoInteractions
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock

class SimplePanelTest extends AnyFlatSpec with Matchers:
  private val panel: SimplePanel[Int] = SimplePanel()
  private val firstElem = 1
  private val secondElem = 2
  private val panelWithElem = panel.add(firstElem).add(secondElem)

  "SimplePanel" should "add elements" in:
    panel.add(firstElem).elements should be(Seq(firstElem))
    panel.add(firstElem).add(secondElem).elements should be(Seq(firstElem, secondElem))

  it should "draw each element once" in:
    val drawableMocker = mock[Drawable[Int]]
    given Drawable[Int] = drawableMocker
    verifyNoInteractions(drawableMocker)
    panelWithElem.draw
    Mockito.mockingDetails(drawableMocker).getInvocations.size() should be(2)

  it should "join drawables" in:
    given Drawable[Int] = _.toString()
    panelWithElem.draw should be(s"$firstElem${panelWithElem.separator}$secondElem${panelWithElem.separator}")
