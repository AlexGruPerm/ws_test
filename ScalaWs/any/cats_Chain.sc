import cats._
import cats.data._

val c = Chain.empty[Int]
val cc = Chain.fromSeq[Int](Seq(1,2,3))
cc.append(4)
cc.prepend(0)

cc.append(c)

cc.append(List(7,8,9))

val nec1 = NonEmptyChain(1, 2, 3, 4)

val nec2 = NonEmptyChain.fromNonEmptyList(NonEmptyList(1, List(2, 3)))


