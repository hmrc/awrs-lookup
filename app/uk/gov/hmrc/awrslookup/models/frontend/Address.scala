/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.awrslookup.models.utils.CountryCodes

case class Address(addressLine1: Option[String] = None,
                   addressLine2: Option[String] = None,
                   addressLine3: Option[String] = None,
                   addressLine4: Option[String] = None,
                   postcode: Option[String] = None,
                   addressCountry: Option[String] = None)
object Address {

  def etmpReader(implicit environment: play.api.Environment): Reads[Address] = new Reads[Address] {
    def reads(js: JsValue): JsResult[Address] =
      for {
        addressLine1 <- (js \ "addressLine1").validateOpt[String]
        addressLine2 <- (js \ "addressLine2").validateOpt[String]
        addressLine3 <- (js \ "addressLine3").validateOpt[String]
        addressLine4 <- (js \ "addressLine4").validateOpt[String]
        postcode <- (js \ "postcode").validateOpt[String]
        countryCode <- (js \ "country").validateOpt[String]
      } yield {
        Address(postcode = postcode, addressLine1 = addressLine1, addressLine2 = addressLine2, addressLine3 = addressLine3,
          addressLine4 = addressLine4, addressCountry = countryCode.filterNot(_ == "GB").flatMap(CountryCodes.getCountry))
      }
  }

  implicit val frontEndFormatter = Json.format[Address]
}
