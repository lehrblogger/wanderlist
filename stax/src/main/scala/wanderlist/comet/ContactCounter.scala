package wanderlist.comet

import _root_.net.liftweb._
import http._
import common._
import actor._
import util._
import Helpers._
import _root_.scala.xml._
import _root_.java.util.Date
import S._
import SHtml._
import js._
import JsCmds._
import JE._
import net.liftweb.http.js.jquery.JqJsCmds.{AppendHtml}
import wanderlist.model._
import wanderlist.lib._

class ContactCounter extends CometActor {
    override def defaultPrefix = Full("fetchStatus")

    def render = bind("status" -> statusSpan)
    
    val counterSpanId = Helpers.nextFuncName
    var count = 0
    def statusSpan = (<span id={counterSpanId}>{count} contacts fetched</span>)
    // def completeSpan = (<span id="{counterSpanId}"></span>)
    // def errorSpan    = (<span id="{counterSpanId}">E</span>)

    override def lowPriority : PartialFunction[Any, Unit] = {
        case c: Int => {
            count = c
            println("Got count " + count + " and id = " + counterSpanId)
            partialUpdate(SetHtml(counterSpanId, Text(count.toString))) 
        }
        case FetchComplete => {
            println("Done and id = " + counterSpanId)
            println()
            partialUpdate(SetHtml(counterSpanId, "All contacts fetched")) 
        }
        case FetchError => {
            println("Error and id = " + counterSpanId)
            partialUpdate(SetHtml(counterSpanId, "Error fetching Contacts")) 
        }
    }
}
case object FetchComplete
case object FetchError


class Clock extends CometActor { 
    override def defaultPrefix = Full("clk")

    def render = bind("time" -> timeSpan)
    
    def timeSpan = (<span id="time">{timeNow}</span>)
    
    ActorPing.schedule(this, Tick, 10000L)

    override def lowPriority : PartialFunction[Any, Unit] = {
        case Tick => {
            println("Got tick " + new Date()); 
            partialUpdate(SetHtml("time", Text(timeNow.toString))) 
            ActorPing.schedule(this, Tick, 10000L)
        }
    }
}
case object Tick