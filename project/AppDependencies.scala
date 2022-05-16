import sbt._

object AppDependencies {
  
  import play.core.PlayVersion
  import play.sbt.PlayImport._
  
  private val domainVersion            = "8.1.0-play-28"
  private val scalaTestPlusPlayVersion = "5.1.0"
  private val pegdownVersion           = "1.6.0"
  private val mockitoVersion           = "1.10.19"

  val compile = Seq(

    ws,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28" % "5.24.0",
    "uk.gov.hmrc"       %% "domain"                    % domainVersion,
    "com.typesafe.play" %% "play-json-joda"            % "2.9.2"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc"            %% "bootstrap-test-play-28" % "5.24.0"                 % scope,
        "org.scalatestplus.play" %% "scalatestplus-play"     % scalaTestPlusPlayVersion % scope,
        "org.pegdown"            %  "pegdown"                % pegdownVersion           % scope,
        "com.typesafe.play"      %% "play-test"              % PlayVersion.current      % scope,
        "org.mockito"            %  "mockito-all"            % mockitoVersion           % scope,
        "org.mockito"            %  "mockito-core"           % "4.5.1"                  % scope,
        "org.scalatestplus"      %% "mockito-3-12"           % "3.2.10.0"               % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "org.pegdown"       %  "pegdown"   % pegdownVersion      % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
