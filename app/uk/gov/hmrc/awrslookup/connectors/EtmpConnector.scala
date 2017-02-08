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

package uk.gov.hmrc.awrslookup.connectors

import java.net.{URLEncoder, URLDecoder}

import play.api.Logger
import uk.gov.hmrc.awrslookup.WSHttp
import uk.gov.hmrc.awrslookup.audit.Auditable
import uk.gov.hmrc.awrslookup.utils.LoggingUtils
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.Authorization

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait EtmpConnector extends ServicesConfig with RawResponseReads with LoggingUtils {

  lazy val serviceURL = baseUrl("etmp-hod")
  val baseURI = "/alcohol-wholesaler-register"
  val lookupByUrnURI = "/lookup/id/"
  val lookupByNameURI = "/lookup/name/"

  val urlHeaderEnvironment: String
  val urlHeaderAuthorization: String

  val http: HttpGet = WSHttp

  private def encode(url: String) = {
    URLEncoder.encode(url, "UTF-8").replaceAll("\\+", "%20")
  }

  def cGET[A](url: String)(implicit rds: HttpReads[A], hc: HeaderCarrier): Future[A] = {
    val future = http.GET[A](url)(rds, hc = createHeaderCarrier(hc))
    future.onFailure {
      case e: Exception => Logger.debug("get request failed: url=$url\ne=$e\n")
    }
    future
  }

  def createHeaderCarrier(headerCarrier: HeaderCarrier): HeaderCarrier = {
    headerCarrier.withExtraHeaders("Environment" -> urlHeaderEnvironment).copy(authorization = Some(Authorization(urlHeaderAuthorization)))
  }

  def lookupByUrn(awrsRef: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    cGET( s"""$serviceURL$baseURI$lookupByUrnURI$awrsRef""")
  }

  def lookupByName(queryString: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    cGET( s"""$serviceURL$baseURI$lookupByNameURI${encode(queryString)}""")
  }

}

object EtmpConnector extends EtmpConnector {
  override val urlHeaderEnvironment: String = config("etmp-hod").getString("environment").getOrElse("")
  override val urlHeaderAuthorization: String = s"Bearer ${config("etmp-hod").getString("authorization-token").getOrElse("")}"
}
