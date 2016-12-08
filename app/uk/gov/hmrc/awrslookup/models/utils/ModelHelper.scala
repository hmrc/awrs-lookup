package uk.gov.hmrc.awrslookup.models.utils

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import uk.gov.hmrc.awrslookup.models.etmp.formatters.EtmpDateReader
import uk.gov.hmrc.awrslookup.models.frontend.AwrsStatus

object ModelHelper {
  def getStatus(endDate: Option[String]) = {
    endDate match {
      case Some(date) => {
        LocalDate.parse(date, DateTimeFormat.forPattern(EtmpDateReader.frontEndDatePattern)).isAfter(LocalDate.now()) match {
          case true => AwrsStatus.Approved
          case _ => AwrsStatus.DeRegistered
        }
      }
      case _ => AwrsStatus.Approved
    }
  }
}
