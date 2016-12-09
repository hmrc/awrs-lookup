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

package controllers

import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.awrslookup.controllers.LookupController
import uk.gov.hmrc.awrslookup.services.EtmpLookupService
import uk.gov.hmrc.play.http.HttpResponse
import utils.AwrsUnitTestTraits
import utils.AwrsTestJson._
import utils.AwrsTestConstants._

import scala.concurrent.Future

class LookupControllerTest extends AwrsUnitTestTraits {
  val mockEtmpLookupService: EtmpLookupService = mock[EtmpLookupService]
  val lookupFailure = Json.parse( """{"reason": "Generic test reason"}""")

  object TestLookupController extends LookupController(environment = environment) {
    override val lookupService: EtmpLookupService = mockEtmpLookupService
  }

  "Lookup Controller " should {

    "use the correct Lookup service" in {
      new LookupController(environment = environment).lookupService shouldBe EtmpLookupService
    }

    "lookup awrs entry from HODS when passed a valid awrs reference" in {
      when(mockEtmpLookupService.lookup(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, Some(businessJson))))
      val result = TestLookupController.lookup(testRefNo).apply(FakeRequest())
      status(result) shouldBe OK
    }

    "return NOT FOUND error from HODS when awrs entry is not found" in {
      when(mockEtmpLookupService.lookup(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(NOT_FOUND, Some(lookupFailure))))
      val result = TestLookupController.lookup(invalidRef).apply(FakeRequest())
      status(result) shouldBe NOT_FOUND
    }

    "return BAD REQUEST error from HODS when the request have not passed validation" in {
      when(mockEtmpLookupService.lookup(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(BAD_REQUEST, Some(lookupFailure))))
      val result = TestLookupController.lookup(invalidRef).apply(FakeRequest())
      status(result) shouldBe BAD_REQUEST
    }

    "return INTERNAL SERVER ERROR error from HODS when WS02 is experiencing problems" in {
      when(mockEtmpLookupService.lookup(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, Some(lookupFailure))))
      val result = TestLookupController.lookup(invalidRef).apply(FakeRequest())
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return SERVICE UNAVAILABLE error from HODS when the service is unavilable" in {
      when(mockEtmpLookupService.lookup(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(SERVICE_UNAVAILABLE, Some(lookupFailure))))
      val result = TestLookupController.lookup(invalidRef).apply(FakeRequest())
      status(result) shouldBe SERVICE_UNAVAILABLE
    }

    "return INTERNAL SERVER ERROR error from HODS when any other error is encountered" in {
      when(mockEtmpLookupService.lookup(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(GATEWAY_TIMEOUT, Some(lookupFailure))))
      val result = TestLookupController.lookup(invalidRef).apply(FakeRequest())
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

  }
}
