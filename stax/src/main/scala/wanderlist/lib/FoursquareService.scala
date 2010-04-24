package wanderlist.lib
import net.liftweb.mapper._
import net.liftweb.util.Props
import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import wanderlist.model._

import net.liftweb._ 
import http._ 
import SHtml._ 
import S._ 
import js._ 
import JsCmds._ 
import mapper._ 
import util._ 
import Helpers._ 

object FoursquareService extends OauthProvider with ContactSource {
    val service = Service.Foursquare
    
    val GetRequestToken = "request_token"
    val AuthorizeToken  = "authorize"
    val GetAccessToken  = "access_token"
    val VerifierParameter = "oauth_token"
    
    val extras = Map.empty[String, String]
    val account = :/("foursquare.com") / "oauth"
    val api = :/("api.foursquare.com") / "v1"
    val contacts = api / "friends"
    val groups = api / ""
    val user = api / "user"
    
    def createIdentifiersForElemAndContact(elem: scala.xml.Node, contact: Contact, account: Account) = {
        Identifier.createIfNeeded((elem \ "id"       ).text                                 , IdentifierType.FoursquareId , contact, account)
        Identifier.createIfNeeded((elem \ "firstname").text + " " + (elem \ "lastname").text, IdentifierType.FullName     , contact, account)
        Identifier.createIfNeeded((elem \ "phone"    ).text                                 , IdentifierType.Phone        , contact, account)
        Identifier.createIfNeeded((elem \ "email"    ).text                                 , IdentifierType.Email        , contact, account)
        Identifier.createIfNeeded((elem \ "twitter"  ).text                                 , IdentifierType.TwitterHandle, contact, account)
        Identifier.createIfNeeded((elem \ "facebook" ).text                                 , IdentifierType.FacebookId   , contact, account)
    }
    
    def saveIdentifiersForSelf(accessToken: Token, self: Contact, account: Account) = {
        val feed = h(user <<? Map("count" -> 0) <@ (consumer, accessToken) <> identity[scala.xml.Elem])
        createIdentifiersForElemAndContact(feed, self, account)
    }
    
    def parseAndStoreContacts(account: Account)(feed: scala.xml.Elem) = {
        var count = 0
        for (entry <- (feed \\ "user")) {
            val newContact = Contact.create.owner(account.owner).saveMe
            createIdentifiersForElemAndContact(entry, newContact, account)
            val group = Group.findAll(By(Group.name       , "friends"              ),
                                      By(Group.groupId    , "friends"              ),
                                      By(Group.owner      , account.owner),
                                      By(Group.account    , account                ),
                                      By(Group.userCreated, false                  )).head
            ContactGroup.join(newContact, group)
            count += 1
            updateSpanText(count + " contacts fetched...")
        }
        updateSpanText("All done! " + count + " contacts fetched.")
    } 
    def getContacts(account: Account) = {
        h(contacts <@ (consumer, account.token) <> parseAndStoreContacts(account))
    }
    
    def parseAndStoreGroups(account: Account)(feed: scala.xml.Elem) = {}
    def getGroups(account: Account) = {
        val foursquareGroups = List("friends", "following", "followers")
        for (foursquareGroup <- foursquareGroups) {
            Group.create.name(foursquareGroup).groupId(foursquareGroup).owner(account.owner).account(account).userCreated(false).save
        }
    }
}

