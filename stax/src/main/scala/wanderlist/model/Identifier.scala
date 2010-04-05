package wanderlist.model 
import net.liftweb._ 
import mapper._ 

class Identifier extends LongKeyedMapper[Identifier] with IdPK { 
    def getSingleton = Identifier 

    object value extends MappedString(this, 256) 
    object service extends MappedEnum(this, AuthService)
    object contact extends MappedLongForeignKey(this, Contact)
}

object Identifier extends Identifier with LongKeyedMetaMapper[Identifier]
