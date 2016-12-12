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

import play.api.libs.json.Json
import uk.gov.hmrc.awrslookup.models.frontend.{AwrsStatus, Business, Group, SearchResult}
import utils.{AwrsTestJson, AwrsUnitTestTraits}
import utils.TestUtil._

class ModelReaderTest extends AwrsUnitTestTraits {

  "ModelReaderTest" should {
    "successfully read the group json into a group object" in {
      val groupObject = AwrsTestJson.groupJson.as[SearchResult](SearchResult.etmpByUrnReader)
      groupObject.results.head.isInstanceOf[Group] shouldBe true
    }

    "successfully read the business json into a business object" in {
      val businessObject = AwrsTestJson.businessJson.as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.isInstanceOf[Business] shouldBe true
    }

    "successfully read the a by name json into a list of AwrsEntry objects" in {
      val groupObject = AwrsTestJson.byNameJson.as[SearchResult](SearchResult.etmpByNameReader)
      groupObject.results.size shouldBe 2
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

    "Correctly format the date fields" in {
      val date = "2017-4-1"
      val expected = "01 April 2017"
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStartDate(date).updateEtmpEndDate(date)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.registrationDate.get shouldBe expected
      businessObject.results.head.get.registrationEndDate.get shouldBe expected
    }

    "update Dates if they are before 1st April 2017" in {
      val date = "2017-3-31"
      val expected = "01 April 2017"
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStartDate(date).updateEtmpEndDate(date)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.registrationDate.get shouldBe expected
      businessObject.results.head.get.registrationEndDate.get shouldBe expected
    }

    "do not update Dates if they are after 1st April 2017" in {
      val date = "2017-4-2"
      val expected = "02 April 2017"
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStartDate(date).updateEtmpEndDate(date)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.registrationDate.get shouldBe expected
      businessObject.results.head.get.registrationEndDate.get shouldBe expected
    }

    "correctly convert the 'active' etmp status to the frontend 'Approved' status" in {
      val etmpStatus = "active"
      val expectedStatus = AwrsStatus.Approved
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStatus(etmpStatus)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.status.get shouldBe expectedStatus
    }

    "correctly convert the 'deregistered' etmp status to the frontend 'DeRegistered' status" in {
      val etmpStatus = "deregistered"
      val expectedStatus = AwrsStatus.DeRegistered
      val updatedBusinessJsonString = AwrsTestJson.businessJsonString.updateEtmpStatus(etmpStatus)
      val businessObject = Json.parse(updatedBusinessJsonString).as[SearchResult](SearchResult.etmpByUrnReader)
      businessObject.results.head.get.status.get shouldBe expectedStatus
    }

    "correctly convert the 'revoked' etmp status to the frontend 'Revoked' status" in {
      val etmpStatus = "revoked"
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
