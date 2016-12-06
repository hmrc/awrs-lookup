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

package uk.gov.hmrc.awrslookup.connectors

import play.api.libs.json.{JsValue, Writes}
import uk.gov.hmrc.awrslookup.WSHttp
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.Authorization

import scala.concurrent.Future

trait EtmpConnector extends ServicesConfig with RawResponseReads {

  lazy val serviceURL = baseUrl("etmp-hod")
  val baseURI = "/alcohol-wholesaler-register"
  val lookupURI = "/lookup/id/"

  val urlHeaderEnvironment: String
  val urlHeaderAuthorization: String

  val http: HttpGet = WSHttp

  @inline def cGET[A](url: String)(implicit rds: HttpReads[A], hc: HeaderCarrier) =
    http.GET[A](url)(rds, hc = createHeaderCarrier(hc))

  def createHeaderCarrier(headerCarrier: HeaderCarrier): HeaderCarrier = {
    headerCarrier.withExtraHeaders("Environment" -> urlHeaderEnvironment).copy(authorization = Some(Authorization(urlHeaderAuthorization)))
  }

  def lookup(awrsRef: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    cGET( s"""$serviceURL$baseURI$lookupURI$awrsRef""")
  }

}

object EtmpConnector extends EtmpConnector {
  override val urlHeaderEnvironment: String = config("etmp-hod").getString("environment").getOrElse("")
  override val urlHeaderAuthorization: String = s"Bearer ${config("etmp-hod").getString("authorization-token").getOrElse("")}"
}
