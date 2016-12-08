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

import play.api.libs.json.{JsValue, Json}

trait AwrsEntry {
  def awrsRef: String

  def registrationDate: String

  def registrationEndDate: Option[String]

  def status: AwrsStatus

  def info: Info
}

object AwrsEntry {
  def unapply(foo: AwrsEntry): Option[(String, JsValue)] = {
    val (prod: Product, sub) = foo match {
      case b: Business => (b, Json.toJson(b)(Business.frontEndFormatter))
      case b: Group => (b, Json.toJson(b)(Group.frontEndFormatter))
    }
    Some(prod.productPrefix -> sub)
  }

  def apply(`class`: String, data: JsValue): AwrsEntry = {
    (`class` match {
      case "Business" => Json.fromJson[Business](data)(Business.frontEndFormatter)
      case "Group" => Json.fromJson[Group](data)(Group.frontEndFormatter)
    }).get
  }

  implicit val frontEndFormatter = Json.format[AwrsEntry]
}
