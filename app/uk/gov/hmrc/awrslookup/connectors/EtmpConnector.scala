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

import uk.gov.hmrc.awrslookup.utils.LoggingUtils
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EtmpConnector @Inject()(config: ServicesConfig, val http: HttpClientV2, loggingUtils: LoggingUtils)(implicit ec: ExecutionContext)
  extends RawResponseReads {

  private lazy val serviceURL: String = config.baseUrl("etmp-hod")
  private val baseURI = "/alcohol-wholesaler-register"
  private val lookupByUrnURI = "/lookup/id/"

  @inline private def cGET[A](url: String)(implicit rds: HttpReads[A], hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {

    val future: Future[A] = http.get(url"url")(hc).execute(rds, ec)
    future.foreach {
      case e: Exception => loggingUtils.err(s"get request failed: url=$url\ne=$e\n")
      case _ => None
    }
    future
  }

  def lookupByUrn(awrsRef: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    cGET( s"""$serviceURL$baseURI$lookupByUrnURI$awrsRef""")
  }
}
