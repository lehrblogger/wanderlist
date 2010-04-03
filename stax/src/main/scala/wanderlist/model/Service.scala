package wanderlist.model 
import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
import wanderlist.model._
 
class HotpotatoService extends LongKeyedMapper[HotpotatoService] with IdPK { 
  def getSingleton = HotpotatoService 
  object name extends MappedPoliteString(this, 256)
  object owner extends MappedLongForeignKey(this, User)
}
object HotpotatoService extends HotpotatoService with LongKeyedMetaMapper[HotpotatoService] {}

class HotpotatoServiceContact extends LongKeyedMapper[HotpotatoServiceContact] with IdPK {
    def getSingleton = HotpotatoServiceContact
    object service extends MappedLongForeignKey(this, HotpotatoService)
    object contact extends MappedLongForeignKey(this, Contact)
}
object HotpotatoServiceContact extends HotpotatoServiceContact with LongKeyedMetaMapper[HotpotatoServiceContact] {
    def join (s: HotpotatoService, c: Contact) = 
        this.create.service(s).contact(c).save
}

class FoursquareService extends LongKeyedMapper[FoursquareService] with IdPK { 
  def getSingleton = FoursquareService 
  object name extends MappedPoliteString(this, 256)
  object owner extends MappedLongForeignKey(this, User)
}
object FoursquareService extends FoursquareService with LongKeyedMetaMapper[FoursquareService] {}

class FoursquareServiceContact extends LongKeyedMapper[FoursquareServiceContact] with IdPK {
    def getSingleton = FoursquareServiceContact
    object service extends MappedLongForeignKey(this, FoursquareService)
    object contact extends MappedLongForeignKey(this, Contact)
}
object FoursquareServiceContact extends FoursquareServiceContact with LongKeyedMetaMapper[FoursquareServiceContact] {
    def join (s: FoursquareService, c: Contact) = 
        this.create.service(s).contact(c).save
}

