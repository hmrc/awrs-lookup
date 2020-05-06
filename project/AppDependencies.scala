import sbt._

object AppDependencies {
  
  import play.core.PlayVersion
  import play.sbt.PlayImport._
  
  private val domainVersion = "5.8.0-play-26"
  private val hmrcTestVersion = "3.9.0-play-26"
  private val scalaTestVersion = "3.0.8"
  private val scalaTestPlusPlayVersion = "3.1.3"
  private val pegdownVersion = "1.6.0"
  private val mockitoVersion = "1.10.19"

  val compile = Seq(

    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.7.0",
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
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
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
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
