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

package models

import uk.gov.hmrc.awrslookup.models.frontend.{Business, Group, SearchResult}
import utils.{AwrsTestJson, AwrsUnitTestTraits}

class ModelReaderTest extends AwrsUnitTestTraits {

  "ModelReaderTest" should {
    "successfully read the group json into a group object" in {
      val groupObject = AwrsTestJson.groupJson.as[SearchResult](SearchResult.etmpReader)
      groupObject.results.head.isInstanceOf[Group] shouldBe true
    }

    "successfully read the business json into a business object" in {
      val businessObject = AwrsTestJson.businessJson.as[SearchResult](SearchResult.etmpReader)
      businessObject.results.head.isInstanceOf[Business] shouldBe true
    }

    "successfully convert the business json country code to a country" in {
      val gbCountryCode = "GB"
      val gbCountry = "United Kingdom"
      val businessJsonString = AwrsTestJson.businessJsonString
      businessJsonString should include (gbCountryCode)
      businessJsonString should not include gbCountry
      val businessObject = AwrsTestJson.businessJson.as[SearchResult](SearchResult.etmpReader)
      businessObject.results.head.get.info.get.address.get.addressCountry.get shouldBe gbCountry
    }

  }

}
