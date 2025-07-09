import sbt.Keys.scalacOptions
import uk.gov.hmrc.DefaultBuildSettings.*
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName: String = "awrs-lookup"

lazy val appDependencies : Seq[ModuleID] = AppDependencies()
lazy val plugins : Seq[Plugins] = Seq.empty
lazy val playSettings : Seq[Setting[?]] = Seq.empty

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages  := "<empty>;app.*;config.*;Reverse.*;.*AuthService.*;models/.data/..*;view.*;uk.gov.hmrc.BuildInfo;uk.gov.hmrc.awrslookup;prod.*;testOnlyDoNotUseInAppConf.*",
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins((Seq(play.sbt.PlayScala, SbtDistributablesPlugin) ++ plugins) *)
  .settings((playSettings ++ scoverageSettings) *)
  .settings(majorVersion := 0)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    scalaVersion := "2.13.16",
    scalacOptions ++= Seq("-feature", "-Wconf:src=routes/.*:s"),
    libraryDependencies ++= appDependencies,
    retrieveManaged := true
  )

  .settings(resolvers ++= Seq(
  ))
  .disablePlugins(JUnitXmlReportPlugin)