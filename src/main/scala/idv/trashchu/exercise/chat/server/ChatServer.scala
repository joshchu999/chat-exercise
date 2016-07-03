package idv.trashchu.exercise.chat.server

import akka.actor.{Actor, ActorIdentity, ActorRef, Identify, Props, ReceiveTimeout}
import akka.event.Logging
import akka.util.Timeout
import akka.pattern.ask
import idv.trashchu.exercise.chat._

import scala.collection.mutable.HashMap
import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * 2016/6/30
  */
class ChatServer (val storagePath: String) extends Actor {
  val log = Logging(context.system, this)

  sendIdentifyRequest()

  def sendIdentifyRequest(): Unit = {
    context.actorSelection(storagePath) ! Identify(storagePath)
    import context.dispatcher
    context.system.scheduler.scheduleOnce(3.seconds, self, ReceiveTimeout)
  }

  def receive = identifying

  def identifying: Actor.Receive = {
    case ActorIdentity(`storagePath`, Some(actor)) =>
      log.info(s"Chat storage actor is OK: $storagePath")
      context.watch(actor)
      context.become(active(actor))

    case ActorIdentity(`storagePath`, None) =>
      log.info(s"Chat storage actor is not available: $storagePath")

    case ReceiveTimeout =>
      log.info("Waiting for chat storage is timeout")
      sendIdentifyRequest()

    case msg @ _ => log.info(s"Not ready yet: $msg")
  }


  val sessions: HashMap[String, ActorRef] = new HashMap[String, ActorRef]

  log.info("Chat server is starting up...")

  // actor message handler
  def active(actor: ActorRef): Actor.Receive = {
    case Login(username) =>
      log.info(s"User $username has logged in")
      val session = context.actorOf(Props(classOf[Session], username), s"session:$username")
      sessions += (username -> session)

    case Logout(username) =>
      log.info(s"User $username has logged out")
      val session = sessions(username)
      context stop session
      sessions -= username

    case msg @ ChatMessage(from, message) =>
      log.info(s"New chat message: $message")
      if (sessions.contains(from))
        actor ! msg

    case msg @ GetChatLog(from) =>
      log.info(s"$from gets chat log")
      if (sessions.contains(from)) {
        implicit val timeout = Timeout(5.seconds)
        val future = actor ? msg
        val result = Await.result(future, timeout.duration).asInstanceOf[ChatLog]
        sender ! result
      }
  }

  override def postStop() = {
    log.info("Chat server is shutting down...")
    shutdownSessions()
  }

  private def shutdownSessions() =
    sessions.foreach { case (_, session) => context stop session }
}
