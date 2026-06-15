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

package services

import connectors.ConnectorTest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.OK
import play.api.libs.json.*
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.awrslookup.connectors.EtmpHipConnector
import uk.gov.hmrc.awrslookup.services.LookupService
import uk.gov.hmrc.http.HttpResponse
import utils.AwrsTestConstants.*
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class LookupServiceTest extends PlaySpec with GuiceOneAppPerSuite with ConnectorTest with BeforeAndAfterEach with BeforeAndAfterAll {

  val mockEtmpHipConnector: EtmpHipConnector = mock[EtmpHipConnector]

  object LookupService extends LookupService(mockEtmpHipConnector)

  "LookupService" must {

    "return internal server error if Json body cannot be parsed and converted" in {

      val awrsRefNo = testRefNo

      when(mockEtmpHipConnector.lookupByUrn(any())(using any())).thenReturn(Future.successful(HttpResponse(OK, "")))
      val result = LookupService.lookupByUrn(awrsRefNo)
      await(result).status shouldBe INTERNAL_SERVER_ERROR
    }

    "successfully return an entry when passed a valid reference number" in {

      val awrsRefNo = testRefNo
      val hipJson = """{
              "success" : {
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
              }
        }"""
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

      when(mockEtmpHipConnector.lookupByUrn(any())(using any())).thenReturn(Future.successful(HttpResponse(OK, hipJson)))
      val result = LookupService.lookupByUrn(awrsRefNo)
      Json.parse(await(result).body) shouldBe Json.parse(expectedOutput)
    }

    "return Not Found when passed an reference number that does not exist" in {

      val invalidAwrsRefNo = invalidRef

      when(mockEtmpHipConnector.lookupByUrn(any())(using any())).thenReturn(Future.successful(HttpResponse(NOT_FOUND, "")))

      val result = LookupService.lookupByUrn(invalidAwrsRefNo)
      await(result).status shouldBe NOT_FOUND
    }

    "convert Hip Json to correct format" in {

      val hipJson = """{
              "success" : {
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
                },
                "groupMembers": [
                  {
                    "companyName": "Company Name 3",
                    "tradingName": "trading Name 3",
                    "businessAddress": {
                      "addressLine1": "addressLine1",
                      "addressLine2": "addressLine2",
                      "addressLine3": "addressLine3",
                      "addressLine4": "addressLine4",
                      "country": "GB",
                      "postcode": "TF3 XYZ"
                    }
                  }
                ]
              }
        }"""
      val expectedJson = """{
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
              },
              "groupMembers": [
                {
                  "companyName": "Company Name 3",
                  "tradingName": "trading Name 3",
                  "businessAddress": {
                    "addressLine1": "addressLine1",
                    "addressLine2": "addressLine2",
                    "addressLine3": "addressLine3",
                    "addressLine4": "addressLine4",
                    "country": "GB",
                    "postcode": "TF3 XYZ"
                  }
                }
              ]
        }"""

      val convertedJsonOpt = LookupService.convertHipJson(hipJson)

      convertedJsonOpt.isDefined and (convertedJsonOpt.get shouldBe  Json.parse(expectedJson).as[JsObject])
    }
  }
}