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
    val Email      = Value(1, "email")
    val Phone      = Value(2, "phone")
    val Google     = Value(3, "google")
    val Foursquare = Value(4, "foursquare")
    val Twitter    = Value(5, "twitter")
    val Hotpotato  = Value(6, "hotpotato")
    val Facebook   = Value(7, "facebook")
}
