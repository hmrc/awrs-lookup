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

package uk.gov.hmrc.awrslookup.models.frontend

import play.api.libs.json._

case class SearchResult(results: List[AwrsEntry])

object SearchResult {

  def etmpByUrnReader(implicit environment: play.api.Environment): Reads[SearchResult] = new Reads[SearchResult] {
    def reads(js: JsValue): JsResult[SearchResult] =
      for {
        result <- js.validate[AwrsEntry](AwrsEntry.etmpReader)
      } yield {
        SearchResult(results = List(result))
      }
  }

  def etmpByNameReader(implicit environment: play.api.Environment): Reads[SearchResult] = new Reads[SearchResult] {
    def reads(js: JsValue): JsResult[SearchResult] = {
      for {
        result <- (js \ "registrations").validate[List[AwrsEntry]](Reads.list(AwrsEntry.etmpReader))
      } yield {
        SearchResult(results = result)
      }
    }
  }

  implicit val frontEndFormatter = Json.format[SearchResult]
}
