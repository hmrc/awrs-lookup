package uk.gov.hmrc.awrslookup.models.frontend

import play.api.libs.json.Json

case class Group(awrsRef: String,
                 registrationDate: String,
                 status: AwrsStatus,
                 members: List[Info],
                 deRegistrationDate: Option[String] = None
                ) extends AwrsEntry

object Group {
  implicit val frontEndFormatter = Json.format[Group]
}