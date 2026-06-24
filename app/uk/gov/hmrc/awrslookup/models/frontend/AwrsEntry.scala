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
import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import uk.gov.hmrc.awrslookup.models.etmp.formatters.{EtmpDateReader, EtmpDateReaderTemp}
import uk.gov.hmrc.awrslookup.models.frontend.*
import uk.gov.hmrc.http.InternalServerException

sealed trait AwrsEntry {
  def awrsRef: String

  def registrationDate: Option[String]

  def registrationEndDate: Option[String]

  def status: AwrsStatus

  def info: Info
}

case class Business(awrsRef: String,
                    registrationDate: Option[String] = None,
                    status: AwrsStatus,
                    info: Info,
                    registrationEndDate: Option[String] = None
                   ) extends AwrsEntry

object Business {

  given frontEndFormatter: OFormat[Business] = Json.format[Business]
}

case class Group(awrsRef: String,
                 registrationDate: Option[String] = None,
                 status: AwrsStatus,
                 info: Info,
                 members: List[Info],
                 registrationEndDate: Option[String] = None
                ) extends AwrsEntry

object Group {

  given frontEndFormatter: OFormat[Group] = Json.format[Group]
}

object AwrsEntry {

  def unapply(awrsEntry: AwrsEntry): (String, JsValue) = {
    val json = awrsEntry match {
      case business: Business => Json.toJson(business)(Business.frontEndFormatter)
      case group: Group => Json.toJson(group)(Group.frontEndFormatter)
    }
    awrsEntry.getClass.getSimpleName -> json
  }

  def apply(`class`: String, data: JsValue): AwrsEntry = {
    (`class` match {
      case "Business" => Json.fromJson[Business](data)(Business.frontEndFormatter)
      case "Group" => Json.fromJson[Group](data)(Group.frontEndFormatter)
    }).getOrElse(throw new InternalServerException("Error deserializing AwrsEntry"))
  }

  implicit val reads: Reads[AwrsEntry] = (
    (JsPath \ "class").read[String] and (JsPath \ "data").read[JsValue]
    )(AwrsEntry.apply _)

  implicit val writes: OWrites[AwrsEntry] = (
    (JsPath \ "class").write[String] and (JsPath \ "data").write[JsValue]
    )(AwrsEntry.unapply)

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
