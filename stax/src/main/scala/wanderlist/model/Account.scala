package wanderlist.model 
import _root_.net.liftweb.mapper._ 

class Account extends LongKeyedMapper[Account] with IdPK {
    def getSingleton = Account
    object owner extends MappedLongForeignKey(this, User)
    object accessTokenValue extends MappedString(this, 256)
    object accessTokenSecret extends MappedString(this, 256)
    object service extends MappedEnum(this, Service)
    object authenticated extends MappedBoolean(this)
    object notes extends MappedString(this, 512)
}
object Account extends Account with LongKeyedMetaMapper[Account]

object Service extends Enumeration {
    val Google     = Value(1, "google")
    val Foursquare = Value(2, "foursquare")
    val Twitter    = Value(3, "twitter")
}