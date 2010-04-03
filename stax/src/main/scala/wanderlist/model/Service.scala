package wanderlist.model 
import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
import wanderlist.model._
 
class Service extends LongKeyedMapper[Service] with IdPK { 
  def getSingleton = Service 
  object name extends MappedPoliteString(this, 256)
  object owner extends MappedLongForeignKey(this, User)
}
object Service extends Service with LongKeyedMetaMapper[Service] {}


class ServiceContact extends LongKeyedMapper[ServiceContact] with IdPK {
    def getSingleton = ServiceContact
    object service extends MappedLongForeignKey(this, Service)
    object contact extends MappedLongForeignKey(this, Contact)
}
object ServiceContact extends ServiceContact with LongKeyedMetaMapper[ServiceContact] {
    def join (s: Service, c: Contact) = 
        //TODO validate that Contact and Group have same owner
        this.create.service(s).contact(c).save
}
