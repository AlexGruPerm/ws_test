import io.circe._, io.circe.parser._

val rawJson: String = """{"foo": "bar"}"""

val parseResult = parse(rawJson)