package wanderlist.model 
import _root_.net.liftweb.mapper._ 

class AuthToken extends LongKeyedMapper[AuthToken] with IdPK {
    def getSingleton = AuthToken
    object accountId extends MappedString(this, 256)
    object owner extends MappedLongForeignKey(this, User)
    object accessTokenKey extends MappedString(this, 256)
    object accessTokenSecret extends MappedString(this, 256)
    object provider extends MappedEnum(this, AuthService)
    object authenticated extends MappedBoolean(this)
}

object AuthToken extends AuthToken with LongKeyedMetaMapper[AuthToken]

object AuthService extends Enumeration {
    val Name       = Value(1, "name")
    val Email      = Value(2, "email")
    val Phone      = Value(3, "phone")
    val Google     = Value(4, "google")
    val Foursquare = Value(5, "foursquare")
    val Twitter    = Value(6, "twitter")
    val Hotpotato  = Value(7, "hotpotato")
    val Facebook   = Value(8, "facebook")
}
