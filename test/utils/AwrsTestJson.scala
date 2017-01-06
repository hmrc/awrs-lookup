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

package utils

import utils.TestUtil._

object AwrsTestJson extends AwrsTestJson

trait AwrsTestJson extends AwrsPathConstants {

  lazy val groupJsonString = load(groupJsonPath)
  lazy val businessJsonString = load(businessJsonPath)
  lazy val byNameJsonString = load(byNameJsonPath)

  lazy val groupJson = loadAndParseJson(groupJsonPath)
  lazy val businessJson = loadAndParseJson(businessJsonPath)
  lazy val byNameJson = loadAndParseJson(byNameJsonPath)

}
