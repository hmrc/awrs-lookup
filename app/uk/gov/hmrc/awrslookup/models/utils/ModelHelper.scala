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

package uk.gov.hmrc.awrslookup.models.utils

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import uk.gov.hmrc.awrslookup.models.etmp.formatters.EtmpDateReader
import uk.gov.hmrc.awrslookup.models.frontend.AwrsStatus

object ModelHelper {
  def getStatus(endDate: Option[String]) = {
    //    endDate match {
    //      case Some(date) => {
    //        LocalDate.parse(date, DateTimeFormat.forPattern(EtmpDateReader.frontEndDatePattern)).isAfter(LocalDate.now()) match {
    //          case true => AwrsStatus.Approved
    //          case _ => AwrsStatus.DeRegistered
    //        }
    //      }
    endDate match {
      case Some(_) => AwrsStatus.DeRegistered
      case _ => AwrsStatus.Approved
    }
  }
}
