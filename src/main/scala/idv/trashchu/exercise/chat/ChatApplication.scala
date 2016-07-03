package idv.trashchu.exercise.chat

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask
import com.typesafe.config.ConfigFactory
import idv.trashchu.exercise.chat.client.ChatClient
import idv.trashchu.exercise.chat.server.ChatServer
import idv.trashchu.exercise.chat.storage.MemoryChatStorage

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * 2016/6/30
  */
object ChatApplication {
  def main(args: Array[String]): Unit = {

    val chatStoragePath = "akka.tcp://ChatStorageSystem@127.0.0.1:2552/user/chatstorage"
    val chatServerPath = "akka.tcp://ChatServerSystem@127.0.0.1:2553/user/chatserver"

    startChatStorageSystem()

    startChatServerSystem(chatStoragePath)

    val client1 = startChatClientSystem(chatServerPath, "Bob")
    val client2 = startChatClientSystem(chatServerPath, "Frank")

    Thread sleep 2000

    client1 ! Login(null)
    client2 ! Login(null)

    Thread sleep 2000

    client1 ! ChatMessage(null, "Hi there")
    client2 ! ChatMessage(null, "Hello")

    Thread sleep 2000

    client1 ! Logout(null)

    Thread sleep 2000

    client1 ! ChatMessage(null, "Hi again")

    Thread sleep 2000

    client2 ! GetChatLog(null)
    implicit val timeout = Timeout(5.seconds)
    val future = client2 ? GetChatLog(null)
    val ChatLog(result) = Await.result(future, timeout.duration).asInstanceOf[ChatLog]
    println("Chat log: \n\t" + result.mkString("\n\t"))

    Thread sleep 2000

    client2 ! Logout(null)


  }

  def startChatStorageSystem(): ActorRef = {
    val system = ActorSystem("ChatStorageSystem", ConfigFactory.load("chatstorage"))
    system.actorOf(Props[MemoryChatStorage], "chatstorage")
  }

  def startChatServerSystem(storagePath: String): ActorRef = {
    val system = ActorSystem("ChatServerSystem", ConfigFactory.load("chatserver"))
    system.actorOf(Props(classOf[ChatServer], storagePath), "chatserver")
  }

  def startChatClientSystem(serverPath: String, name: String): ActorRef = {
    val system = ActorSystem("ChatClientSystem", ConfigFactory.load("chatclient"))
    system.actorOf(Props(classOf[ChatClient], serverPath, name), "chatclient")
  }
}
