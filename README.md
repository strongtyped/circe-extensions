# Circe Extensions

Some extensions for Circe

## type Codec[A] = Encoder[A] with Decoder[A]

Utility method to generate Encoder[A] and Decoder[A] together
```scala
import io.strongtyped.circe.CirceExt
import io.strongtyped.circe.CirceExt.Codec
implicit val codec: Codec[Foo] = CirceExt.codec[Foo]
```

## Custom type hint

Given the following ADT: 
```scala
sealed trait Foo
case class Bar(value: String) extends Foo
case class Baz(value: Int) extends Foo
case class Qux(value: Boolean) extends Foo
val foo: Foo = Bar("abc")
println(foo.asJson)
```    
Circe default output: 
```json
{
 "Bar": { "value": "abc" }
}
```

The type hint extension allows you to choose include an extra field holding the type hint.

```scala
object Foo {
  import io.strongtyped.circe.CirceExt
  implicit val codec: Codec[Foo] = CirceExt.withTypeHint.codec[Foo]
}
val foo: Foo = Bar("abc")
println(foo.asJson)
```
output: 

```json
{
 "_type": "Bar",
 "value": "abc"
}
```

Or using a custom name for the type hint.
```scala
object Foo {
  import io.strongtyped.circe.CirceExt
  implicit val codec: Codec[Foo] = CirceExt.withTypeHint("$type").codec[Foo]
}
val foo: Foo = Bar("abc")
println(foo.asJson)
```
output: 

```json
{
 "$type": "Bar",
 "value": "abc"
}
```

## Enumeration from sealed tratis

```scala

import io.strongtyped.circe.CirceExt
import io.strongtyped.circe.CirceExt.Codec

  sealed trait Status
  case object New extends Status
  case object Updated extends Status
  case object Deleted extends Status
  object Status {
    implicit val codec: Codec[Status] = CirceExt.codec[Status]
  }

  case class Foo(value: String, status: Status)
  implicit val codec: Codec[Foo] = CirceExt.codec[Foo]
  
  val foo = Foo("abc", New)
  println(foo.asJson)
```    

Circe default output: 

```json
{
 "value": "abc",
 "status": { "New": {} }
}
```

Using the custom enum derivation instead:
```scala
  object Status {
    implicit val codec: Codec[Status] = CirceExt.enum.codec[Status]
  }
  
  val foo = Foo("abc", New)
  println(foo.asJson)
```  

output: 

```json
{
 "value": "abc",
 "status" "New"
}
```
