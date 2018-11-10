package com.alvinalexander.bubbles

import java.awt._
import java.awt.image.BufferedImage

case class Bubble(
                   var x: Int,
                   var y: Int,
                   var lastX: Int,
                   var lastY: Int,
                   circleDiameter: Int,
                   fgColor: Color,
                   bgColor: Color,
                   char: Char
                 ) {

  private var image: BufferedImage = _

  def drawBubbleFast(g: Graphics, gc: GraphicsConfiguration): Unit = {
    val g2 = g.asInstanceOf[Graphics2D]
    if (image == null) {
      println("image was null")
      // build the image on the first call
      image = gc.createCompatibleImage(circleDiameter + 1, circleDiameter + 1, Transparency.BITMASK)
      val gImg = image.getGraphics.asInstanceOf[Graphics2D]
      renderBubble(gImg)
      gImg.dispose()
    }
    g2.drawImage(image, x, y, null)
  }

  // given a Graphics object, render the bubble
  def renderBubble(g: Graphics): Unit = {
    val g2 = g.create.asInstanceOf[Graphics2D]
    // draw the circle
    g2.setColor(bgColor)
    g2.fillOval(0, 0, circleDiameter, circleDiameter)
    // draw the character
    g2.setFont(CIRCLE_FONT)
    g2.setColor(fgColor)
    g2.drawString(char.toString, CIRCLE_FONT_PADDING_X, CIRCLE_FONT_PADDING_Y)
    g2.dispose()
  }
}
