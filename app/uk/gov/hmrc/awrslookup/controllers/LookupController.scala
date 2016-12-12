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

import javax.inject.Inject

import play.api.Environment
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.awrslookup.models.frontend._
import uk.gov.hmrc.awrslookup.services.EtmpLookupService
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global

class LookupController @Inject()(val environment: Environment) extends BaseController {

  val referenceNotFoundString = "AWRS reference not found"

  val lookupService: EtmpLookupService = EtmpLookupService

  def lookupByUrn(awrsRef: String): Action[AnyContent] = Action.async {
    implicit request =>
      lookupService.lookupByUrn(awrsRef).map {
        lookupResponse =>
          lookupResponse.status match {
            case OK => val convertedJson = lookupResponse.json.as[SearchResult](SearchResult.etmpByUrnReader(environment = environment))
              Ok(Json.toJson(convertedJson))
            case NOT_FOUND => NotFound(referenceNotFoundString)
            case BAD_REQUEST => BadRequest(lookupResponse.body)
            case INTERNAL_SERVER_ERROR => InternalServerError(lookupResponse.body)
            case SERVICE_UNAVAILABLE => ServiceUnavailable(lookupResponse.body)
            case _ => InternalServerError(lookupResponse.body)
          }
      }
  }

  def lookupByName(queryString: String): Action[AnyContent] = Action.async {
    implicit request =>
      lookupService.lookupByName(queryString).map {
        lookupResponse =>
          lookupResponse.status match {
            case OK => val convertedJson = lookupResponse.json.as[SearchResult](SearchResult.etmpByNameReader(environment = environment))
              Ok(Json.toJson(convertedJson))
            case NOT_FOUND => NotFound(referenceNotFoundString)
            case BAD_REQUEST => BadRequest(lookupResponse.body)
            case INTERNAL_SERVER_ERROR => InternalServerError(lookupResponse.body)
            case SERVICE_UNAVAILABLE => ServiceUnavailable(lookupResponse.body)
            case _ => InternalServerError(lookupResponse.body)
          }
      }
  }

}
