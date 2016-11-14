package io.strongtyped.circe

import cats.syntax.either._
import io.circe.Decoder._
import io.circe.generic.decoding.DerivedDecoder
import io.circe.generic.encoding.DerivedObjectEncoder
import io.circe.generic.semiauto._
import io.circe._
import shapeless.Lazy

object CirceExt {

  type Codec[A] = Encoder[A] with Decoder[A]

  def codec[A](implicit encode: Lazy[DerivedObjectEncoder[A]],
               decode: Lazy[DerivedDecoder[A]]): Codec[A] =
    new Encoder[A] with Decoder[A] {
      val enc = deriveEncoder[A]
      val dec = deriveDecoder[A]
      def apply(a: A): Json = enc(a)
      def apply(c: HCursor): Result[A] = dec(c)
    }

  def withTypeHint = DerivationWithTypeHint("_type")
  def withTypeHint(hint: String) = DerivationWithTypeHint(hint)

  case class DerivationWithTypeHint(hint: String) {

    def encoder[A](implicit encode: Lazy[DerivedObjectEncoder[A]]) = {

      def addCustomTypeHint(json: Json): Json = {

        val modified =
          for {
            // can only work if json has only one element at top level
            // which will be used as type hint value
            size <- json.hcursor.fields.map(_.size) if size == 1

            // if we only have one element, we pick the field name
            typeName <- json.hcursor.fields.flatMap(_.headOption)

            // and we take it's body, but only if it's a JObject
            body <- json.hcursor.downField(typeName).focus if body.isObject

            // TODO: we must check if body has not
            // a property clashing with the type hint

          } yield {

            // finally, we add the 'hint' using the type name
            body.mapObject { obj =>
              obj.add(hint, Json.fromString(typeName))
            }
          }

        // the returned json is expected to have the type hint
        // on the same level as the other properties
        // we fallback to the original json if necessary
        modified.getOrElse(json)

      }

      deriveEncoder[A].mapJson(addCustomTypeHint)
    }

    def decoder[A](implicit decode: Lazy[DerivedDecoder[A]]) = {

      val derivedDecoder = deriveDecoder[A]

      new Decoder[A] {

        def apply(cursor: HCursor): Result[A] = {

          // find field holding the type info
          val hintField = cursor.downField(hint).as[String]

          hintField.flatMap { typeHint =>
            // reconstruct the original json format as expected by Circe
            val circeParseableJson =
              cursor.top.mapObject { obj =>
                // build an new Json object using typeHint as top level property
                // ex: { "Bar": { "value": "abc" } }
                obj.add(typeHint, cursor.top)
              }

            derivedDecoder(circeParseableJson.hcursor)

          }.recoverWith {
            // if we can't find the type hint, than we fallback to default derived decoder
            case _ => derivedDecoder(cursor)
          }
        }
      }
    }

    def codec[A](implicit encode: Lazy[DerivedObjectEncoder[A]],
                 decode: Lazy[DerivedDecoder[A]]): Codec[A] =
      new Encoder[A] with Decoder[A] {
        val enc = encoder[A]
        val dec = decoder[A]
        def apply(a: A): Json = enc(a)
        def apply(c: HCursor): Result[A] = dec(c)
      }
  }

  val enum = EnumDerivation

  object EnumDerivation {

    def encoder[A](implicit encode: Lazy[DerivedObjectEncoder[A]]) = {

      def addCustomTypeHint(json: Json): Json = {
        val modified =
          for {
            // can only work if json has only one element at top level
            // which will be used as type hint
            size <- json.hcursor.fields.map(_.size) if size == 1

            // if we only have one element, we pick the field name
            typeName <- json.hcursor.fields.flatMap(_.headOption)
          } yield Json.fromString(typeName)

        // the return jsoned is expected to have the type hint
        // on the same level as the other properties
        modified.getOrElse(json)

      }

      deriveEncoder[A].mapJson(addCustomTypeHint)
    }

    def decoder[A](implicit decode: Lazy[DerivedDecoder[A]]) = {

      val derivedDecoder = deriveDecoder[A]

      new Decoder[A] {

        def apply(cursor: HCursor): Result[A] = {
          val circeParseableJson =
            cursor.as[Json].map { json =>
              json.asString match {
                case Some(str) => Json.obj(str -> Json.obj())
                case _ => json
              }
            }
          circeParseableJson.flatMap { json =>
            derivedDecoder(json.hcursor)
          }

        }
      }

    }

    def codec[A](implicit encode: Lazy[DerivedObjectEncoder[A]],
                 decode: Lazy[DerivedDecoder[A]]): Codec[A] =
      new Encoder[A] with Decoder[A] {
        val enc = encoder[A]
        val dec = decoder[A]
        def apply(a: A): Json = enc(a)
        def apply(c: HCursor): Result[A] = dec(c)
      }

  }
}
