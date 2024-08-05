/*
 * Copyright 2023 HM Revenue & Customs
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

import org.mockito.Mockito._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec
import play.api.http.Status._
import uk.gov.hmrc.awrslookup.connectors.EtmpConnector
import uk.gov.hmrc.awrslookup.utils.LoggingUtils
import uk.gov.hmrc.http.{SessionId, _}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.AwrsTestConstants._
import utils.{AwrsTestJson, AwrsUnitTestTraits}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class EtmpConnectorTest extends PlaySpec with AwrsUnitTestTraits with ConnectorTest{

  val mockWSHttp: HttpClientV2 = mock[HttpClientV2]
  val servicesConfig: ServicesConfig = app.injector.instanceOf[ServicesConfig]
  val loggingUtils: LoggingUtils = app.injector.instanceOf[LoggingUtils]
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  class Setup extends ConnectorTest{
    val TestEtmpConnector: EtmpConnector = new EtmpConnector(servicesConfig, mockHttpClient, loggingUtils)
  }

  before {
    reset(mockWSHttp)
  }

  "EtmpConnector" must {

    "lookup an application with a valid reference number " in new Setup {
      val lookupSuccess = AwrsTestJson.businessJson
      val awrsRefNo = testRefNo
      implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(HttpResponse(OK, lookupSuccess.toString)))
      val result = TestEtmpConnector.lookupByUrn(awrsRefNo)
      await(result).json shouldBe lookupSuccess
    }

  }

}
