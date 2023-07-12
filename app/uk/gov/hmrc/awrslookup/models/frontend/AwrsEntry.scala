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

package uk.gov.hmrc.awrslookup.models.frontend

import play.api.Environment
import play.api.libs.json._
import uk.gov.hmrc.awrslookup.models.etmp.formatters.{EtmpDateReader, EtmpDateReaderTemp}

trait AwrsEntry {
  def awrsRef: String

  def registrationDate: Option[String]

  def registrationEndDate: Option[String]

  def status: AwrsStatus

  def info: Info
}

object AwrsEntry {

  def unapply(foo: AwrsEntry): Option[(String, JsValue)] = {
    foo match {
      case b: Business => Some(b.productPrefix -> Json.toJson(b)(Business.frontEndFormatter))
      case b: Group => Some(b.productPrefix -> Json.toJson(b)(Group.frontEndFormatter))
      case _ => None
    }
  }

  def apply(`class`: String, data: JsValue): AwrsEntry = {
    (`class` match {
      case "Business" => Json.fromJson[Business](data)(Business.frontEndFormatter)
      case "Group" => Json.fromJson[Group](data)(Group.frontEndFormatter)
    }).get
  }

  implicit val frontEndFormatter: OFormat[AwrsEntry] = Json.format[AwrsEntry]

  def etmpReader(implicit environment: Environment): Reads[AwrsEntry] = (js: JsValue) => {
    for {
      // TODO remove endDatePreApril line after 1st of April and pass endDate to awrsStatus reader
      endDatePreApril <- (js \ "endDate").validateOpt[String](EtmpDateReaderTemp)
      awrsRegistrationNumber <- (js \ "awrsRegistrationNumber").validate[String]
      startDate <- (js \ "startDate").validateOpt[String](EtmpDateReader)
      endDate <- (js \ "endDate").validateOpt[String](EtmpDateReader)
      wholesaler <- (js \ "wholesaler").validate[Info](Info.etmpReader)
      awrsStatus <- (js \ "awrsStatus").validate[AwrsStatus](AwrsStatus.etmpReader(endDatePreApril))
      groupMembers <- (js \ "groupMembers").validateOpt[List[Info]](Reads.list(Info.etmpReader))
    } yield {
      groupMembers match {
        case Some(grpMembers) => Group(awrsRef = awrsRegistrationNumber,
          registrationDate = startDate,
          status = awrsStatus,
          info = wholesaler,
          members = grpMembers,
          registrationEndDate = endDate)
        case _ => Business(awrsRef = awrsRegistrationNumber,
          registrationDate = startDate,
          status = awrsStatus,
          info = wholesaler,
          registrationEndDate = endDate)
      }
    }
  }
}
