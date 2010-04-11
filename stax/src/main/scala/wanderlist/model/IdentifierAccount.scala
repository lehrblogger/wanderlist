package wanderlist.model 
import net.liftweb._ 
import mapper._ 

class IdentifierAccount extends LongKeyedMapper[IdentifierAccount] with IdPK {
    def getSingleton = IdentifierAccount
  
    object identifier extends MappedLongForeignKey(this, Identifier)
    object account extends MappedLongForeignKey(this, Account)
}
object IdentifierAccount extends IdentifierAccount with LongKeyedMetaMapper[IdentifierAccount] {}
