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

package uk.gov.hmrc.awrslookup.models.frontend

import play.api.libs.json.{JsResult, JsValue, Json, Reads}
import uk.gov.hmrc.awrslookup.models._
import etmp.formatters._

case class Business(awrsRef: String,
                    registrationDate: Option[String] = None,
                    status: AwrsStatus,
                    info: Info,
                    registrationEndDate: Option[String] = None
                   ) extends AwrsEntry

object Business {

  def etmpReader(implicit environment: play.api.Environment): Reads[Business] = new Reads[Business] {
    def reads(js: JsValue): JsResult[Business] =
      for {
        awrsRegistrationNumber <- (js \ "awrsRegistrationNumber").validate[String]
        startDate <- (js \ "startDate").validateOpt[String](EtmpDateReader)
        endDate <- (js \ "endDate").validateOpt[String](EtmpDateReader)
        wholesaler <- (js \ "wholesaler").validate[Info](Info.etmpReader)
        awrsStatus <- (js \ "awrsStatus").validate[AwrsStatus](AwrsStatus.etmpReader)
      } yield {
        Business(awrsRef = awrsRegistrationNumber,
          registrationDate = startDate,
          status = awrsStatus,
          info = wholesaler,
          registrationEndDate = endDate)
      }
  }

  implicit val frontEndFormatter = Json.format[Business]
}
