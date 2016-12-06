/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import java.util.UUID

import uk.gov.hmrc.awrslookup.connectors.EtmpConnector
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import play.api.Play
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.awrslookup.connectors.EtmpConnector
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.{AppName, RunMode}
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.http.ws.{WSGet, WSPost, WSPut}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import utils.AwrsTestJson

import scala.concurrent.Future

class EtmpConnectorTest extends UnitSpec with OneServerPerSuite with MockitoSugar with BeforeAndAfter with AwrsTestJson {

  object TestAuditConnector extends AuditConnector with AppName with RunMode {
    override lazy val auditingConfig = LoadAuditingConfig("auditing")
  }

  class MockHttp extends WSGet with WSPost with WSPut with HttpAuditing {
    override val hooks = Seq(AuditingHook)

    override def auditConnector: AuditConnector = TestAuditConnector

    override def appName = Play.configuration.getString("appName").getOrElse("awrs-lookup")
  }

  val mockWSHttp = mock[MockHttp]

  object TestEtmpConnector extends EtmpConnector {
    override val http = mockWSHttp
    override val urlHeaderEnvironment: String = config("etmp-hod").getString("environment").getOrElse("")
    override val urlHeaderAuthorization: String = s"Bearer ${config("etmp-hod").getString("authorization-token").getOrElse("")}"
  }

  before {
    reset(mockWSHttp)
  }

  "EtmpConnector" should {

    "lookup an application with a valid reference number " in {
      val lookupSuccess = AwrsTestJson.businessJson
      val awrsRefNo = "XAAW0000012345"
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      when(mockWSHttp.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(200, responseJson = Some(lookupSuccess))))
      val result = TestEtmpConnector.lookup(awrsRefNo)
      await(result).json shouldBe lookupSuccess
    }

  }

}
