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

package metrics

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer.Context
import com.google.inject.Inject
import uk.gov.hmrc.awrslookup.models.ApiType
import uk.gov.hmrc.awrslookup.models.ApiType.ApiType

class AwrsLookupMetrics @Inject() () {

  private val metricRegistry = new MetricRegistry

  private val timers = Map(
    ApiType.LookupByURN -> metricRegistry.timer("etmp-lookup-by-urn-response-timer")
  )

  private val successCounters = Map(
    ApiType.LookupByURN -> metricRegistry.counter("etmp-lookup-by-urn-success")
  )

  private val failedCounters = Map(
    ApiType.LookupByURN -> metricRegistry.counter("etmp-lookup-by-urn-failed")
  )

  def startTimer(api: ApiType): Context = timers(api).time()

  def incrementSuccessCounter(api: ApiType): Unit = successCounters(api).inc()

  def incrementFailedCounter(api: ApiType): Unit = failedCounters(api).inc()

}
