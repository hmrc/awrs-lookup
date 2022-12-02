/*
 * Copyright 2022 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.awrslookup.models.utils.CountryCodes
import utils.AwrsUnitTestTraits

class CountryCodeTest extends PlaySpec with AwrsUnitTestTraits {

  "CountryCodeTest" must {
    "successfully convert a country code to a country" in {
      CountryCodes.getCountry("GB") should be (Some("United Kingdom of Great Britain and Northern Ireland (the)"))
    }
    "successfully convert a country code to a country containing special characters" in {
      CountryCodes.getCountry("CI") should be (Some("Côte d'Ivoire"))
    }
    "return None if a country code is not found" in {
      CountryCodes.getCountry("ZZ") should be (None)
    }
  }
}
