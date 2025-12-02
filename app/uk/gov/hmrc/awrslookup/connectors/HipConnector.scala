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

package uk.gov.hmrc.awrslookup.connectors

import play.api.http.Status.OK
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.awrslookup.utils.LoggingUtils

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import java.util.{Base64, UUID}
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

import uk.gov.hmrc.http.HttpReads.Implicits._

class HipConnector @Inject() (http: HttpClientV2,
                              loggingUtils: LoggingUtils,
                              config: ServicesConfig,
                              @Named("appName") val appName: String)
                             (implicit ec: ExecutionContext) {

  lazy val serviceURL: String = config.baseUrl("hip")
  val baseURI: String = "/etmp/RESTAdapter/awrs"
  val lookupURI: String = "/alcohol-wholesalers/lookup/id/"
  private val transmittingSystem = "HIP"

  private val clientId: String = config.getConfString("hip.clientId", "")
  private val clientSecret: String = config.getConfString("hip.clientSecret", "")
  private val authorizationToken: String = Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes("UTF-8"))

  // remove the "-lookup" part, as HIP assumes the app names to be "awrs" (but auditing still expects "awrs-lookup")
  private val appNameForHIP: String = appName.replace("-lookup", "")

  val headers: Seq[(String, String)] = Seq(
    "correlationid" -> UUID.randomUUID().toString,
    "X-Originating-System" -> appNameForHIP,
    "X-Receipt-Date" -> retrieveCurrentUkTimestamp,
    "X-Transmitting-System" -> transmittingSystem,
    "Authorization" -> s"Basic $authorizationToken"
  )

  private def retrieveCurrentUkTimestamp: String = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    formatter.format(ZonedDateTime.now(ZoneId.of("Europe/London")))
  }

  def lookupByUrn(awrsRefNo: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val lookupByUrnUrl = s"""$serviceURL$baseURI$lookupURI$awrsRefNo"""

    http.get(url"$lookupByUrnUrl")
      .setHeader(headers: _*)
      .execute[HttpResponse]
      .map {
        response =>
          response.status match {
            case OK => response
            case _ => loggingUtils.err(s"get request failed: url=$serviceURL$baseURI$lookupURI$awrsRefNo\ne=${response.body}\n")
              response
          }
      }
  }
}
