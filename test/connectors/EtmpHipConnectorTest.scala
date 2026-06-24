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
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status.*
import play.api.libs.json.JsValue
import uk.gov.hmrc.awrslookup.connectors.EtmpHipConnector
import uk.gov.hmrc.awrslookup.utils.LoggingUtils
import uk.gov.hmrc.http.*
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.AwrsTestConstants.*
import utils.AwrsTestJson

import scala.concurrent.{ExecutionContext, Future}

class EtmpHipConnectorTest extends PlaySpec with GuiceOneServerPerSuite  with ConnectorTest {

  val servicesConfig: ServicesConfig = mock[ServicesConfig]
  val loggingUtils: LoggingUtils = mock[LoggingUtils]

  given ExecutionContext = scala.concurrent.ExecutionContext.global
  given HttpReads[HttpResponse] = (_: String, _: String, response: HttpResponse) => response

  object TestEtmpHipConnector$ extends EtmpHipConnector(mockHttpClient, loggingUtils, servicesConfig)

  "EtmpHipConnector" must {

    "lookup an application with a valid reference number " in {
      val lookupSuccess: JsValue = AwrsTestJson.businessJson
      val awrsRefNo: String = testRefNo

      when(servicesConfig.baseUrl(any())).thenReturn("http://hip/")

      when(mockHttpClient.get(any())(any)).thenReturn(requestBuilder)
      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(HttpResponse(OK, lookupSuccess.toString)))

      val result: Future[HttpResponse] = TestEtmpHipConnector$.lookupByUrn(awrsRefNo)
      await(result).json must  be(lookupSuccess)
    }
  }
}
