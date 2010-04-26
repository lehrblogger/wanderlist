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

    def identifierPairListFromElem(elem: scala.xml.Node) = { 
        List(((elem \ "id"         ).text, IdentifierType.TwitterId    ),
             ((elem \ "screen_name").text, IdentifierType.TwitterHandle),
             ((elem \ "name"       ).text, IdentifierType.FullName     ))
    }
    
    def saveIdentifiersForSelf(accessToken: Token, self: Contact, account: Account) = {
        val feed = h(user <<? Map("count" -> 0) <@ (consumer, accessToken) <> identity[scala.xml.Elem])
        createIdentifiersForElemContactAccount(((feed \\ "status") \ "user").theSeq.first, self, account)
    }

    def parseAndStoreContacts(account: Account, additionalGroups: List[Group])(feed: scala.xml.Elem) = {
        var count = account.contacts.length
        for (entry <- (feed \\ "user")) {
            val newContact = findContactForIdentifiersOrCreate(entry, account)
            createIdentifiersForElemContactAccount(entry, newContact, account)
            for (group <- additionalGroups) {
                ContactGroup.join(newContact, group)
            }
            count += 1
            updateSpan(count)
        }
    }
    def getContacts(account: Account) = {    //TODO implement paging for Twitter
        val following = Group.findAll(By(Group.groupId    , "following"  ),
                                      By(Group.owner      , account.owner),
                                      By(Group.account    , account      ),
                                      By(Group.userCreated, false        )).head
        h(contacts / "friends.xml"   <@ (consumer, account.token) <> parseAndStoreContacts(account, List(following)))
        updateSpan("Fetched the people you're following! " + account.contacts.length + " contacts fetched.")
        
        val followers = Group.findAll(By(Group.groupId    , "followers"  ),
                                      By(Group.owner      , account.owner),
                                      By(Group.account    , account      ),
                                      By(Group.userCreated, false        )).head
        h(contacts / "followers.xml" <@ (consumer, account.token) <> parseAndStoreContacts(account, List(followers)))
        updateSpan("Fetched your followers too! " + account.contacts.length + " contacts fetched.")
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