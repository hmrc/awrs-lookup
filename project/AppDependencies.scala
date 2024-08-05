import sbt._

object AppDependencies {
  
  import play.sbt.PlayImport._
  
  private val domainVersion            = "10.0.0"
  private val bootstrapVersion         = "9.1.0"
  private val scalaTestPlusPlayVersion = "7.0.1"
  private val mockitoVersion           = "1.10.19"

  val compile: Seq[ModuleID] = Seq(

    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "domain-play-30"            % domainVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[sbt.ModuleID] = new TestDependencies {
      override lazy val test: Seq[sbt.ModuleID] = Seq(
        "uk.gov.hmrc"            %% "bootstrap-test-play-30" % bootstrapVersion         % scope,
        "org.scalatestplus.play" %% "scalatestplus-play"     % scalaTestPlusPlayVersion % scope,
        "org.mockito"            %  "mockito-all"            % mockitoVersion           % scope,
        "org.mockito"            %  "mockito-core"           % "5.11.0"                 % scope,
        "org.scalatestplus"      %% "mockito-4-11"           % "3.2.18.0"               % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}
