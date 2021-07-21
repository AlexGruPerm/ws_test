
object Cont {

  sealed abstract class Option[+A] /*extends Product with Serializable*/ {
    //self  =>
    def isEmpty: Boolean

    def isDefined: Boolean = !isEmpty

    def get: A

    final def map[B](f: A => B):             Option[B] = Some(f(this.get))
    final def flatMap[B](f: A => Option[B]): Option[B] =      f(this.get)

  }

  final case class Some[+A](value: A) extends Option[A] {
    def isEmpty = false
    def get = value
  }

/*  case object None extends Option[Nothing] {
    def isEmpty = true
    def get = throw new NoSuchElementException("None.get")
  }*/

}

import Cont._

val v :List[Cont.Option[Int]] = List(Some(5),Some(6))

v.map()

