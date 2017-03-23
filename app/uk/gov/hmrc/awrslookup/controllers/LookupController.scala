/*
 * Copyright 2017 HM Revenue & Customs
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
import play.api.libs.json.{JsDefined, JsError, JsSuccess, Json}
import play.api.mvc._
import metrics.AwrsLookupMetrics
import uk.gov.hmrc.awrslookup.models.ApiType
import uk.gov.hmrc.awrslookup.models.ApiType.ApiType
import uk.gov.hmrc.awrslookup.models.frontend._
import uk.gov.hmrc.awrslookup.services.EtmpLookupService
import uk.gov.hmrc.awrslookup.utils.LoggingUtils
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.microservice.controller.BaseController
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.concurrent.ExecutionContext.Implicits.global

class LookupController @Inject()(val environment: Environment) extends BaseController with LoggingUtils {

  val referenceNotFoundString = "AWRS reference not found"

  val lookupService: EtmpLookupService = EtmpLookupService
  val metrics: AwrsLookupMetrics = AwrsLookupMetrics

  def lookupByUrn(awrsRef: String): Action[AnyContent] = Action.async {
    implicit request =>
      val timer = metrics.startTimer(ApiType.LookupByURN)
      lookupService.lookupByUrn(awrsRef) map {
        response => timer.stop()
          processResponse(response, ApiType.LookupByURN)(SearchResult.etmpByUrnReader(environment = environment), hc)
      }
  }

  def lookupByName(queryString: String): Action[AnyContent] = Action.async {
    implicit request =>
      val timer = metrics.startTimer(ApiType.LookupByName)
      lookupService.lookupByName(queryString) map {
        response => timer.stop()
          processResponse(response, ApiType.LookupByName)(SearchResult.etmpByNameReader(environment = environment), hc)
      }
  }

  def processResponse(lookupResponse: HttpResponse, apiType : ApiType)(implicit fjs : play.api.libs.json.Reads[SearchResult], hc: HeaderCarrier) = {
        lookupResponse.status match {
          case OK => {
              val status = (lookupResponse.json \ "awrsStatus").validate[String]
              val endDate =  (lookupResponse.json  \ "endDate").validate[DateTime]
              val earliestDate = DateTime.parse("2017-04-01")

              (status,endDate) match {
                case (s: JsSuccess[String],e: JsSuccess[DateTime]) => {
                  if ( status.get.toLowerCase != "approved" && endDate.get.isBefore(earliestDate)) {
                    metrics.incrementSuccessCounter(apiType)
                    audit(auditLookupTxName, Map("Search Result" -> "success - capped (Prior 01/04/2017)"), eventTypeSuccess)
                    NotFound(referenceNotFoundString)
                  } else {
                    metrics.incrementSuccessCounter(apiType)
                    audit(auditLookupTxName, Map("Search Result" -> "success"), eventTypeSuccess)
                    val convertedJson = lookupResponse.json.as[SearchResult]
                    Ok(Json.toJson(convertedJson))
                  }
                }
                case _ => {
                  metrics.incrementSuccessCounter(apiType)
                  audit(auditLookupTxName, Map("Search Result" -> "success"), eventTypeSuccess)
                  val convertedJson = lookupResponse.json.as[SearchResult]
                  Ok(Json.toJson(convertedJson))
                }
              }
          }
          case NOT_FOUND =>
            metrics.incrementFailedCounter(apiType)
            audit(auditLookupTxName, Map("Search Result" -> "NOT_FOUND"), eventTypeNotFound)
            NotFound(referenceNotFoundString)
          case BAD_REQUEST =>
            metrics.incrementFailedCounter(apiType)
            audit(auditLookupTxName, Map("Search Result" -> "BAD_REQUEST"), eventTypeNotFound)
            BadRequest(lookupResponse.body)
          case INTERNAL_SERVER_ERROR =>
            metrics.incrementFailedCounter(apiType)
            audit(auditLookupTxName, Map("Search Result" -> "INTERNAL_SERVER_ERROR"), eventTypeNotFound)
            InternalServerError(lookupResponse.body)
          case SERVICE_UNAVAILABLE =>
            metrics.incrementFailedCounter(apiType)
            audit(auditLookupTxName, Map("Search Result" -> "SERVICE_UNAVAILABLE"), eventTypeNotFound)
            ServiceUnavailable(lookupResponse.body)
          case _ =>
            metrics.incrementFailedCounter(apiType)
            audit(auditLookupTxName, Map("Search Result" -> "OTHER_ERROR"), eventTypeNotFound)
            InternalServerError(lookupResponse.body)
        }
    }
}


