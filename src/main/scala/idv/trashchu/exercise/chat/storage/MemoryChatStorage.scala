package idv.trashchu.exercise.chat.storage

import akka.event.Logging
import idv.trashchu.exercise.chat.{ChatLog, ChatMessage, GetChatLog}

/**
  * 2016/6/30
  */
class MemoryChatStorage extends ChatStorage {
  val log = Logging(context.system, this)

  private var chatLog: List[String] = Nil

  log.info("Memory-based chat storage is starting up...")

  def receive = {
    case ChatMessage(_, message) =>
      log.info(s"New chat message: $message")
      chatLog ::= message

    case GetChatLog(from) =>
      log.info(s"$from gets chat log")
      sender ! ChatLog(chatLog.reverse)
  }
}
