package com.alvinalexander.bubbles

import java.awt._
import java.awt.event.{KeyEvent, KeyListener}

import akka.actor.{Actor, ActorRef, Props, Terminated}
import javax.swing.{JFrame, JPanel, SwingUtilities}

case object DisplayMainFrame
case object ShowGameOverWindow
case object ShowYouWinWindow

/**
 * An Actor to handle all the interactions with the JFrame.
 * User keystrokes trigger a noise and may kill a bubble (character actor).
 */
class MainFrameActor(bubblePanel: BubblePanel) extends Actor {

  private var playSoundActor: ActorRef = _
  private var actorManager: ActorRef = _
  
  def receive: Receive = {
    case DisplayMainFrame => showMainFrame()
    case ShowGameOverWindow => showGameOverWindow()
    case ShowYouWinWindow => showYouWinWindow()
    case Terminated(_) => getPlaySoundActor ! PlayFailureSound
    case _ =>
  }
  
  val myKeyListener: KeyListener = new KeyListener {
    override def keyPressed(e: KeyEvent): Unit =
    {
      if (e.getKeyCode == KeyEvent.VK_ESCAPE) {
        // TODO quit the app
      } else {
        // look up the peer actor and kill it (assuming the keystroke is right. invalid keystrokes cause no problems)
        attemptToKillCharacterActor(e.getKeyChar)
      }
    }

    override def keyReleased(e: KeyEvent): Unit = {}
    override def keyTyped(e: KeyEvent): Unit = {}
  }

  val mainFrame: JFrame = new JFrame {
    setMinimumSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT))
    setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT))
    addKeyListener(myKeyListener)
  }

  configureMainFrame()
  
  def configureMainFrame(): Unit = {
    mainFrame.setTitle(APPLICATION_NAME)
    mainFrame.setBackground(Color.BLACK)
    mainFrame.getContentPane.add(bubblePanel)
    mainFrame.setLocationRelativeTo(null)
    mainFrame.setUndecorated(true)
    mainFrame.getRootPane.putClientProperty("Window.alpha", 0.9f)
  }
  
  def lookupCharacterActor(c: Char): ActorRef = context.actorOf(Props(), c.toString)

  def getPlaySoundActor: ActorRef = {
    if (playSoundActor == null) playSoundActor = context.actorOf(Props(), PLAY_SOUND_ACTOR_NAME)
    playSoundActor
  } 
  
  def getActorManager: ActorRef = {
    if (actorManager == null) actorManager = context.actorOf(Props(), ACTOR_MANAGER_NAME)
    actorManager
  } 
  
  def attemptToKillCharacterActor(c: Char): Unit = {
    val characterActor = lookupCharacterActor(c)

    context.watch(characterActor)
  }

  def showGameOverWindow(): Unit = {
    SwingUtilities.invokeLater(() => {
      mainFrame.removeKeyListener(myKeyListener)
      mainFrame.setGlassPane(new OverlayPanel(240, 320, "GAME OVER", new Font("Sans Serif", Font.BOLD, 60), Color.RED))
      mainFrame.getGlassPane.setVisible(true)
    })
  }
  
  def showYouWinWindow(): Unit = {
    SwingUtilities.invokeLater(() => {
      mainFrame.removeKeyListener(myKeyListener)
      mainFrame.setGlassPane(new OverlayPanel(240, 320, "YOU WIN!", new Font("Sans Serif", Font.BOLD, 60), Color.GREEN))
      mainFrame.getGlassPane.setVisible(true)
    })
  }

  def showMainFrame(): Unit = {
    SwingUtilities.invokeLater(() => {
      mainFrame.setVisible(true)
    })
  }
}


////////////////////////////////////

class OverlayPanel(
    x: Int, 
    y: Int, 
    message: String,
    font: Font,
    fontColor: Color) extends JPanel {

  setOpaque(false)
  override def paintComponent(g: Graphics): Unit = {
    val g2 = g.asInstanceOf[Graphics2D]
    g2.setFont(font)
    g2.setColor(fontColor)
    g2.drawString(message, x, y)
  }
}
