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

package controllers

import metrics.AwrsLookupMetrics
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.awrslookup.controllers.LookupController
import uk.gov.hmrc.awrslookup.services.LookupService
import uk.gov.hmrc.awrslookup.utils.LoggingUtils
import uk.gov.hmrc.http.HttpResponse
import utils.AwrsTestConstants._
import utils.AwrsTestJson._
import utils.AwrsUnitTestTraits

import scala.concurrent.{ExecutionContext, Future}

class LookupControllerTest extends PlaySpec with AwrsUnitTestTraits {
  val mockLookupService: LookupService = mock[LookupService]
  val lookupFailure: JsValue = Json.parse( """{"reason": "Generic test reason"}""")
  val controllerComponents: ControllerComponents = app.injector.instanceOf[ControllerComponents]
  val awrsMetrics: AwrsLookupMetrics = app.injector.instanceOf[AwrsLookupMetrics]
  val loggingUtils: LoggingUtils = app.injector.instanceOf[LoggingUtils]
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  object TestLookupController extends LookupController(environment, controllerComponents, awrsMetrics, mockLookupService, loggingUtils)

  "Lookup Controller " must {

    "lookup awrs entry from HODS when passed a valid awrs reference" in {
      when(mockLookupService.lookupByUrn(any())(any())).thenReturn(Future.successful(HttpResponse(OK, businessJson.toString)))
      val result = TestLookupController.lookupByUrn(testRefNo).apply(FakeRequest())
      status(result) shouldBe OK
    }

    "return NOT FOUND error from HODS when awrs entry is not found" in {
      when(mockLookupService.lookupByUrn(any())(any())).thenReturn(Future.successful(HttpResponse(NOT_FOUND, lookupFailure.toString)))
      val result = TestLookupController.lookupByUrn(invalidRef).apply(FakeRequest())
      status(result) shouldBe NOT_FOUND
    }

    "return BAD REQUEST error from HODS when the request have not passed validation" in {
      when(mockLookupService.lookupByUrn(any())(any())).thenReturn(Future.successful(HttpResponse(BAD_REQUEST, lookupFailure.toString)))
      val result = TestLookupController.lookupByUrn(invalidRef).apply(FakeRequest())
      status(result) shouldBe BAD_REQUEST
    }

    "return INTERNAL SERVER ERROR error from HODS when WS02 is experiencing problems" in {
      when(mockLookupService.lookupByUrn(any())(any))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, lookupFailure.toString)))
      val result = TestLookupController.lookupByUrn(invalidRef).apply(FakeRequest())
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return SERVICE UNAVAILABLE error from HODS when the service is unavilable" in {
      when(mockLookupService.lookupByUrn(any())(any()))
        .thenReturn(Future.successful(HttpResponse(SERVICE_UNAVAILABLE, lookupFailure.toString)))
      val result = TestLookupController.lookupByUrn(invalidRef).apply(FakeRequest())
      status(result) shouldBe SERVICE_UNAVAILABLE
    }

    "return INTERNAL SERVER ERROR error from HODS when any other error is encountered" in {
      when(mockLookupService.lookupByUrn(any())(any()))
        .thenReturn(Future.successful(HttpResponse(GATEWAY_TIMEOUT, lookupFailure.toString)))
      val result = TestLookupController.lookupByUrn(invalidRef).apply(FakeRequest())
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return an entry with the correct dates when registration Date is prior to 01 April 2017" in {
      val expectedOutput = "{\"results\":[{" +
        "\"class\":\"Business\",\"data\":{" +
        "\"awrsRef\":\"2345678\"," +
        "\"registrationDate\":\"01 April 2017\"," +
        "\"status\":\"04\",\"" +
        "info\":{\"businessName\":\"companyName\",\"tradingName\":\"tradingName\",\"" +
        "address\":{" +
        "\"addressLine1\":\"addressLine1\"," +
        "\"addressLine2\":\"addressLine2\"," +
        "\"addressLine3\":\"addressLine3\"," +
        "\"addressLine4\":\"addressLine4\"," +
        "\"postcode\":\"TF3 XYZ\"}}," +
        "\"registrationEndDate\":\"01 April 2017\"}}]}"
      when(mockLookupService.lookupByUrn(any())(any()))
        .thenReturn(Future.successful(HttpResponse(OK, businessJson.toString)))
      val result = TestLookupController.lookupByUrn(testRefNo).apply(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) shouldBe expectedOutput
    }

    "return a NOT FOUND when ETMP returns a deregistered business with date prior to 01 April 2017" in {
      when(mockLookupService.lookupByUrn(any())(any()))
        .thenReturn(Future.successful(HttpResponse(OK, deRegisteredBusinessPriorToFirstApril.toString)))
      val result = TestLookupController.lookupByUrn(testRefNo).apply(FakeRequest())
      status(result) shouldBe NOT_FOUND
    }

    "return a NOT FOUND when ETMP returns a revoked business with date prior to 01 April 2017" in {
      when(mockLookupService.lookupByUrn(any())(any()))
        .thenReturn(Future.successful(HttpResponse(OK, revokedBusinessPriorToFirstApril.toString)))
      val result = TestLookupController.lookupByUrn(testRefNo).apply(FakeRequest())
      status(result) shouldBe NOT_FOUND
    }

    "return a NOT FOUND when ETMP returns a revoked business with date of 01 April 2017" in {
      when(mockLookupService.lookupByUrn(any())(any()))
        .thenReturn(Future.successful(HttpResponse(OK, revokedBusinessFirstApril.toString)))
      val result = TestLookupController.lookupByUrn(testRefNo).apply(FakeRequest())
      status(result) shouldBe NOT_FOUND
    }
  }

  "return a NOT FOUND when HIP returns a 422 with error code 006 (nor records found)" in {

    val error = """{
                  |  "errors": {
                  |    "processingDate": "2025-12-02T13:14:41Z",
                  |    "code": "006",
                  |    "text": "No records found"
                  |  }
                  |}
                  |  """.stripMargin

    when(mockLookupService.lookupByUrn(any())(any()))
      .thenReturn(Future.successful(HttpResponse(UNPROCESSABLE_ENTITY, error)))

    val result = TestLookupController.lookupByUrn(testRefNo).apply(FakeRequest())
    status(result) shouldBe NOT_FOUND
  }

  "return a INTERNAL SERVER ERROR  when HIP returns a 422 with a error code different from 006" in {

    val error = """{
                  |  "errors": {
                  |    "processingDate": "2025-12-02T13:14:41Z",
                  |    "code": "002",
                  |    "text": "ID not found"
                  |  }
                  |}
                  |  """.stripMargin

    when(mockLookupService.lookupByUrn(any())(any()))
      .thenReturn(Future.successful(HttpResponse(UNPROCESSABLE_ENTITY, error)))

    val result = TestLookupController.lookupByUrn(testRefNo).apply(FakeRequest())
    status(result) shouldBe INTERNAL_SERVER_ERROR
  }

  "return a INTERNAL SERVER ERROR  when HIP returns a 422 with a unexpected error payload" in {

    val error = """{
                  |}
                  |  """.stripMargin

    when(mockLookupService.lookupByUrn(any())(any()))
      .thenReturn(Future.successful(HttpResponse(UNPROCESSABLE_ENTITY, error)))

    val result = TestLookupController.lookupByUrn(testRefNo).apply(FakeRequest())
    status(result) shouldBe INTERNAL_SERVER_ERROR
  }

}
