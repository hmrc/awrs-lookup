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

import jakarta.inject.Singleton
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject

@Singleton
class FeatureSwitches @Inject()(config: ServicesConfig) extends Logging {

  def enable(property: String): Unit = sys.props += (property-> "true")
  def disable(property: String): Unit = sys.props += (property-> "false")

  def hipSwitch(): Boolean = {
    isEnabled("hipSwitch")
  }

  private def isEnabled(feature: String): Boolean = {

    sys.props.get(s"feature.$feature") match {
      case Some(value) => value.toBooleanOption.getOrElse(false)
      case _ => config.getString(s"feature.$feature").toBooleanOption.getOrElse(false)
    }
  }
}
