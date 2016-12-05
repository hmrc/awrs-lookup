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

package uk.gov.hmrc.awrslookup.models.etmp.formatters

import play.api.libs.json.{JsResult, JsValue, Reads}
import uk.gov.hmrc.awrslookup.models.frontend._

trait EtmpReaders {

//  val addressReader = new Reads[Address] {
//    def reads(js: JsValue): JsResult[Address] =
//      for {
//        addressLine1 <- (js \ "addressLine1").validate[String]
//        addressLine2 <- (js \ "addressLine2").validateOpt[String]
//        addressLine3 <- (js \ "addressLine3").validateOpt[String]
//        addressLine4 <- (js \ "addressLine4").validateOpt[String]
//        postcode <- (js \ "postcode").validateOpt[String]
//        countryCode <- (js \ "country").validateOpt[String]
//      } yield {
//        Address(postcode = postcode, addressLine1 = addressLine1, addressLine2 = addressLine2, addressLine3 = addressLine3,
//          addressLine4 = addressLine4, addressCountry = countryCode)
//      }
//  }
//
//  val infoReader = new Reads[Info] {
//    def reads(js: JsValue): JsResult[Info] =
//      for {
//        companyName <- (js \ "companyName").validateOpt[String]
//        tradingName <- (js \ "tradingName").validateOpt[String]
//        businessAddress <- (js \ "businessAddress").validate[Address]
//      } yield {
//        Info(businessName = companyName,
//        tradingName = tradingName,
//        fullName = None, // TODO how to handle SoleTrader
//        address = Some(businessAddress))
//      }
//  }
//
//  val businessReader = new Reads[Business] {
//    def reads(js: JsValue): JsResult[Business] =
//      for {
//        awrsRegistrationNumber <- (js \ "awrsRegistrationNumber").validate[String]
//        startDate <- (js \ "startDate").validate[String]
//        endDate <- (js \ "endDate").validateOpt[String]
//        wholesaler <- (js \ "wholesaler").validate[Info]
//      } yield {
//        Business(awrsRef = awrsRegistrationNumber,
//          registrationDate = startDate,
//          status = endDate match {
//            case Some(_) => AwrsStatus.DeRegistered
//            case _ => AwrsStatus.Approved
//          },
//          info = wholesaler,
//          deRegistrationDate = endDate)
//      }
//  }
//
//  val groupReader = new Reads[Group] {
//    def reads(js: JsValue): JsResult[Group] =
//      for {
//        awrsRegistrationNumber <- (js \ "awrsRegistrationNumber").validate[String]
//        startDate <- (js \ "startDate").validate[String]
//        endDate <- (js \ "endDate").validateOpt[String]
//        wholesaler <- (js \ "wholesaler").validate[Info]
//        groupMembers <- (js \ "groupMembers").validate[List[Info]]
//      } yield {
//        Group(awrsRef = awrsRegistrationNumber,
//          registrationDate = startDate,
//          status = endDate match {
//            case Some(_) => AwrsStatus.DeRegistered
//            case _ => AwrsStatus.Approved
//          },
//          info = wholesaler,
//          members = groupMembers,
//          deRegistrationDate = endDate)
//      }
//  }
//
//  val searchResultReader = new Reads[SearchResult] {
//    def reads(js: JsValue): JsResult[SearchResult] =
//      for {
//        business <- (js).validate[Business]
//        group <- (js).validate[Group]
//      } yield {
//        println("BUSINESS::\n"+business)
//        println("GROUP::\n"+group)
//        SearchResult(results = List())
//      }
//  }
}
