import sbt._
class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  // Libraries
  val stax = "eu.getintheloop" % "sbt-stax-plugin" % "1.0"

  // Repositories
  val staxReleases = "stax-release-repo" at "http://mvn.stax.net/content/repositories/public"
}
