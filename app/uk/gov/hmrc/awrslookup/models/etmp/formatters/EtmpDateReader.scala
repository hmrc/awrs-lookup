/*
 * Copyright 2021 HM Revenue & Customs
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

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{JsResult, JsSuccess, JsValue, Reads}

object EtmpDateReader extends EtmpDateReader

trait EtmpDateReader extends Reads[String] {

  val earliestDateString = "2017-04-01"

  val etmpDatePattern = "yyyy-MM-dd"

  val frontEndDatePattern = "dd MMMM yyyy"

  val parseDate = (str: JsResult[String]) => DateTime.parse(str.get, DateTimeFormat.forPattern(etmpDatePattern))

  val earliestPossibleDate = DateTime.parse(earliestDateString, DateTimeFormat.forPattern(etmpDatePattern))

  override def reads(json: JsValue): JsResult[String] = {
    val str = json.validate[String]
    val dateTime = parseDate(str)
    val transformedDate = dateTime.isBefore(earliestPossibleDate) match {
      case true => earliestPossibleDate
      case false => dateTime
    }
    JsSuccess(transformedDate.toString(frontEndDatePattern))
  }

}
