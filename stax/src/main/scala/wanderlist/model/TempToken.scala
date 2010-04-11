package wanderlist.model 
import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
import wanderlist.model._

class TempToken extends LongKeyedMapper[TempToken] with IdPK { 
    def getSingleton = TempToken 
    object owner extends MappedLongForeignKey(this, User)
    object key extends MappedString(this, 256)
    object secret extends MappedString(this, 256)
}
object TempToken extends TempToken with LongKeyedMetaMapper[TempToken]
