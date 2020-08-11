import sbt._

object AppDependencies {
  
  import play.core.PlayVersion
  import play.sbt.PlayImport._
  
  private val domainVersion = "5.9.0-play-27"
  private val scalaTestPlusPlayVersion = "4.0.3"
  private val pegdownVersion = "1.6.0"
  private val mockitoVersion = "1.10.19"

  val compile = Seq(

    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-27" % "2.24.0",
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "com.typesafe.play" %% "play-json-joda" % "2.6.14"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-all" % mockitoVersion % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
