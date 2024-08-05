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
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.awrslookup.connectors.EtmpConnector
import uk.gov.hmrc.awrslookup.utils.LoggingUtils
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.AwrsTestConstants._
import utils.AwrsTestJson

import scala.concurrent.{ExecutionContext, Future}

class EtmpConnectorTest extends PlaySpec with GuiceOneServerPerSuite  with ConnectorTest with BeforeAndAfterEach {

  val servicesConfig: ServicesConfig = mock[ServicesConfig]
  val loggingUtils: LoggingUtils = mock[LoggingUtils]

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  implicit val httpReads: HttpReads[HttpResponse] = (_: String, _: String, response: HttpResponse) => response

  class Setup {
    val connector: EtmpConnector = new EtmpConnector(servicesConfig, mockHttpClient, loggingUtils)
  }

  override def beforeEach(): Unit = {
    reset(mockHttpClient)
    reset(servicesConfig)
    reset(loggingUtils)
  }

  "EtmpConnector" must {

    "lookup an application with a valid reference number " in new Setup {
      val lookupSuccess: JsValue = AwrsTestJson.businessJson
      val awrsRefNo: String = testRefNo

      when(servicesConfig.baseUrl(any())).thenReturn("http://etmp-hod/")

      when(mockHttpClient.get(any())(any)).thenReturn(requestBuilder)
      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(HttpResponse(OK, lookupSuccess.toString)))

      val result: Future[HttpResponse] = connector.lookupByUrn(awrsRefNo)
      await(result).json must  be(lookupSuccess)
    }

  }

}