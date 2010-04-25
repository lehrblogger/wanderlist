package wanderlist.lib
import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._
import java.util.Date
import java.text.SimpleDateFormat

object GoogleService extends OauthProvider with ContactSource  {
    val service = Service.Google

    val api = :/("www.google.com").secure / "m8" / "feeds"
    val user = api / "contacts"
    val groups = api / "groups"
    val contacts = user
    
    val GetRequestToken = "OAuthGetRequestToken"
    val AuthorizeToken  = "OAuthAuthorizeToken"
    val GetAccessToken  = "OAuthGetAccessToken"
    val VerifierParameter = "oauth_verifier"

    val extras = Map("scope" -> api.to_uri.toString, "oauth_callback" -> (:/(Props.get("host").open_!) / "service" / callback).to_uri.toString)
    val account = :/("www.google.com").secure / "accounts" 

    def identifierPairListFromElem(elem: scala.xml.Node) = { 
        var identifierPairList = List(((elem \ "id"   ).text       , IdentifierType.GoogleId),
                                      ((elem \ "title").text       , IdentifierType.FullName))
        for (email <- (elem \\ "email")) {
            identifierPairList ::= ((email \ "@address").toString, IdentifierType.Email)
        }
        for (phone <- (elem \\ "phoneNumber")) {
            identifierPairList ::= (phone.text, IdentifierType.Phone)
        }
        identifierPairList
    }
    
    def saveIdentifiersForSelf(accessToken: Token, self: Contact, account: Account) = {
        val feed = h(user / "default" / "full" <<? Map("max-results" -> 0) <@ (consumer, accessToken) <> identity[scala.xml.Elem])
        Identifier.createIfNeeded( (feed \  "id"              ).text, IdentifierType.GoogleId, self, account)
        Identifier.createIfNeeded(((feed \ "author") \ "name" ).text, IdentifierType.FullName, self, account)
        Identifier.createIfNeeded(((feed \ "author") \ "email").text, IdentifierType.Email   , self, account)
    }
    
    def parseAndStoreContacts(account: Account, additionalGroups: List[Group])(feed: scala.xml.Elem) = {
        var count = 0
        for (entry <- (feed \\ "entry")) {
            val newContact = findContactForIdentifiersOrCreate(entry, account)
            createIdentifiersForElemContactAccount(entry, newContact, account)
            for (googleGroup <- (entry \\ "groupMembershipInfo")) {
                val group = Group.findAll(By(Group.groupId    , (googleGroup \ "@href").toString),
                                          By(Group.owner      , account.owner                   ),
                                          By(Group.account    , account                         ),
                                          By(Group.userCreated, true                            )).head
                ContactGroup.join(newContact, group)
            }
            count += 1
            updateSpanText(count + " contacts fetched...")
        }
    }
    def getContacts(account: Account) = {
        h(contacts / "default" / "full" <<? Map("max-results" -> 20000) <@ (consumer, account.token) <> parseAndStoreContacts(account, List()))
        updateSpanText("All done! " + account.contacts.length + " contacts fetched.")
    }
    
    def parseAndStoreGroups(account: Account)(feed: scala.xml.Elem) = {
        for (entry <- (feed \\ "entry")) {
            val name = (entry \ "title").text
            val googleId = (entry \ "id").text
            Group.create.name(name).groupId(googleId).owner(account.owner).account(account).userCreated(true).save
        }
    }
    def getGroups(account: Account) = {
        h(groups / "default" / "full" <<? Map("max-results" -> 20000) <@ (consumer, account.token) <> parseAndStoreGroups(account))
    }

}