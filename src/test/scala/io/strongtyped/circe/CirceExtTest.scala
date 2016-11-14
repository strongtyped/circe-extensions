package io.strongtyped.circe

import cats.syntax.either._
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, Json, Printer}
import io.circe.parser._
import io.circe.syntax._
import io.strongtyped.circe.CirceExt.Codec
import org.scalatest.{FunSuite, Matchers}

class CirceExtTest extends FunSuite with Matchers {


  test("serialize and deserialize an ADT using a type hint") {

    sealed trait Foo
    case class Bar(value: String) extends Foo
    case class Baz(value: Int) extends Foo
    case class Qux(value: Boolean) extends Foo
    object Foo {
      implicit val codec: Codec[Foo] = CirceExt.withTypeHint.codec[Foo]
    }

    val jsonStr =
      """
        |{
        |  "_type": "Bar",
        |  "value": "abc"
        |}
      """.stripMargin

      val barIsFoo: Foo = Bar("abc")

    val expectedJson = parse(jsonStr).getOrElse(Json.Null)

    barIsFoo.asJson shouldBe expectedJson

    // and we should be able to parse that same json String
    val statusFromJson = decode[Foo](jsonStr)

    statusFromJson match {
      case Right(parsedStatus) => parsedStatus shouldBe Bar("abc")
      case Left(err) => fail(s"Got an error instead: $err")
    }
  }

  test("serialize and deserialize an enum ADT convert it to a JString") {

    sealed trait Status
    case object New extends Status
    case object Updated extends Status
    case object Deleted extends Status
    object Status {
      implicit val codec: Codec[Status] = CirceExt.enum.codec[Status]
    }

    case class Foo(value: String, status: Status)
    implicit val codec: Codec[Foo] = CirceExt.codec[Foo]

    val jsonStr =
      """
        |{
        |  "value": "abc",
        |  "status": "New"
        |}
      """.stripMargin

    val foo = Foo("abc", New)
    val jsonFromFoo = foo.asJson.pretty(Printer.spaces2)

    val expectedJson = parse(jsonStr).getOrElse(Json.Null)
    foo.asJson shouldBe expectedJson

    // and we should be able to parse that same json String
    val fooFromJson = decode[Foo](jsonFromFoo)

    fooFromJson match {
      case Right(parsedFoo) => parsedFoo.status shouldBe New
      case Left(err) => fail(s"Got an error instead: $err")
    }
  }

  test("using it with case classes should also work") {
    case class Foo(value: String)
    implicit val codec: Codec[Foo] = CirceExt.withTypeHint("_type").codec[Foo]
    val jsonStr =
      """
        |{
        |  "value": "abc"
        |}
      """.stripMargin

    val foo: Foo = Foo("abc")

    val expectedJson = parse(jsonStr).getOrElse(Json.Null)

    foo.asJson shouldBe expectedJson

    // and we should be able to parse that same json String
    val statusFromJson = decode[Foo](jsonStr)

    statusFromJson match {
      case Right(parsedStatus) => parsedStatus shouldBe Foo("abc")
      case Left(err) => fail(s"Got an error instead: $err")
    }
  }

  test("nested sealed trait hierarchy works still works") {

    sealed trait Foo
    case class Baz(value: Int) extends Foo
    sealed trait Qux extends Foo
    case class Bar(value: String) extends Qux

    object Foo {
      implicit val codec: Codec[Foo] = CirceExt.withTypeHint("_type").codec[Foo]
    }

    val jsonStr =
      """
        |{
        |  "_type": "Bar",
        |  "value": "abc"
        |}
      """.stripMargin

    val barIsFoo: Foo = Bar("abc")

    val expectedJson = parse(jsonStr).getOrElse(Json.Null)

    barIsFoo.asJson shouldBe expectedJson

    // and we should be able to parse that same json String
    val statusFromJson = decode[Foo](jsonStr)

    statusFromJson match {
      case Right(parsedStatus) => parsedStatus shouldBe Bar("abc")
      case Left(err) => fail(s"Got an error instead: $err")
    }
  }
}


