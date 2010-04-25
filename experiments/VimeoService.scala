package wanderlist.lib
import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._

object VimeoService extends OauthProvider {
    val service = Service.Vimeo
    
    val GetRequestToken = "request_token"
    val AuthorizeToken  = "authorize"
    val GetAccessToken  = "access_token"
    
    val extras = Map.empty[String, String]
    val account = :/("vimeo.com") / "oauth"
    
    val api = :/("api.twitter.com") / "1"
    val contacts = api / "statuses" / "friends.xml"
    
    def getContacts() = {
        val authToken = getAccountForUser(User.currentUser.open_!)
        def parseAndStoreContacts(account: Account, additionalGroups: List[Group])(feed: scala.xml.Elem) = {
            for (entry <- (feed \\ "user")) {
                val newContact = Contact.create.owner(User.currentUser.open_!).saveMe
                Identifier.createIfNeeded((entry \ "id").text         , IdentifierType.TwitterId    , newContact, authToken)
                Identifier.createIfNeeded((entry \ "screen_name").text, IdentifierType.TwitterHandle, newContact, authToken)
                val name = (entry \ "name").text 
                if (name != "") {
                    Identifier.createIfNeeded(name                    , IdentifierType.FullName     , newContact, authToken)
                }
            }
        }
        // <contacts on_this_page="1" page="1" perpage="50" total="1">
        //   <contact display_name="Blake Whitman" id="151382" is_plus="1" is_staff="1" mutual="1" profileurl="http://www.vimeo.com/blakewhitman" realname="Blake Whitman" username="blakewhitman" videosurl="http://www.vimeo.com/blakewhitman/videos">
        //     <portraits>
        //       <portrait height="30" width="30">http://20.media.vimeo.com/...jpg</portrait>
        //       <portrait height="75" width="75">http://80.media.vimeo.com/...jpg</portrait>
        //       <portrait height="100" width="100">http://80.media.vimeo.com/...jpg</portrait>
        //       <portrait height="300" width="300">http://20.media.vimeo.com/...jpg</portrait>
        //     </portraits>
        //   </contact>
        // </contacts>
        val accessToken = Token(authToken.accessTokenValue, authToken.accessTokenSecret)
        h(contacts <@ (consumer, accessToken) <> parseAndStoreContacts)
        //TODO implement paging for Twitter
    }
}

