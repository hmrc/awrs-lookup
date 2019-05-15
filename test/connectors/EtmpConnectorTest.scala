/*
 * Copyright 2019 HM Revenue & Customs
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

import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status._
import uk.gov.hmrc.awrslookup.connectors.EtmpConnector
import uk.gov.hmrc.awrslookup.utils.LoggingUtils
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import utils.AwrsTestConstants._
import utils.{AwrsTestJson, AwrsUnitTestTraits}

import scala.concurrent.Future

class EtmpConnectorTest extends AwrsUnitTestTraits {

  val mockWSHttp: DefaultHttpClient = mock[DefaultHttpClient]
  val servicesConfig: ServicesConfig = app.injector.instanceOf[ServicesConfig]
  val loggingUtils: LoggingUtils = app.injector.instanceOf[LoggingUtils]

  object TestEtmpConnector extends EtmpConnector(servicesConfig, mockWSHttp, loggingUtils)

  before {
    reset(mockWSHttp)
  }

  "EtmpConnector" should {

    "lookup an application with a valid reference number " in {
      val lookupSuccess = AwrsTestJson.businessJson
      val awrsRefNo = testRefNo
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      when(mockWSHttp.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, responseJson = Some(lookupSuccess))))
      val result = TestEtmpConnector.lookupByUrn(awrsRefNo)
      await(result).json shouldBe lookupSuccess
    }

    "lookup an application with a valid business name " in {
      val lookupSuccess = AwrsTestJson.businessJson
      val businessName = testBusinessName
      implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      when(mockWSHttp.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, responseJson = Some(lookupSuccess))))
      val result = TestEtmpConnector.lookupByName(businessName)
      await(result).json shouldBe lookupSuccess
    }

  }

}
