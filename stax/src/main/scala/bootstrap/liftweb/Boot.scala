package bootstrap.liftweb

import _root_.net.liftweb.common._
import _root_.net.liftweb.util._
import _root_.net.liftweb.http._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import _root_.net.liftweb.mapper.{DB, ConnectionManager, Schemifier, DefaultConnectionIdentifier, ConnectionIdentifier}
import _root_.java.sql.{Connection, DriverManager}
import _root_.wanderlist.model._
import _root_.wanderlist.lib._
import _root_.net.liftweb.http.provider.HTTPRequest
import _root_.dispatch._

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
    def boot {
    if (!DB.jndiJdbcConnAvailable_?)
        DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)

    // where to search snippet
    LiftRules.addToPackages("wanderlist") 
    Schemifier.schemify(true, Log.infoF _, User, ToDo, Account, Contact, ContactGroup, Group, Identifier, IdentifierAccount, TempToken)

    Log.info("Hostname: " + Props.hostName)
    Log.info("Username: " + Props.userName)
    Log.info("Run mode: " + Props.mode)
    Log.info("Database driver: " + DBVendor.driverName)
    Log.info("Database url: " + DBVendor.dbUrl)
	
    // Build SiteMap
    val entries = Menu(Loc("Home"    , List("index")   , "Home"              )) ::
                  Menu(Loc("Add"     , List("add")     , "Add an Account"    )) ::
                  Menu(Loc("Accounts", List("accounts"), "View your Accounts")) ::
                  Menu(Loc("Groups"  , List("groups")  , "View your Groups"  )) ::
                  Menu(Loc("Contacts", List("contacts"), "View your Contacts")) ::
                  User.sitemap
    LiftRules.setSiteMap(SiteMap(entries:_*))

    val oauthServices = List(FoursquareService, GoogleService, TwitterService)
    for (oauthService <- oauthServices) {
        LiftRules.dispatch.append {
            case Req("service" :: oauthService.start    :: Nil, _, _) => {
                 S.redirectTo(oauthService.getRequestUrl)
            }
            case Req("service" :: oauthService.callback :: Nil, _, _) => {
                val verifier = java.net.URLDecoder.decode(S.param(oauthService.VerifierParameter).open_!, "UTF-8")
                oauthService.exchangeToken(verifier)
                S.redirectTo("http://" + Props.get("host").open_! + "/accounts")
            }
        }
    }
    
    // case Req("service" :: GoogleService.callback :: Nil, _, _) => {
    //     val verifier = java.net.URLDecoder.decode(S.param("oauth_verifier").open_!, "UTF-8")
    // case Req("service" :: FoursquareService.callback ::  Nil, _, _) => {
    //     val token = java.net.URLDecoder.decode(S.param("oauth_token").open_!, "UTF-8")
    // case Req("service" :: TwitterService.callback ::  Nil, _, _) => {
    //     val token = java.net.URLDecoder.decode(S.param("oauth_token").open_!, "UTF-8")
        

    // Show the spinny image when an Ajax call start
    LiftRules.ajaxStart =
        Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
        Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(makeUtf8)

    S.addAround(DB.buildLoanWrapper)
    }

    /**
    * Force the request to be UTF-8
    */
    private def makeUtf8(req: HTTPRequest) {
        req.setCharacterEncoding("UTF-8")
    }

}

/**
* Database connection calculation
*/
object DBVendor extends ConnectionManager {
  val driverName = Props.get("db.driver") openOr "org.apache.derby.jdbc.EmbeddedDriver"
  val dbUrl      = Props.get("db.url")    openOr "jdbc:derby:lift_example;create=true"

  def newConnection(name: ConnectionIdentifier): Box[Connection] = {
    try {
      Class.forName(driverName)

      val dm = (Props.get("db.user"), Props.get("db.password")) match {
        case (Full(user), Full(pwd)) => DriverManager.getConnection(dbUrl, user, pwd)
        case _ =>                       DriverManager.getConnection(dbUrl)
      }

      Full(dm)
    } catch {
      case e : Exception => e.printStackTrace; Empty
    }
  }
  def releaseConnection(conn: Connection) {conn.close}
}
