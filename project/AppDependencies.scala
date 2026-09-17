import sbt.*
import play.sbt.PlayImport.*

object AppDependencies {

  private val domainVersion            = "13.0.0"
  private val bootstrapVersion         = "10.8.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "domain-play-30"            % domainVersion
  )
  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test,
  )

  def apply(): Seq[ModuleID] = compile ++ test
}

