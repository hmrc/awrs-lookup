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

package uk.gov.hmrc.awrslookup.controllers

import play.api.mvc._
import uk.gov.hmrc.awrslookup.models.frontend.AwrsStatus.Pending
import uk.gov.hmrc.awrslookup.models.frontend._
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future

class LookupController extends BaseController {

  implicit def conv[T](data: T): Option[T] = Some(data)

  def lookup(awrsRef: String) = Action.async { implicit request =>
    val res:Option[SearchResult] = awrsRef match {
      case "XXAW00000123457" =>
        SearchResult(
          List(
            Business("XXAW00000123456", "1 April 2017", Pending, Info("info", "info")),
            Group("XXAW00000123455", "1 April 2017", Pending, Info("info", "info"), List(Info("info", "info"), Info("info2", "info2")))
          ))
      case "XXAW00000123456" => SearchResult(List(Business("XXAW00000123456", "1 April 2017", Pending, Info("info", "info"))))
      case "XXAW00000123455" => SearchResult(List(Group("XXAW00000123455", "1 April 2017", Pending, Info("info", "info"), List(Info("info", "info"), Info("info2", "info2")))))
      case "XXAW00000123454" => SearchResult(List())
      case _ => None
    }
    res match {
      case Some(r) =>
        val t = SearchResult.frontEndFormatter.writes(r)
        Future.successful(Ok(t))
      case _ =>
        Future.successful(NotFound(""))
    }

  }

}
