/*
 * Copyright 2021 HM Revenue & Customs
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

import javax.inject.Inject
import uk.gov.hmrc.awrslookup.utils.LoggingUtils
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EtmpConnector @Inject()(config: ServicesConfig, val http: DefaultHttpClient, loggingUtils: LoggingUtils) extends RawResponseReads {

  lazy val serviceURL: String = config.baseUrl("etmp-hod")
  val baseURI = "/alcohol-wholesaler-register"
  val lookupByUrnURI = "/lookup/id/"

  val urlHeaderEnvironment: String = config.getConfString("etmp-hod.environment", "")
  val urlHeaderAuthorization: String = s"Bearer ${config.getConfString("etmp-hod.authorization-token", "")}"

  @inline def cGET[A](url: String)(implicit rds: HttpReads[A], hc: HeaderCarrier): Future[A] = {
    val future: Future[A] = http.GET[A](url)(rds, hc = createHeaderCarrier(hc), ec = ExecutionContext.global)
    future.foreach {
      case e: Exception => loggingUtils.err(s"get request failed: url=$url\ne=$e\n")
    }
    future
  }

  def createHeaderCarrier(headerCarrier: HeaderCarrier): HeaderCarrier = {
    headerCarrier.withExtraHeaders("Environment" -> urlHeaderEnvironment).copy(authorization = Some(Authorization(urlHeaderAuthorization)))
  }

  def lookupByUrn(awrsRef: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    cGET( s"""$serviceURL$baseURI$lookupByUrnURI$awrsRef""")
  }
}