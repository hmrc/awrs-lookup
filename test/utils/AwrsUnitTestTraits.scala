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

package utils

import org.scalatest.BeforeAndAfter
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Environment
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.http.HeaderCarrier
import scala.language.implicitConversions

import scala.concurrent.Future

trait AwrsUnitTestTraits extends PlaySpec with MockitoSugar with BeforeAndAfter with GuiceOneAppPerSuite with I18nSupport {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  implicit def convertToOption[T](value: T): Option[T] = Some(value)

  implicit def convertToFuture[T](value: T): Future[Option[T]] = Future.successful(value)

  implicit def convertToFuture[T](err: Throwable): Future[Option[T]] = Future.failed(err)

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val environment: Environment = app.injector.instanceOf[Environment]

}
