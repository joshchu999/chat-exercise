package idv.trashchu.exercise.chat.client

import akka.actor.{Actor, ActorIdentity, ActorRef, Identify, ReceiveTimeout}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import idv.trashchu.exercise.chat.{GetChatLog, _}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * 2016/6/30
  */
class ChatClient (val serverPath: String, val name: String) extends Actor {
  val log = Logging(context.system, this)

  sendIdentifyRequest()

  def sendIdentifyRequest(): Unit = {
    context.actorSelection(serverPath) ! Identify(serverPath)
    import context.dispatcher
    context.system.scheduler.scheduleOnce(3.seconds, self, ReceiveTimeout)
  }

  def receive = identifying

  def identifying: Actor.Receive = {
    case ActorIdentity(`serverPath`, Some(actor)) =>
      log.info(s"Chat server actor is OK: $serverPath")
      context.watch(actor)
      context.become(active(actor))

    case ActorIdentity(`serverPath`, None) =>
      log.info(s"Chat server actor is not available: $serverPath")

    case ReceiveTimeout =>
      log.info("Waiting for chat server is timeout")
      sendIdentifyRequest()

    case msg @ _ => log.info(s"Not ready yet $msg")
  }

  def active(actor: ActorRef): Actor.Receive = {
    case Login(_) =>
      actor ! Login(name)

    case Logout(_) =>
      actor ! Logout(name)

    case ChatMessage(_, message) =>
      actor ! ChatMessage(name, name + ": " + message)

    case GetChatLog(_) =>
      implicit val timeout = Timeout(5.seconds)
      val future = actor ? GetChatLog(name)
      val result = Await.result(future, timeout.duration).asInstanceOf[ChatLog]
      sender ! result
  }
}