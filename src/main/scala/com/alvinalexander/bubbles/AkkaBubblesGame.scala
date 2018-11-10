package com.alvinalexander.bubbles

import akka.actor._
import org.apache.commons.cli.{BasicParser, CommandLine, Options}

import scala.concurrent.duration._

/**
  * The game starts with this object.
  */
object AkkaBubblesGame extends App {

  // set default values, and let users override them
  private var speedMultiplier = 0.6
  private var numCircles = NUM_CIRCLES
  overrideDefaultSettingsWithCommandLineArgs()

  // needed by several actors
  private val bubblePanel = new BubblePanel

  // create the actor system and actors
  private val actorSystem = ActorSystem(ACTOR_SYSTEM_NAME)
  private val bubblePanelActor = actorSystem.actorOf(Props(new BubblePanelActor(bubblePanel)), name = BUBBLE_PANEL_ACTOR_NAME)
  private val mainFrameActor = actorSystem.actorOf(Props(new MainFrameActor(bubblePanel)), name = MAIN_FRAME_ACTOR_NAME)
  private val playSoundActor = actorSystem.actorOf(Props(new PlaySoundActor()), name = PLAY_SOUND_ACTOR_NAME)

  // create the desired number of circles
  private val circles = new Array[ActorRef](numCircles)
  populateTheCircles()
  private val actorManager = actorSystem.actorOf(Props(new ActorManager(circles)), name = ACTOR_MANAGER_NAME)
  displayMainFrame()
  startTheCircles()

  // if nothing else happens, the game ends in XX seconds
  Thread.sleep(20 * 1000)
  actorSystem.stop(actorManager)
  System.exit(0)

  def displayMainFrame(): Unit = {
    mainFrameActor ! DisplayMainFrame
  }
  /**
    * create the circles. each will have a unique/random character, speed, and color.
    * they'll also be spread out along the x-axis.
    */
  def populateTheCircles(): Unit = {
    val chars = Utils.getRandomChars(numCircles)
    for (i <- 0 until numCircles) {
      val name = chars(i).toString
      val xPos = i * SPACE_BETWEEN_CIRCLES + INITIAL_SPACE
      val speed = (Utils.getRandomSpeed(i) * speedMultiplier).toInt
      val (fgColor, bgColor) = COLORS(i)
      circles(i) = actorSystem.actorOf(Props(new BubbleActor(bubblePanelActor, name, fgColor, bgColor, chars(i), xPos, speed)), name = name)
    }
  }

  def overrideDefaultSettingsWithCommandLineArgs(): Unit = {
    val options = new Options
    options.addOption("n", true, "number of circles")
    options.addOption("s", true, "speed multiplier")
    val commandLineArgs = (new BasicParser).parse(options, args)
    handleNumBubblesOverride(commandLineArgs)
    handleSpeedOverride(commandLineArgs)
  }

  def handleNumBubblesOverride(commandLineArgs: CommandLine): Unit = {
    val numBubblesString = commandLineArgs.getOptionValue("n")
    if (numBubblesString != null) {
      val nDesired = numBubblesString.toInt
      if (nDesired > 0 && nDesired < 11) numCircles = nDesired
    }
  }

  def handleSpeedOverride(commandLineArgs: CommandLine): Unit = {
    val speedString = commandLineArgs.getOptionValue("s")
    if (speedString != null) {
      speedMultiplier = speedString.toFloat
    }
  }

  def startTheCircles(): Unit = {
    for (i <- 0 until numCircles) {
      addActorToScheduler(circles(i))
    }
  }

  def addActorToScheduler(actor: ActorRef): Unit = {
    // use actor system's dispatcher as ExecutionContext
    import actorSystem.dispatcher
    // send the tick message every XX ms (25ms = 40 frames/sec)

    actorSystem.scheduler.schedule(
      0 milliseconds,
      25 milliseconds,
      actor,
      Tick
    )
  }
}
