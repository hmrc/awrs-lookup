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

package uk.gov.hmrc.awrslookup.models.etmp.formatters

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import play.api.libs.json.{ JsResult, JsSuccess, JsValue, Reads }
import scala.util.Try

// TODO remove this object/trait after 1st April

object EtmpDateReaderTemp extends EtmpDateReaderTemp

trait EtmpDateReaderTemp extends Reads[String] {

  val earliestDateString = "2017-04-01"

  val etmpDatePattern = "yyyy-M-d"

  val frontEndDatePattern = "dd MMMM yyyy"

  val parseDate: JsResult[String] => LocalDate = (str: JsResult[String]) =>
    Try(LocalDate.parse(str.get, DateTimeFormatter.ofPattern(etmpDatePattern))).fold(
      _         => LocalDate.parse(str.get, DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      localDate => localDate
    )

  val earliestPossibleDate: LocalDate = LocalDate.parse(earliestDateString, DateTimeFormatter.ofPattern(etmpDatePattern))

  override def reads(json: JsValue): JsResult[String] = {
    val str      = json.validate[String]
    val dateTime = parseDate(str)
    JsSuccess(dateTime.format(DateTimeFormatter.ofPattern(frontEndDatePattern)))
  }

}
