/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.awrslookup.models.utils

import java.io.InputStream

import play.api.Environment
import play.api.libs.json.{JsValue, Json}

import scala.io.Source

object CountryCodes {

  case class Country(country: String, countryCode: String)

  object Country {
    implicit val formats = Json.format[Country]
  }

  def jsonInputStream(implicit environment: Environment): Option[InputStream] = environment.resourceAsStream("country-code.json")

  private def json(implicit environment: Environment): JsValue = {
    jsonInputStream match {
      case Some(inputStream) => Json.parse(Source.fromInputStream(inputStream, "UTF-8").mkString)
      case _ => throw new Exception("Country codes file not found")
    }
  }

  private def countryCodesMap(implicit environment: Environment): Map[String, String] = {
    val countryCodeList = json.validate[List[Country]].get
    countryCodeList.map(country => (country.countryCode, country.country)).toMap
  }

  def getCountry(countryCode: String)(implicit environment: Environment): Option[String] = {
    countryCodesMap.get(countryCode)
  }

}
