package wanderlist.model 
import net.liftweb._ 
import mapper._ 
import wanderlist.model._

class Identifier extends LongKeyedMapper[Identifier] with IdPK { 
    def getSingleton = Identifier 

    object value extends MappedString(this, 256) 
    object lastUpdated extends MappedDateTime(this)
    object idType extends MappedEnum(this, IdentifierType)
    object contact extends MappedLongForeignKey(this, Contact)
    object owner extends MappedLongForeignKey(this, User)
}
object Identifier extends Identifier with LongKeyedMetaMapper[Identifier] {
    def createIfNeeded(value: String, idType: IdentifierType.Value, contact: Contact, owner: User, account: Account ): Unit = {
        if (value == "") return
        findAll(By(Identifier.value, value), 
                By(Identifier.idType, idType), 
                By(Identifier.contact, contact), 
                By(Identifier.owner, User)) match {
            case List(identifier) => {     
                IdentifierAccount.findAll(By(IdentifierAccount.identifier, this), By(IdentifierAccount.account, account)) match {
                    case List(identifierAccount) => {/* we have both objects already! */}
                    case _ => IdentifierAccount.create.identifier(this).account(account).save
                }
            }
            case _ => {
                val newIdentifier = Identifier.create.value(value).idType(idType).contact(contact).owner(owner).saveMe
                IdentifierAccount.create.identifier(newIdentifier).account(account).save
            }
        }
    }
}

object IdentifierType extends Enumeration {
    val FullName      = Value(1, "full_name")
    val Email         = Value(2, "email")
    val Phone         = Value(3, "phone")
    val GoogleId      = Value(4, "google_id")
    val FoursquareId  = Value(5, "foursquare_id")
    val TwitterId     = Value(6, "twitter_id")
    val TwitterHandle = Value(7, "twitter_handle") //TODO these can change so should not be Identifiers
    val FacebookId    = Value(8, "facebook_id")
}

