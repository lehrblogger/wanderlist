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
    val Google     = Value(1, "google")
    val Foursquare = Value(2, "foursquare")
    val Twitter    = Value(3, "twitter")
    val Vimeo      = Value(4, "vimeo")
    // val Hotpotato  = Value(4, "hotpotato")
    // val Facebook   = Value(5, "facebook")
}

object RelationshipType extends Enumeration {
    val None    = Value(1, "none")
    val Contact = Value(2, "contact")
    val Friend  = Value(3, "friend")
    val Family  = Value(4, "family")
    val Blocked = Value(5, "blocked")
}