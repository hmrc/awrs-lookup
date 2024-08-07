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

package uk.gov.hmrc.awrslookup.connectors


import uk.gov.hmrc.http.client.HttpClientV2
import play.api.http.Status.OK
import uk.gov.hmrc.awrslookup.utils.LoggingUtils
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionId, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import uk.gov.hmrc.http.HttpReads.Implicits._

class EtmpConnector @Inject()(config: ServicesConfig, val http: HttpClientV2, loggingUtils: LoggingUtils)(implicit ec: ExecutionContext)
{

  private lazy val serviceURL: String = config.baseUrl("etmp-hod")
  private val baseURI = "/alcohol-wholesaler-register"
  private val lookupByUrnURI = "/lookup/id/"

  private val urlHeaderEnvironment: String = config.getConfString("etmp-hod.environment", "")
  private val urlHeaderAuthorization: String = s"Bearer ${config.getConfString("etmp-hod.authorization-token", "")}"
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))
  def lookupByUrn(awrsRef: String): Future[HttpResponse] = {
    val lookUpByUrnUrl = s"""$serviceURL$baseURI$lookupByUrnURI$awrsRef"""

    http.get(url"$lookUpByUrnUrl")(hc)
      .setHeader("Environment" -> urlHeaderEnvironment)
      .setHeader("Authorization" -> urlHeaderAuthorization)
      .execute[HttpResponse]
      .map {
        response =>
          response.status match {
            case OK => response
            case _ => loggingUtils.err(s"get request failed: url=$lookUpByUrnUrl\ne=${response.body}\n")
              response
          }
      }
  }
}