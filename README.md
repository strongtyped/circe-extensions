# Circe Extensions

The original intention of this project was to explore Circe and see if it was possible to have custom type hints and enumerations mapped to JString. It turns our that since 0.6.0 circe-extras provides exactly what I was trying to achieve, but at compile time.

I will keep this repo though as a reference to myself. 

It contains now only an example for configuration and the `type Codec[A] = Encoder[A] with Decoder[A]`. That's all!

## type Codec[A] = Encoder[A] with Decoder[A]

CirceExt can be used as a example to for using circe-extras.

It provides a `deriveCodec` and `deriveEnumCodec` utility methods to generate Encoder[A] and Decoder[A] altogether


```scala
import io.strongtyped.circe.CirceExt._
import io.strongtyped.circe.CirceExt.Codec


sealed trait Status
case object New extends Status
case object Updated extends Status
case object Deleted extends Status

sealed trait Foo {
  def status: Status
}
case class Bar(value: String, status: Status) extends Foo
case class Baz(value: Int, status: Status) extends Foo
case class Qux(value: Boolean, status: Status) extends Foo

implicit val enumCodec: Codec[Status] = deriveEnumCodec[Status]
implicit val codec: Codec[Foo] = deriveCodec[Foo]

val foo: Foo = Bar("abc", New)
println(foo.asJson)

// output: 
{
  "value" : "abc",
  "status" : "New",
  "_type" : "Bar"
}
```
