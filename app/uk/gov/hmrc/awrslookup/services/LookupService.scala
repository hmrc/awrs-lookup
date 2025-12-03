/*
 * Copyright 2025 HM Revenue & Customs
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

import uk.gov.hmrc.awrslookup.connectors.{EtmpConnector, HipConnector}
import play.api.Logging
import play.api.http.Status
import play.api.libs.json._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.awrslookup.utils.FeatureSwitches

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class LookupService @Inject()(etmpConnector: EtmpConnector,
                              hipConnector: HipConnector)
                             (implicit ec: ExecutionContext)
  extends Logging {

  val Success = "success"

  private def lookupEtmp(awrsRefNo: String): Future[HttpResponse] =
    etmpConnector.lookupByUrn(awrsRefNo) map {
      response =>
        response.status match {
          case _ =>
            response
        }
    }

  private def lookup(awrsRefNo: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    hipConnector.lookupByUrn(awrsRefNo).map {
      response =>
        response.status match {

          case Status.OK =>
            convertHipJson(response.body).map{
              convertedJson =>
                HttpResponse(
                  Status.OK,
                  Json.stringify(convertedJson),
                  response.headers
                )
            }.getOrElse {
              HttpResponse(
                Status.INTERNAL_SERVER_ERROR,
                s"Error parsing response Json. ${response.body}",
                response.headers
              )
            }

          case _ => response
        }
    }
  }

  def convertHipJson(jsonString: String): Option[JsObject] = {

    for {
      body <- Try(Json.parse(jsonString)).toOption
      successNode <- (body \ Success).toOption
    } yield {
      successNode.asInstanceOf[JsObject]
    }
  }

  def lookupByUrn(awrsRefNo: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    if (FeatureSwitches.hipEnabled()) {
      lookup(awrsRefNo)
    } else {
      lookupEtmp(awrsRefNo)
    }
  }
}
