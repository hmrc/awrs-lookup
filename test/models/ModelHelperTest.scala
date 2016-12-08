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

import uk.gov.hmrc.awrslookup.models.frontend.AwrsStatus
import uk.gov.hmrc.awrslookup.models.utils.ModelHelper
import utils.AwrsUnitTestTraits

class ModelHelperTest extends AwrsUnitTestTraits {

  "ModelHelperTest" should {
    "should return a status of Approved if no end date exists" in {
      ModelHelper.getStatus(None) shouldBe AwrsStatus.Approved
    }
    "should return a status of Approved if an end date exists but that date has not yet been reached" in {
      ModelHelper.getStatus("03 July 2020") shouldBe AwrsStatus.Approved

    }
    "should return a status of Deregistered if an end date exists and that date is in the past" in {
      ModelHelper.getStatus("03 July 2020") shouldBe AwrsStatus.DeRegistered

    }
  }
}
