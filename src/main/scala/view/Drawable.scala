package org.pps.functus
package view

trait Drawable[T]:
  def draw(element: T): String
