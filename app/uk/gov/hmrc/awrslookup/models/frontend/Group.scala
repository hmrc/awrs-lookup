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

case class Group(awrsRef: String,
                 registrationDate: String,
                 status: AwrsStatus,
                 info: Info,
                 members: List[Info],
                 registrationEndDate: Option[String] = None
                ) extends AwrsEntry

object Group {

  def etmpReader(implicit environment: play.api.Environment): Reads[Option[Group]] = new Reads[Option[Group]] {
    def reads(js: JsValue): JsResult[Option[Group]] =
      for {
        awrsRegistrationNumber <- (js \ "awrsRegistrationNumber").validate[String]
        startDate <- (js \ "startDate").validate[String]
        endDate <- (js \ "endDate").validateOpt[String]
        wholesaler <- (js \ "wholesaler").validate[Info](Info.etmpReader)
        groupMembers <- (js \ "groupMembers").validateOpt[List[Info]](Reads.list(Info.etmpReader))
      } yield {
        groupMembers match {
          case Some(grpMembers) => Some(Group(awrsRef = awrsRegistrationNumber,
            registrationDate = startDate,
            status = endDate match {
              case Some(_) => AwrsStatus.DeRegistered
              case _ => AwrsStatus.Approved
            },
            info = wholesaler,
            members = grpMembers,
            registrationEndDate = endDate))
          case _ => None
        }
      }
  }

  implicit val frontEndFormatter = Json.format[Group]
}
