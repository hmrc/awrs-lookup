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

package models

import java.time.LocalDate
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import uk.gov.hmrc.awrslookup.models.etmp.formatters.EtmpDateReader
import uk.gov.hmrc.awrslookup.models.frontend.{AwrsStatus, Business, Group, SearchResult}
import utils.{AwrsTestJson, AwrsUnitTestTraits}
import utils.TestUtil._

import java.time.format.DateTimeFormatter

class ModelReaderTest extends PlaySpec with AwrsUnitTestTraits {

  "ModelReaderTest" must {
    "successfully read the group json into a group object" in {
      val groupObject = AwrsTestJson.groupJson.as[SearchResult](SearchResult.etmpByUrnReader)
      groupObject.results.head.isInstanceOf[Group] shouldBe true
    }

    "successfully read the business json into a business object" in {
      val businessObject = AwrsTestJson.businessJson.as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.isInstanceOf[Business] shouldBe true
    }

    "successfully convert the business json country code to a country" in {
      val frCountryCode = "FR"
      val frCountry = "France"
      val businessJsonString = AwrsTestJson.businessJsonString
      val updatedJson = businessJsonString.updateEtmpCountry(frCountryCode)
      updatedJson should include(frCountryCode)
      updatedJson should not include frCountry
      val businessObject = Json.parse(updatedJson).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.info.get.address.get.addressCountry.get shouldBe frCountry
    }

    "if the country code is GB, do not add United Kingdom into the country field" in {
      val gbCountryCode = "GB"
      val gbCountry = "United Kingdom"
      val businessJsonString = AwrsTestJson.businessJsonString
      businessJsonString should include(gbCountryCode)
      businessJsonString should not include gbCountry
      val businessObject = AwrsTestJson.businessJson.as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.info.get.address.get.addressCountry shouldBe None
    }

    "Correctly format when only the date is provided" in {
      val date = "2017-4-1"
      val expected = "01 April 2017"
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStartDate(date).updateEtmpEndDate(date)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.registrationDate.get shouldBe expected
      businessObject.results.head.get.registrationEndDate.get shouldBe expected
    }

    "Correctly format when only the date is provided with leading zeros for month and day" in {
      val date = "2017-04-01"
      val expected = "01 April 2017"
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStartDate(date).updateEtmpEndDate(date)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.registrationDate.get shouldBe expected
      businessObject.results.head.get.registrationEndDate.get shouldBe expected
    }

    "Correctly format the date and time fields" in {
      val date = "2017-04-01T00:00:00.000+01:00"
      val expected = "01 April 2017"
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStartDate(date).updateEtmpEndDate(date)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.registrationDate.get shouldBe expected
      businessObject.results.head.get.registrationEndDate.get shouldBe expected
    }

    "Update date if they are before 1st April 2017" in {
      val date = "2010-4-1"
      val expected = "01 April 2017"
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStartDate(date).updateEtmpEndDate(date)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.registrationDate.get shouldBe expected
    }

    "do not update Dates if they are after 1st April 2017" in {
      val date = "2017-4-2"
      val expected = "02 April 2017"
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStartDate(date).updateEtmpEndDate(date)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.registrationDate.get shouldBe expected
      businessObject.results.head.get.registrationEndDate.get shouldBe expected
    }

    "correctly convert the 'Approved' etmp status to the frontend 'Approved' status" in {
      val etmpStatus = "Approved"
      val expectedStatus = AwrsStatus.Approved
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStatus(etmpStatus)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.status.get shouldBe expectedStatus
    }

    "correctly convert the 'Approved with Conditions' etmp status to the frontend 'Approved' status" in {
      val etmpStatus = "Approved with Conditions"
      val expectedStatus = AwrsStatus.Approved
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStatus(etmpStatus)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.status.get shouldBe expectedStatus
    }

    "correctly convert the 'De-registered' etmp status to the frontend 'DeRegistered' status when the dereg date has been reached" in {
      val etmpStatus = "De-registered"
      val expectedStatus = AwrsStatus.DeRegistered
      val pastDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern(EtmpDateReader.etmpDatePattern))
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStatus(etmpStatus).updateEtmpEndDate(pastDate)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.status.get shouldBe expectedStatus
    }

    "correctly convert the 'De-registered' etmp status to the frontend 'Approved' status when the dereg date has not been reached" in {
      val etmpStatus = "De-registered"
      val expectedStatus = AwrsStatus.Approved
      val futureDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern(EtmpDateReader.etmpDatePattern))
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStatus(etmpStatus).updateEtmpEndDate(futureDate)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.status.get shouldBe expectedStatus
    }

    "correctly convert the 'Revoked' etmp status to the frontend 'Revoked' status" in {
      val etmpStatus = "Revoked"
      val expectedStatus = AwrsStatus.Revoked
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStatus(etmpStatus)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.status.get shouldBe expectedStatus
    }

    "correctly convert the 'Revoked under Review / Appeal' etmp status to the frontend 'Revoked' status" in {
      val etmpStatus = "Revoked under Review / Appeal"
      val expectedStatus = AwrsStatus.Revoked
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStatus(etmpStatus)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.status.get shouldBe expectedStatus
    }

    "return any other etmp status as a 'NotFound' status" in {
      val etmpStatus = "rubbish"
      val expectedStatus = AwrsStatus.NotFound(etmpStatus)
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStatus(etmpStatus)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.status.get shouldBe expectedStatus
    }
  }

  implicit class JsonStringUtil(jsonString: String) {

    def updateEtmpCountry(newCountry: String): String =
      updateJson(
        Json.obj("wholesaler" ->
          Json.obj("businessAddress" ->
            Json.obj("country" -> newCountry)
          )
        ), jsonString)

    def updateEtmpStartDate(newDate: String): String =
      updateJson(Json.obj("startDate" -> newDate), jsonString)

    def updateEtmpEndDate(newDate: String): String =
      updateJson(Json.obj("endDate" -> newDate), jsonString)

    def updateEtmpStatus(newStatus: String): String =
      updateJson(Json.obj("awrsStatus" -> newStatus), jsonString)

  }

}
