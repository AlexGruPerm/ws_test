import cats.implicits.catsStdInstancesForList
import cats.syntax.functor._

val len: String => Int = _.length

List("scala", "cats").fproduct(len)