package wanderlist.model 
import _root_.net.liftweb.mapper._ 

class Account extends LongKeyedMapper[Account] with IdPK {
    def getSingleton = Account
    object owner extends MappedLongForeignKey(this, User)
    object accessTokenKey extends MappedString(this, 256)
    object accessTokenSecret extends MappedString(this, 256)
    object provider extends MappedEnum(this, AuthService)
    object authenticated extends MappedBoolean(this)
}
object Account extends Account with LongKeyedMetaMapper[Account]

object AuthService extends Enumeration {
    val Google     = Value(1, "google")
    val Foursquare = Value(2, "foursquare")
    val Twitter    = Value(3, "twitter")
}