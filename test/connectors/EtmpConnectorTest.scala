/*
 * Copyright 2021 HM Revenue & Customs
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
import org.scalatest.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec
import play.api.http.Status._
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.awrslookup.connectors.EtmpConnector
import uk.gov.hmrc.awrslookup.utils.LoggingUtils
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.SessionId
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import utils.AwrsTestConstants._
import utils.{AwrsTestJson, AwrsUnitTestTraits}

import scala.concurrent.Future

class EtmpConnectorTest extends PlaySpec with AwrsUnitTestTraits {

  val mockWSHttp: DefaultHttpClient = mock[DefaultHttpClient]
  val servicesConfig: ServicesConfig = app.injector.instanceOf[ServicesConfig]
  val loggingUtils: LoggingUtils = app.injector.instanceOf[LoggingUtils]

  object TestEtmpConnector extends EtmpConnector(servicesConfig, mockWSHttp, loggingUtils)

  before {
    reset(mockWSHttp)
  }

  "EtmpConnector" must {

    "lookup an application with a valid reference number " in {
      val lookupSuccess = AwrsTestJson.businessJson
      val awrsRefNo = testRefNo
      implicit val hc = HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      when(mockWSHttp.GET[HttpResponse](Matchers.any(),Matchers.any(),Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, lookupSuccess.toString)))
      val result = TestEtmpConnector.lookupByUrn(awrsRefNo)
      await(result).json shouldBe lookupSuccess
    }

  }

}
