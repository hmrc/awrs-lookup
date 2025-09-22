/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.awrslookup.utils

import play.api.Logging

import scala.util.Try

object FeatureSwitches extends Logging {

  def enable(property: String): Unit = sys.props += (property-> "true")
  def disable(property: String): Unit = sys.props += (property-> "false")

  def hipEnabled(): Boolean = {
    isEnabled("hipEnabled")
  }

  private def isEnabled(feature: String): Boolean = {
    sys.props.get(s"feature.$feature").exists {
      feature =>
        Try(feature.toBoolean).getOrElse(false)
    }
  }
}
