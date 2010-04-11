package wanderlist.model 

import _root_.net.liftweb.common._
import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
 
class ToDo extends LongKeyedMapper[ToDo] with IdPK { 
 def getSingleton = ToDo 
 
 object done extends MappedBoolean(this) 
 object owner extends MappedLongForeignKey(this, User) 
 object priority extends MappedInt(this) { 
    override def defaultValue = 5 

    override def validations = validPriority _ :: super.validations 

    def validPriority(in: Int): List[FieldError] = 
    if (in > 0 && in <= 10) Nil 
    else List(FieldError(this, <b>Priority must be 1-10</b>)) 

    override def _toForm = Full(select(ToDo.priorityList, 
                             Full(is.toString), 
                             f => set(f.toInt))) 
  }
  object desc extends MappedPoliteString(this, 128) { 
     override def validations = 
      valMinLen(3, "Description must be 3 characters") _ :: 
        super.validations 
   }
}
object ToDo extends ToDo with LongKeyedMetaMapper[ToDo] { 
 lazy val priorityList = (1 to 10). 
      map(v => (v.toString, v.toString)) 
}