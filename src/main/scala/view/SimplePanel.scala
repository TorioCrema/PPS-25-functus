package org.pps.functus
package view

class SimplePanel[T](val elements: Seq[T] = Nil, val separator: String = " ") extends Panel[T]:
  require(elements != null)
  require(separator != null)

  override def add(elem: T): SimplePanel[T] = SimplePanel(elements :+ elem, separator)

  override def draw(using drawer: Drawable[T]): String =
    elements.foldLeft("")((acc, elem) => acc + drawer.draw(elem) + separator)

object SimplePanels:
  def rowPanel[T](rows: Seq[T] = Nil): SimplePanel[T] = SimplePanel[T](rows, "\n")
  def columnPanel[T](columns: Seq[T] = Nil): SimplePanel[T] = SimplePanel[T](columns, "\t")