package io

import io.webhook._
import play.api.libs.json._

object Deserializer {
  private implicit val eventReads: Reads[Event] = EventDeserializer.r

  implicit val r: Reads[Request] = Json.reads[Request]
  object EventDeserializer {
    implicit val r: Reads[Event] = (json: JsValue) =>
      json \ "type" match {
        case JsDefined(JsString(MessageEventDeserializer.eventType))  => MessageEventDeserializer.r.reads(json)
        case JsDefined(JsString(PostbackEventDeserializer.eventType)) => PostbackEventDeserializer.r.reads(json)
        case JsDefined(JsString(t))                                   => JsSuccess(IgnorableTypeEvent(t))
        case _                                                        => throw new Exception("Could not find a string value 'type' in an Event json.")
      }
  }

  object MessageEventDeserializer {
    private implicit val eventSourceReads: Reads[EventSource] = EventSourceDeserializer.r
    private implicit val messageReads: Reads[Message] = MessageDeserializer.r

    val r: Reads[MessageEvent] = Json.reads[MessageEvent]
    val eventType = "message"
  }
  object PostbackEventDeserializer {

    private implicit val eventSourceReads: Reads[EventSource] = EventSourceDeserializer.r
    private implicit val postbackReads: Reads[Postback] = Json.reads[Postback]

    val r: Reads[PostbackEvent] = Json.reads[PostbackEvent]
    val eventType = "postback"
  }
  object EventSourceDeserializer {
    implicit val r: Reads[EventSource] = (json: JsValue) =>
      json \ "type" match {
        case JsDefined(JsString(UserDeserializer.eventSourceType)) => UserDeserializer.r.reads(json)
        case JsDefined(JsString(t))                                => JsSuccess(IgnorableTypeEventSource(t))
        case _                                                     => throw new Exception("Could not find a string value 'type' in an EventSource json.")
      }
  }

  object UserDeserializer {
    val r: Reads[User] = Json.reads[User]
    val eventSourceType = "user"
  }

  object MessageDeserializer {
    implicit val r: Reads[Message] = (json: JsValue) =>
      json \ "type" match {
        case JsDefined(JsString(TextMessageDeserializer.messageType))     => TextMessageDeserializer.r.reads(json)
        case JsDefined(JsString(LocationMessageDeserializer.messageType)) => LocationMessageDeserializer.r.reads(json)
        case JsDefined(JsString(t))                                       => JsSuccess(IgnorableTypeMessage((json \ "id").as[String], t))
        case _                                                            => throw new Exception("Could not find a string value 'type' in a Message json.")
      }
  }

  object TextMessageDeserializer {
    val r: Reads[TextMessage] = Json.reads[TextMessage]
    val messageType = "text"
  }

  object LocationMessageDeserializer {
    val r: Reads[LocationMessage] = Json.reads[LocationMessage]
    val messageType = "location"
  }
}