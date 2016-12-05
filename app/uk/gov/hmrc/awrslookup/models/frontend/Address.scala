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

case class Address(
                    addressLine1: String,
                    addressLine2: Option[String] = None,
                    addressLine3: Option[String] = None,
                    addressLine4: Option[String] = None,
                    postcode: Option[String] = None,
                    addressCountry: Option[String] = None
                  ) {

  override def toString = {
    val line3display = addressLine3.map(line3 => s"$line3, ").fold("")(x => x)
    val line4display = addressLine4.map(line4 => s"$line4, ").fold("")(x => x)
    val postcodeDisplay = postcode.map(postcode1 => s"$postcode1, ").fold("")(x => x)
    val countryDisplay = addressCountry.map(country => s"$country, ").fold("")(x => x)
    s"$addressLine1, $addressLine2, $line3display, $line4display, $postcodeDisplay, $countryDisplay"
  }

  override def equals(obj: Any): Boolean = obj match {
    case that: Address =>
      that.addressLine1.equals(addressLine1) &&
        that.addressLine2.equals(addressLine2) &&
        that.addressLine3.equals(addressLine3) &&
        that.addressLine4.equals(addressLine4) &&
        that.postcode.equals(postcode) &&
        that.addressCountry.equals(addressCountry)
    case _ => false
  }

  override def hashCode(): Int =
    (addressLine1, addressLine2, addressLine3, addressLine4, postcode, addressCountry).hashCode()
}

object Address {

  val etmpReader = new Reads[Address] {
    def reads(js: JsValue): JsResult[Address] =
      for {
        addressLine1 <- (js \ "addressLine1").validate[String]
        addressLine2 <- (js \ "addressLine2").validateOpt[String]
        addressLine3 <- (js \ "addressLine3").validateOpt[String]
        addressLine4 <- (js \ "addressLine4").validateOpt[String]
        postcode <- (js \ "postcode").validateOpt[String]
        countryCode <- (js \ "country").validateOpt[String]
      } yield {
        Address(postcode = postcode, addressLine1 = addressLine1, addressLine2 = addressLine2, addressLine3 = addressLine3,
          addressLine4 = addressLine4, addressCountry = countryCode)
      }
  }

  implicit val frontEndFormatter = Json.format[Address]
}
