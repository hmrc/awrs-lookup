package uk.gov.hmrc.awrslookup.models.frontend

import play.api.libs.json.{JsValue, Json}

trait AwrsEntry {
  def awrsRef: String //TODO what if it's pending? would pending companies be on the register?

  def registrationDate: String

  // in case we don't get it
  def deRegistrationDate: Option[String]

  def status: AwrsStatus
}

object AwrsEntry {
  def unapply(foo: AwrsEntry): Option[(String, JsValue)] = {
    val (prod: Product, sub) = foo match {
      case b: Business => (b, Json.toJson(b)(Business.frontEndFormatter))
      case b: Group => (b, Json.toJson(b)(Group.frontEndFormatter))
    }
    Some(prod.productPrefix -> sub)
  }

  def apply(`class`: String, data: JsValue): AwrsEntry = {
    (`class` match {
      case "Business" => Json.fromJson[Business](data)(Business.frontEndFormatter)
      case "Group" => Json.fromJson[Group](data)(Group.frontEndFormatter)
    }).get
  }

  implicit val frontEndFormatter = Json.format[AwrsEntry]
}