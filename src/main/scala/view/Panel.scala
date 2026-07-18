package org.pps.functus
package view

trait Panel[T]:
  def add(elem: T): Panel[T]
  def draw(using drawer: Drawable[T]): String
