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

package metrics

import com.codahale.metrics.Timer
import com.codahale.metrics.Timer.Context
import uk.gov.hmrc.awrslookup.models.ApiType
import uk.gov.hmrc.awrslookup.models.ApiType.ApiType
import uk.gov.hmrc.play.graphite.MicroserviceMetrics

trait AwrsLookupMetrics extends MicroserviceMetrics {
  def startTimer(api: ApiType): Timer.Context

  def incrementSuccessCounter(api: ApiType.ApiType): Unit

  def incrementFailedCounter(api: ApiType.ApiType): Unit
}

object AwrsLookupMetrics extends AwrsLookupMetrics {

  val timers = Map(
    ApiType.LookupByURN -> metrics.defaultRegistry.timer("etmp-lookup-by-urn-response-timer"),
    ApiType.LookupByName -> metrics.defaultRegistry.timer("etmp-lookup-by-name-response-timer")
   )

  val successCounters = Map(
    ApiType.LookupByURN -> metrics.defaultRegistry.counter("etmp-lookup-by-urn-success"),
    ApiType.LookupByName -> metrics.defaultRegistry.counter("etmp-lookup-by-name-success")
  )

  val failedCounters = Map(
    ApiType.LookupByURN -> metrics.defaultRegistry.counter("etmp-lookup-by-urn-failed"),
    ApiType.LookupByName -> metrics.defaultRegistry.counter("etmp-lookup-by-name-failed")
  )

  override def startTimer(api: ApiType): Context = timers(api).time()

  override def incrementSuccessCounter(api: ApiType): Unit = successCounters(api).inc()

  override def incrementFailedCounter(api: ApiType): Unit = failedCounters(api).inc()
}
