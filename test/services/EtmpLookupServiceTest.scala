/*
 * Copyright 2020 HM Revenue & Customs
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

package services

import java.util.UUID

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers._
import play.libs.Json
import uk.gov.hmrc.awrslookup.connectors.EtmpConnector
import uk.gov.hmrc.awrslookup.services.EtmpLookupService
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import utils.AwrsTestConstants._
import utils.AwrsTestJson

import scala.concurrent.Future

class EtmpLookupServiceTest extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

  val mockEtmpConnector: EtmpConnector = mock[EtmpConnector]

  object TestEtmpLookupService extends EtmpLookupService(mockEtmpConnector)

  "EtmpLookupService " should {

    "successfully lookup an entry when passed a valid reference number" in {
      val awrsRefNo = testRefNo
      when(mockEtmpConnector.lookupByUrn(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, None)))
      val result = TestEtmpLookupService.lookupByUrn(awrsRefNo)
      await(result).status shouldBe OK
    }

    "succesfully return an entry when passed a valid reference number" in {
      val awrsRefNo = testRefNo
      val expectedOutput =
        """{
                                  "awrsRegistrationNumber" : "2345678",
                                 "startDate" : "2016-04-01",
                                  "endDate" : "2016-10-17",
                                  "awrsStatus" : "approved",
                                  "processingDate" : "2001-12-31T12:30:59Z",
                                  "wholesaler" : {
                                   "companyName" : "companyName",
                                  "tradingName" : "tradingName",
                                    "businessAddress" : {
                                      "addressLine1" : "addressLine1",
                                      "addressLine2" : "addressLine2",
                                      "addressLine3" : "addressLine3",
                                      "addressLine4" : "addressLine4",
                                      "country" : "GB",
                                      "postcode" : "TF3 XYZ"
                                    }
                                  }
                                }"""
      val lookupSuccess = AwrsTestJson.businessJson
      when(mockEtmpConnector.lookupByUrn(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, responseJson = Some(lookupSuccess))))
      val result = TestEtmpLookupService.lookupByUrn(awrsRefNo)
      Json.parse(await(result).body) shouldBe Json.parse(expectedOutput)
    }

    "return Not Found when passed an reference number that does not exist" in {
      val invalidAwrsRefNo = invalidRef
      when(mockEtmpConnector.lookupByUrn(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(NOT_FOUND, None)))
      val result = TestEtmpLookupService.lookupByUrn(invalidAwrsRefNo)
      await(result).status shouldBe NOT_FOUND
    }

    "successfully lookup an entry when passed a valid business name" in {
      val businessName = testBusinessName
      when(mockEtmpConnector.lookupByName(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, None)))
      val result = TestEtmpLookupService.lookupByName(businessName)
      await(result).status shouldBe OK
    }
  }
}
