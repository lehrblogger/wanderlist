package wanderlist.model

import _root_.net.liftweb.common._
import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._

class User extends MegaProtoUser[User] {
  def getSingleton = User // what's the "meta" server
  object selfContact extends MappedLongForeignKey(this, Contact)
}
object User extends User with MetaMegaProtoUser[User] {
  override def dbTableName = "users"
  override def screenWrap = Full(<lift:surround with="default" at="content"><lift:bind /></lift:surround>)
  override def skipEmailValidation = true  // comment this line out to require email validations
}
