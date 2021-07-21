

case class Box[A](value :A){
  def map[B](f: A => B) : B = f(value)
}

val b :Box[Int] = Box(3)

val func: Int => String = i => i.toString

b.map(func)