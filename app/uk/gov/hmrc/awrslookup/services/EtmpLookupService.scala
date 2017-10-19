/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.awrslookup.services

import uk.gov.hmrc.awrslookup.connectors.EtmpConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse }

object EtmpLookupService extends EtmpLookupService {
  val etmpConnector: EtmpConnector = EtmpConnector
}

trait EtmpLookupService {
  val etmpConnector: EtmpConnector

  def lookupByUrn(awrsRefNo: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] =
    etmpConnector.lookupByUrn(awrsRefNo) map {
      response =>
        response.status match {
          case _ => response
        }
    }

  def lookupByName(queryString: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] =
    etmpConnector.lookupByName(queryString) map {
      response =>
        response.status match {
          case _ => response
        }
    }

}
