package wanderlist.model 
import net.liftweb._ 
import mapper._ 
import wanderlist.model._

class Identifier extends LongKeyedMapper[Identifier] with IdPK { 
    def getSingleton = Identifier 

    object value extends MappedString(this, 256) 
    object lastUpdated extends MappedDateTime(this)
    object service extends MappedEnum(this, AuthService)
    object contact extends MappedLongForeignKey(this, Contact)
    object owner extends MappedLongForeignKey(this, User)
}

object Identifier extends Identifier with LongKeyedMetaMapper[Identifier] {
    def createIfNeeded(value: String, service: AuthService.Value, contact: Contact, owner: User, source: AuthToken ) = {
        findAll(By(Identifier.value, value), 
                By(Identifier.service, service), 
                By(Identifier.contact, contact), 
                By(Identifier.owner, User)) match {
            case List(identifier) => {     
                IdentifierSource.findAll(By(IdentifierSource.identifier, this), By(IdentifierSource.source, source)) match {
                    case List(identifierSource) => {/* we have both objects already! */}
                    case _ => IdentifierSource.create.identifier(this).source(source).save
                }
            }
            case _ => {
                val newIdentifier = Identifier.create.value(value).service(service).contact(contact).owner(owner).saveMe
                IdentifierSource.create.identifier(newIdentifier).source(source).save
            }
        }
    }
}
