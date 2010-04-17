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


class ContactFetcher extends CometActor { 
    override def defaultPrefix = Full("accountStatus")
    
    def render = bind("count" -> countSpan)
         
    def countSpan = (<span id="time">{timeNow}</span>)
    
    override def lowPriority : PartialFunction[Any, Unit] = {
        case source: Service.Value => {
            println("Got source " + source); 
            partialUpdate(SetHtml("time", Text(countSpan.toString))) 
            ActorPing.schedule(this, source, 10000L)
        }
    }
}

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