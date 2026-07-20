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
  def multiColumnFieldsPanel[T](field1: Seq[T], field2: Seq[T])(using drawer: Drawable[T]): SimplePanel[String] =
    val f1Drawn = field1.map(drawer.draw)
    val f2Drawn = field2.map(drawer.draw)
    
    val f1Rows = f1Drawn.grouped(2).toSeq
    val f2Rows = f2Drawn.grouped(2).toSeq
    
    val combinedRows = f1Rows.zipAll(f2Rows, Nil, Nil).map { case (r1, r2) =>
      val f1Text = r1.mkString(" ")
      val f2Text = r2.mkString(" ")
      f"$f1Text%-15s$f2Text"
    }
    SimplePanels.rowPanel(combinedRows)