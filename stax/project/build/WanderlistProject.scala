import sbt._

class WanderlistProject(info: ProjectInfo) extends DefaultWebProject(info) with stax.StaxPlugin {
  override def staxApplicationId = "wanderlist"

  // Libraries
  val lift = "net.liftweb" % "lift-core" % "2.0-M2"
  val dispatch_http = "net.databinder" %% "dispatch-http" % "0.7.2"
  val dispatch_futures = "net.databinder" %% "dispatch-futures" % "0.7.2"
  val dispatch_oauth = "net.databinder" %% "dispatch-oauth" % "0.7.2"

  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.14" % "test"
  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided"
  val derby = "org.apache.derby" % "derby" % "10.2.2.0" % "runtime"
  val mysql = "mysql" % "mysql-connector-java" % "5.1.6" % "runtime"
  val junit = "junit" % "junit" % "3.8.1" % "test"

  // Repositories
  val nexusRepo = "nexus" at "https://nexus.griddynamics.net/nexus/content/groups/public"
}
