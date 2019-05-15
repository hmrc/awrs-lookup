/*
 * Copyright 2019 HM Revenue & Customs
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
import metrics.AwrsLookupMetrics
import org.joda.time.DateTime
import play.api.Environment
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc._
import uk.gov.hmrc.awrslookup.models.ApiType
import uk.gov.hmrc.awrslookup.models.ApiType.ApiType
import uk.gov.hmrc.awrslookup.models.frontend._
import uk.gov.hmrc.awrslookup.services.EtmpLookupService
import uk.gov.hmrc.awrslookup.utils.LoggingUtils
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import play.api.libs.json.JodaReads._

import scala.concurrent.ExecutionContext.Implicits.global

class LookupController @Inject()(val environment: Environment,
                                 controllerComponents: ControllerComponents,
                                 metrics: AwrsLookupMetrics,
                                 lookupService: EtmpLookupService,
                                 loggingUtils: LoggingUtils) extends BackendController(controllerComponents) {

  val referenceNotFoundString = "AWRS reference not found"

  def lookupByUrn(awrsRef: String): Action[AnyContent] = Action.async {
    implicit request =>
      val timer = metrics.startTimer(ApiType.LookupByURN)
      lookupService.lookupByUrn(awrsRef) map {
        response =>
          timer.stop()
          processResponse(response, ApiType.LookupByURN)(SearchResult.etmpByUrnReader(environment = environment), hc)
      }
  }

  def lookupByName(queryString: String): Action[AnyContent] = Action.async {
    implicit request =>
      val timer = metrics.startTimer(ApiType.LookupByName)
      lookupService.lookupByName(queryString) map {
        response =>
          timer.stop()
          processResponse(response, ApiType.LookupByName)(SearchResult.etmpByNameReader(environment = environment), hc)
      }
  }

  def processResponse(lookupResponse: HttpResponse, apiType: ApiType)(implicit fjs: play.api.libs.json.Reads[SearchResult], hc: HeaderCarrier) = {
    lookupResponse.status match {
      case OK => {
        val status = (lookupResponse.json \ "awrsStatus").validate[String]
        val endDate = (lookupResponse.json \ "endDate").validate[DateTime]
        val earliestDate = DateTime.parse("2017-04-01")
        (status, endDate) match {
          case (s: JsSuccess[String], e: JsSuccess[DateTime]) => {
            if (status.get.toLowerCase != "approved" && (endDate.get.isBefore(earliestDate) || endDate.get == earliestDate)) {
              DoAuditing(apiType, "Search Result", "success - capped (Prior 01/04/2017)", loggingUtils.eventTypeSuccess, metrics.incrementSuccessCounter)
              NotFound(referenceNotFoundString)
            } else {
              DoAuditing(apiType, "Search Result", "success", loggingUtils.eventTypeSuccess, metrics.incrementSuccessCounter)
              val convertedJson = lookupResponse.json.as[SearchResult]
              Ok(Json.toJson(convertedJson))
            }
          }
          case _ => {
            DoAuditing(apiType, "Search Result", "success", loggingUtils.eventTypeSuccess, metrics.incrementSuccessCounter)
            val convertedJson = lookupResponse.json.as[SearchResult]
            Ok(Json.toJson(convertedJson))
          }
        }
      }
      case NOT_FOUND =>
        DoAuditing(apiType, "Search Result", "NOT_FOUND", loggingUtils.eventTypeNotFound, metrics.incrementFailedCounter)
        NotFound(referenceNotFoundString)
      case BAD_REQUEST =>
        DoAuditing(apiType, "Search Result", "BAD_REQUEST", loggingUtils.eventTypeBadRequest, metrics.incrementFailedCounter)
        BadRequest(lookupResponse.body)
      case INTERNAL_SERVER_ERROR =>
        DoAuditing(apiType, "Search Result", "INTERNAL_SERVER_ERROR", loggingUtils.eventTypeInternalServerError, metrics.incrementFailedCounter)
        InternalServerError(lookupResponse.body)
      case SERVICE_UNAVAILABLE =>
        DoAuditing(apiType, "Search Result", "SERVICE_UNAVAILABLE", loggingUtils.eventTypeServiceUnavailable, metrics.incrementFailedCounter)
        ServiceUnavailable(lookupResponse.body)
      case _ =>
        DoAuditing(apiType, "Search Result", "OTHER_ERROR", loggingUtils.eventTypeGeneric, metrics.incrementFailedCounter)
        InternalServerError(lookupResponse.body)
    }
  }

  private def DoAuditing(apiType: ApiType, action: String, message: String, eventType: String, incrementCounter: (ApiType) => Unit)(implicit hc: HeaderCarrier) = {
    incrementCounter(apiType)
    loggingUtils.audit(loggingUtils.auditLookupTxName, Map(action -> message), eventType)
  }
}


