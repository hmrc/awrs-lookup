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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import play.api.libs.json._
import uk.gov.hmrc.awrslookup.models.etmp.formatters.EtmpDateReader

sealed trait AwrsStatus {

  def code: String

  def name: String

  override def toString: String = f"$name($code)"

}

object AwrsStatus {

  val allStatus: Set[AwrsStatus] =
    Set(Approved, Revoked, DeRegistered)

  implicit val reader: Reads[AwrsStatus] = (json: JsValue) =>
    JsSuccess(json match {
      case JsString(code) => apply(code)
      case _              => apply("-01")
    })

  implicit val writer: Writes[AwrsStatus] = (v: AwrsStatus) => JsString(v.code)

  def apply(code: String): AwrsStatus = code match {
    case Approved.code     => Approved
    case Revoked.code      => Revoked
    case DeRegistered.code => DeRegistered
    case _                 => NotFound(code)
  }

  case object Approved extends AwrsStatus {

    val code = "04"
    val name = "Approved"

  }

  case object Revoked extends AwrsStatus {

    val code = "08"
    val name = "Revoked"

  }

  case object DeRegistered extends AwrsStatus {

    val code = "10"
    val name = "De-Registered"

  }

  case class NotFound(code: String) extends AwrsStatus {

    val name = "Not Found"

  }

  def etmpReader(endDate: Option[String]): Reads[AwrsStatus] = new Reads[AwrsStatus] {

    def isDeregDateInTheFuture(endDate: Option[String]): Boolean =
      endDate match {
        case Some(date) => LocalDate.parse(date, DateTimeFormatter.ofPattern(EtmpDateReader.frontEndDatePattern)).isAfter(LocalDate.now())
        case _          => false // TODO should never happen but should we throw an exception instead?
      }

    def reads(js: JsValue): JsResult[AwrsStatus] =
      for {
        awrsStatus <- js.validate[String]
      } yield
      // remove case sensitivity, spaces and dashes as the schema has changed a few times and caused issues
      awrsStatus.toLowerCase.replaceAll(" ", "").replaceAll("-", "") match {
        case "approved" | "approvedwithconditions"   => Approved
        case "deregistered"                          =>
          if (isDeregDateInTheFuture(endDate))
            Approved
          else
            DeRegistered
        case "revoked" | "revokedunderreview/appeal" =>
          if (isDeregDateInTheFuture(endDate))
            Approved
          else
            Revoked
        case _                                       => NotFound(awrsStatus)
      }

  }

}
