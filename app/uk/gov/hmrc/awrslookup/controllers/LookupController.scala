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

package uk.gov.hmrc.awrslookup.controllers

import javax.inject.Inject
import metrics.AwrsLookupMetrics

import java.time.LocalDate
import play.api.Environment
import play.api.libs.json.{JsResultException, JsString, JsSuccess, JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.awrslookup.models.ApiType
import uk.gov.hmrc.awrslookup.models.ApiType.ApiType
import uk.gov.hmrc.awrslookup.models.frontend._
import uk.gov.hmrc.awrslookup.services.LookupService
import uk.gov.hmrc.awrslookup.utils.LoggingUtils
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext
import scala.util.Try

class LookupController @Inject()(val environment: Environment,
                                 controllerComponents: ControllerComponents,
                                 metrics: AwrsLookupMetrics,
                                 lookupService: LookupService,
                                 loggingUtils: LoggingUtils)(implicit ec: ExecutionContext) extends BackendController(controllerComponents) {

  val referenceNotFoundString = "AWRS reference not found"

  val errorsNode = "errors"
  val codeNode = "code"

  def lookupByUrn(awrsRef: String): Action[AnyContent] = Action.async {
    implicit request =>
      val timer = metrics.startTimer(ApiType.LookupByURN)
      lookupService.lookupByUrn(awrsRef) map {
        response =>
          timer.stop()
          processResponse(response, ApiType.LookupByURN)(SearchResult.etmpByUrnReader(environment = environment), hc)
      }
  }

  // Temporary code to prevent confidential information being logged if Json cannot be parsed
  private def parseSearchResultAndPreventSensitiveInfoLeakOnFailure(json: JsValue)(implicit fjs: play.api.libs.json.Reads[SearchResult]): SearchResult = {
    try {
      json.as[SearchResult]
    } catch {
      case e: Exception => throw new RuntimeException("Error parsing lookup response Json")
    }
  }

  def processResponse(lookupResponse: HttpResponse, apiType: ApiType)(implicit fjs: play.api.libs.json.Reads[SearchResult], hc: HeaderCarrier): Result = {
    lookupResponse.status match {
      case OK =>
        val status = (lookupResponse.json \ "awrsStatus").validate[String]
        val endDate = (lookupResponse.json \ "endDate").validate[LocalDate]
        val earliestDate = LocalDate.parse("2017-04-01")
        (status, endDate) match {
          case (_: JsSuccess[String], _: JsSuccess[LocalDate]) =>
            if (status.get.toLowerCase != "approved" && (endDate.get.isBefore(earliestDate) || endDate.get == earliestDate)) {
              doAuditing(apiType, "Search Result", "success - capped (Prior 01/04/2017)", loggingUtils.eventTypeSuccess, metrics.incrementSuccessCounter)
              NotFound(referenceNotFoundString)
            } else {
              doAuditing(apiType, "Search Result", "success", loggingUtils.eventTypeSuccess, metrics.incrementSuccessCounter)

              val convertedJson = parseSearchResultAndPreventSensitiveInfoLeakOnFailure(lookupResponse.json)

              Ok(Json.toJson(convertedJson))
            }
          case _ =>
            doAuditing(apiType, "Search Result", "success", loggingUtils.eventTypeSuccess, metrics.incrementSuccessCounter)
            val convertedJson = parseSearchResultAndPreventSensitiveInfoLeakOnFailure(lookupResponse.json)
            Ok(Json.toJson(convertedJson))
        }
      case NOT_FOUND =>
        doAuditing(apiType, "Search Result", "NOT_FOUND", loggingUtils.eventTypeNotFound, metrics.incrementFailedCounter)
        NotFound(referenceNotFoundString)
      case BAD_REQUEST =>
        doAuditing(apiType, "Search Result", "BAD_REQUEST", loggingUtils.eventTypeBadRequest, metrics.incrementFailedCounter)
        BadRequest(lookupResponse.body)
      case INTERNAL_SERVER_ERROR =>
        doAuditing(apiType, "Search Result", "INTERNAL_SERVER_ERROR", loggingUtils.eventTypeInternalServerError, metrics.incrementFailedCounter)
        InternalServerError(lookupResponse.body)
      case SERVICE_UNAVAILABLE =>
        doAuditing(apiType, "Search Result", "SERVICE_UNAVAILABLE", loggingUtils.eventTypeServiceUnavailable, metrics.incrementFailedCounter)
        ServiceUnavailable(lookupResponse.body)
      case UNPROCESSABLE_ENTITY  =>
        hipErrorCode(lookupResponse.body) match {
          case Some("003") =>
            doAuditing(apiType, "Search Result", "UNPROCESSABLE_ENTITY, BAD_REQUEST", loggingUtils.eventTypeNotFound, metrics.incrementFailedCounter)
            BadRequest(lookupResponse.body)
          case Some("006") =>
            doAuditing(apiType, "Search Result", "UNPROCESSABLE_ENTITY, NOT_FOUND", loggingUtils.eventTypeNotFound, metrics.incrementFailedCounter)
            NotFound(referenceNotFoundString)
          case _ =>
            doAuditing(apiType, "Search Result", "UNPROCESSABLE_ENTITY, OTHER_ERROR", loggingUtils.eventTypeGeneric, metrics.incrementFailedCounter)
            InternalServerError(lookupResponse.body)
        }
      case _ =>
        doAuditing(apiType, "Search Result", "OTHER_ERROR", loggingUtils.eventTypeGeneric, metrics.incrementFailedCounter)
        InternalServerError(lookupResponse.body)
    }
  }

  private def doAuditing(apiType: ApiType,
                         action: String,
                         message: String,
                         eventType: String,
                         incrementCounter: ApiType => Unit)(implicit hc: HeaderCarrier): Unit = {
    incrementCounter(apiType)
    loggingUtils.audit(loggingUtils.auditLookupTxName, Map(action -> message), eventType)
  }

  private def hipErrorCode(jsonString: String): Option[String] = {

    for {
      errorMessageBody <- Try(Json.parse(jsonString)).toOption
      errorCodeNode <- (errorMessageBody \ errorsNode \ codeNode).toOption
    } yield {
      errorCodeNode.asInstanceOf[JsString].value
    }
  }
}
