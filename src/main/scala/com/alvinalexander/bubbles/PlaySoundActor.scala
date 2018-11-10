package com.alvinalexander.bubbles

import akka.actor.Actor
import sun.audio._

case object PlaySuccessSound
case object PlayFailureSound
    
class PlaySoundActor extends Actor {

  private val SUCCESS_SOUND_FILENAME = "Synth-Zingers-04.aif"
  private val FAILURE_SOUND_FILENAME = "Comedy-Low-Honk.aif"

  def receive: Receive = {
    case PlaySuccessSound => playSoundFile(SUCCESS_SOUND_FILENAME)
    case PlayFailureSound => playSoundFile(FAILURE_SOUND_FILENAME)
    case _ =>
  }

  def playSoundFile(filename: String): Unit = {
    val inputStream = getClass.getClassLoader.getResourceAsStream(filename)
    val audioStream = new AudioStream(inputStream)
    AudioPlayer.player.start(audioStream)
    
    // TODO sound won't play if the file is closed right away
    //inputStream.close
  }
}
