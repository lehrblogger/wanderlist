package wanderlist.model 
import _root_.net.liftweb.mapper._ 
import dispatch.oauth._
import scala.collection.mutable.HashSet


class Account extends LongKeyedMapper[Account] with IdPK {
    def getSingleton = Account
    object owner extends MappedLongForeignKey(this, User)
    object accessTokenValue extends MappedString(this, 256)
    object accessTokenSecret extends MappedString(this, 256)
    object service extends MappedEnum(this, Service)
    object authenticated extends MappedBoolean(this)
    object notes extends MappedString(this, 512)
    
    def token = Token(accessTokenValue, accessTokenSecret)
    
    def identifiers = IdentifierAccount.findAll(By(IdentifierAccount.account, this.id)).map(_.identifier.obj.open_!)
    
    def contacts = { 
        val contactSet = new HashSet[Contact]
        for (identifier <- this.identifiers) {
            val contact = identifier.contact.obj.open_!
            if (contact != owner.obj.open_!.selfContact.obj.open_!) { contactSet += contact }
        }
        contactSet.toList
    }
}
object Account extends Account with LongKeyedMetaMapper[Account] {
    override def dbTableName = "accounts"
}

object Service extends Enumeration {
    val Google     = Value(1, "google")
    val Foursquare = Value(2, "foursquare")
    val Twitter    = Value(3, "twitter")
}