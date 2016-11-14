package io.strongtyped.circe

import io.circe.{ Decoder, ObjectEncoder }
import io.circe.parser.decode, io.circe.syntax._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import cats.syntax.either._
import io.circe.Decoder._
import io.circe.generic.extras.encoding.EnumerationEncoder
import io.circe.generic.extras.decoding.EnumerationDecoder
import io.circe.generic.extras.encoding.ConfiguredObjectEncoder
import io.circe.generic.extras.decoding.ConfiguredDecoder
import io.circe._
import shapeless.Lazy
import io.circe.generic.extras.semiauto.{ deriveEnumerationDecoder, deriveEnumerationEncoder }

trait CirceExt {

  type Codec[A] = Encoder[A] with Decoder[A]


  implicit val customConfig: Configuration =
    Configuration.default.withDefaults.withDiscriminator("_type")

    
  def deriveCodec[A](implicit encode: Lazy[ConfiguredObjectEncoder[A]],
                     decode: Lazy[ConfiguredDecoder[A]]): Codec[A] =
    new Encoder[A] with Decoder[A] {
      val enc = deriveEncoder[A]
      val dec = deriveDecoder[A]
      def apply(a: A): Json = enc(a)
      def apply(c: HCursor): Result[A] = dec(c)
    }


  def deriveEnumCodec[A](implicit encode: Lazy[EnumerationEncoder[A]],
                         decode: Lazy[EnumerationDecoder[A]]): Codec[A] =
    new Encoder[A] with Decoder[A] {
      val enc = deriveEnumerationEncoder[A]
      val dec = deriveEnumerationDecoder[A]
      def apply(a: A): Json = enc(a)
      def apply(c: HCursor): Result[A] = dec(c)
    }

}

object CirceExt extends CirceExt