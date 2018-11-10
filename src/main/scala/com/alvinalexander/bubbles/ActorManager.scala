package com.alvinalexander.bubbles

import akka.actor.{Actor, ActorRef}

case class KillActor(actorRef: ActorRef)
case class SetInitialNumActors(num: Int)

/**
 * Responsibilities:
 * 
 * 1) kill the actors
 * 2) shut down the system
 * 3) show the "You Win" message when all the actors are stopped
 */
class ActorManager(bubbles: Array[ActorRef]) extends Actor {
  
  private var activeActorCount = bubbles.length

  def receive: Receive = {
    case KillActor(actorRef) => doKillActorAction(actorRef)
    case GameOver => doGameOverAction()
    case _ =>
  }
  
  def doKillActorAction(actorRef: ActorRef): Unit = {
    context.stop(actorRef)
    activeActorCount -= 1
    if (activeActorCount == 0) {
      context.parent ! ShowYouWinWindow
      shutdownApplication()
    }
  }
  
  def doGameOverAction(): Unit = {
    stopAllBubbles()
    context.parent ! ShowGameOverWindow
    // TODO don't shut the app down until the 'Game Over' overlay is shown
    //shutdownApplication
  }
  
  // use StopMoving so the panel redraws properly at the end (vs. context.stop) 
  def stopAllBubbles(): Unit = {
    for (b <- bubbles) {
      b ! StopMoving
    }
  }

  def shutdownApplication(): Unit =  {
    context.system.stop(context.parent)
    Thread.sleep(3000)
    System.exit(0)
  }
}
