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

import play.api.libs.json._

case class Info(businessName: Option[String] = None,
                tradingName: Option[String] = None,
                fullName: Option[String] = None,
                address: Option[Address] = None
               )

object Info {

  def etmpReader(implicit environment: play.api.Environment): Reads[Info] = new Reads[Info] {
    def reads(js: JsValue): JsResult[Info] =
      for {
        companyName <- (js \ "companyName").validateOpt[String]
        tradingName <- (js \ "tradingName").validateOpt[String]
        businessAddress <- (js \ "businessAddress").validate[Address](Address.etmpReader)
      } yield {
        Info(businessName = companyName,
          tradingName = tradingName,
          fullName = None, // TODO how to handle SoleTrader
          address = Some(businessAddress))
      }
  }

  implicit val frontEndFormatter = Json.format[Info]
}
