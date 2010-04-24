package wanderlist.lib
import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._

object TwitterService extends OauthProvider with ContactSource  {
    val service = Service.Twitter
    
    val GetRequestToken = "request_token"
    val AuthorizeToken  = "authorize"
    val GetAccessToken  = "access_token"
    val VerifierParameter = "oauth_token"
    
    val extras = Map.empty[String, String]
    val account = :/("twitter.com") / "oauth"
    
    val api = :/("api.twitter.com") / "1"
    val contacts = api / "statuses"
    val groups = api / ""
    val user = api / "statuses" / "user_timeline.xml"

    def createIdentifiersForElemContactAccount(elem: scala.xml.Node, contact: Contact, account: Account) = {
        Identifier.createIfNeeded((elem \ "id"         ).text, IdentifierType.TwitterId    , contact, account)
        Identifier.createIfNeeded((elem \ "screen_name").text, IdentifierType.TwitterHandle, contact, account)
        Identifier.createIfNeeded((elem \ "name"       ).text, IdentifierType.FullName     , contact, account)
    }

    def saveIdentifiersForSelf(accessToken: Token, self: Contact, account: Account) = {
        val feed = h(user <<? Map("count" -> 0) <@ (consumer, accessToken) <> identity[scala.xml.Elem])
        createIdentifiersForElemContactAccount(((feed \\ "status") \ "user").theSeq.first, self, account)
    }


    def parseAndStoreContacts(account: Account, additionalGroups: List[Group])(feed: scala.xml.Elem) = {
        var count = account.contacts.length
        for (entry <- (feed \\ "user")) {
            println(entry)
            val newContact = Contact.create.owner(account.owner).saveMe
            createIdentifiersForElemContactAccount(entry, newContact, account)
            for (group <- additionalGroups) {
                ContactGroup.join(newContact, group)
            }
            count += 1
            updateSpanText(count + " contacts fetched...")
        }
    }
    def getContacts(account: Account) = {    //TODO implement paging for Twitter
        val following = Group.findAll(By(Group.groupId    , "following"  ),
                                      By(Group.owner      , account.owner),
                                      By(Group.account    , account      ),
                                      By(Group.userCreated, false        )).head
        println(following)
        h(contacts / "friends.xml"   <@ (consumer, account.token) <> parseAndStoreContacts(account, List(following)))
        updateSpanText("Got the people you're following! " + account.contacts.length + " contacts fetched.")
        
        val followers = Group.findAll(By(Group.groupId    , "followers"  ),
                                      By(Group.owner      , account.owner),
                                      By(Group.account    , account      ),
                                      By(Group.userCreated, false        )).head
        println(followers)
        h(contacts / "followers.xml" <@ (consumer, account.token) <> parseAndStoreContacts(account, List(followers)))
        updateSpanText("Got your followers too! " + account.contacts.length + " contacts fetched.")
    }
    
    def parseAndStoreGroups(account: Account)(feed: scala.xml.Elem) = {}
    def getGroups(account: Account) = {
        val twitterGroups = List("following", "followers")
        for (twitterGroup <- twitterGroups) {
            Group.create.name(twitterGroup)
                        .groupId(twitterGroup)
                        .owner(account.owner)
                        .account(account)
                        .userCreated(false).save
        }
        //TODO get lists here! but you can have people on Lists you aren't friends with. ugh.
    }
}