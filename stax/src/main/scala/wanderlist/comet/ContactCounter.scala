package wanderlist.comet

import _root_.net.liftweb._
import http._
import common._
import actor._
import util._
import Helpers._
import _root_.scala.xml._
import _root_.scala.collection.mutable._
import _root_.java.util.Date
import S._
import SHtml._
import js._
import JsCmds._
import JE._
import net.liftweb.http.js.jquery.JqJsCmds.{AppendHtml}
import wanderlist.model._
import wanderlist.lib._

class ContactCounter extends CometActor { //with CometListener { 
    override def defaultPrefix = Full("fetchStatus")

    def render = bind("status" -> statusSpan)
    def statusSpan = (<span id={name.open_!}>click to get your contacts for this account</span>)
    
    override def lowPriority: PartialFunction[Any, Unit] = {
        case newText:String => {
            println("Got CounterUpdate " + name.open_! + "," + newText)
            partialUpdate(SetHtml(name.open_!, Text(newText)))
        }
        
    }
    //def registerWith = CounterMaster
    
    override def localSetup = {
        CounterMaster ! SubscribeCounter(name.open_!, this)
        super.localSetup()
    }
    override def localShutdown = {
        CounterMaster ! UnsubscribeCounter(name.open_!)
        super.localShutdown()
    }
}
 
object CounterMaster extends LiftActor with ListenerManager { 
    private var counters = new HashMap[String, ContactCounter]
    
    override def mediumPriority: PartialFunction[Any, Unit]  = {
        case SubscribeCounter(name, counter) => counters.put(name, counter)
        case UnsubscribeCounter(name)        => counters -= name
    }
    override def lowPriority: PartialFunction[Any, Unit]     = { 
        case CounterUpdate(name, newText)    => counters.get(name) match {
            case None => println("That ContactCounter is gone!")
            case Some(x) => x ! newText
        }
    }
    
    def createUpdate = {}
}
case class CounterUpdate(name: String, newText: String)
case class UnsubscribeCounter(name:String)
case class SubscribeCounter(name: String, counter: ContactCounter)