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

package uk.gov.hmrc.awrslookup.models.utils

import java.io.InputStream
import play.api.Environment
import play.api.libs.json.{JsValue, Json, OFormat}

import scala.io.Source

object CountryCodes {

  case class Country(country: String, countryCode: String)

  object Country {
    given OFormat[Country] = Json.format[Country]
  }

  def jsonInputStream(using environment: Environment): Option[InputStream] = environment.resourceAsStream("country-code.json")

  private def json(using environment: Environment): JsValue = {
    jsonInputStream match {
      case Some(inputStream) => Json.parse(Source.fromInputStream(inputStream, "UTF-8").mkString)
      case _ => throw new Exception("Country codes file not found")
    }
  }

  private def countryCodesMap(using environment: Environment): Map[String, String] = {
    val countryCodeList = json.validate[List[Country]].get
    countryCodeList.map(country => (country.countryCode, country.country)).toMap
  }

  def getCountry(countryCode: String)(using environment: Environment): Option[String] = {
    countryCodesMap.get(countryCode)
  }

}
