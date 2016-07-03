package idv.trashchu.exercise.chat.server

import akka.actor.Actor
import akka.event.Logging

/**
  * 2016/6/30
  */
class Session(user: String) extends Actor {
  val log = Logging(context.system, this)

  private val loginTime = System.currentTimeMillis

  log.info(s"New session for user $user has been created at $loginTime")

  def receive = {
    case _ =>
  }

  override def postStop() = {
    log.info(s"Session $user is shutting down...")
  }
}
