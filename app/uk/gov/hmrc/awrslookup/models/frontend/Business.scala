package uk.gov.hmrc.awrslookup.models.frontend

import play.api.libs.json.Json

case class Business(awrsRef: String,
                    registrationDate: String,
                    status: AwrsStatus,
                    info: Info,
                    deRegistrationDate: Option[String] = None
                   ) extends AwrsEntry

object Business {
  implicit val frontEndFormatter = Json.format[Business]
}