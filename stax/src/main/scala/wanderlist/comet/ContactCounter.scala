package wanderlist.comet

import _root_.net.liftweb._
import mapper._
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
    override def defaultPrefix = Full("fetchStatus") //TODO make the below line less hacky - there has to be a better way to communicate the account being used
    var currentCount: Int = 0
    
    def render = {
        println("rendering " + name.open_! + " with currentCount=" + currentCount)
        bind("status" -> statusSpan)
    }
    def statusSpan = (<span id={name.open_!}>{currentCount} contacts fetched</span>)
    
    override def lowPriority: PartialFunction[Any, Unit] = {
        case newCount: Int => {
            currentCount = newCount
            partialUpdate(SetHtml(name.open_!, statusSpan))
        }
        case newText: String => {
            partialUpdate(SetHtml(name.open_!, Text(newText)))
        }
        
    }
    //def registerWith = CounterMaster
    
    override def localSetup = {
        Account.findAll(By(Account.id, name.open_!.replace("account_comet_", "").toLong)) match {
            case List(a) => currentCount = a.contacts.length
            case _       => currentCount = -1
        }
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
        case CounterUpdate(name, newAny)    => counters.get(name) match {
            case None => println("That ContactCounter is gone!")
            case Some(x) => x ! newAny
        }
    }
    
    def createUpdate = {}
}
case class CounterUpdate(name: String, newAny: Any)
case class UnsubscribeCounter(name:String)
case class SubscribeCounter(name: String, counter: ContactCounter)