package wanderlist.model 
import net.liftweb._ 
import mapper._ 

class IdentifierSource extends LongKeyedMapper[IdentifierSource] with IdPK {
    def getSingleton = IdentifierSource
  
    object identifier extends MappedLongForeignKey(this, Identifier)
    object source extends MappedLongForeignKey(this, AuthToken)
}

object IdentifierSource extends IdentifierSource with LongKeyedMetaMapper[IdentifierSource]
