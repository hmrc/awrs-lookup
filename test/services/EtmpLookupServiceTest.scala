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

package services

import java.util.UUID

import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import play.api.test.Helpers._
import uk.gov.hmrc.awrslookup.connectors.EtmpConnector
import uk.gov.hmrc.awrslookup.services.EtmpLookupService
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import utils.AwrsTestConstants._

import scala.concurrent.Future

class EtmpLookupServiceTest extends UnitSpec with OneServerPerSuite with MockitoSugar {

  implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

  val mockEtmpConnector = mock[EtmpConnector]

  object TestEtmpLookupService extends EtmpLookupService {
    override val etmpConnector = mockEtmpConnector
  }

  "EtmpLookupService " should {
    "use the correct connector" in {
      EtmpLookupService.etmpConnector shouldBe EtmpConnector
    }

    "successfully lookup an entry when passed a valid reference number" in {
      val awrsRefNo = testRefNo
      when(mockEtmpConnector.lookup(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, None)))
      val result = TestEtmpLookupService.lookup(awrsRefNo)
      await(result).status shouldBe OK
    }

    "return Not Found when passed an reference number that does not exist" in {
      val invalidAwrsRefNo = "invalid ref"
      when(mockEtmpConnector.lookup(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(NOT_FOUND, None)))
      val result = TestEtmpLookupService.lookup(invalidAwrsRefNo)
      await(result).status shouldBe NOT_FOUND
    }
  }
}
