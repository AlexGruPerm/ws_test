
val f : Int => Int = (v: Int) => {
 println("f ")
 v * 2
}

val g : Int => Int = (v: Int) => {
  println("g ")
  v + 5
}

val c: Int => Int = new Function1[Int,Int]{
  override def apply(v1: Int): Int = v1*v1
}

val f_andThean_g : Int => Int = f andThen g
val f_compose_g: Int => Int = f compose g

f_andThean_g(3) // f = 3*2 = 6 => g(6) = 6+5 = 11
f_compose_g(3)  // g = 3+5 = 8 => f(8) = 8+2 = 16

val cc: Int => Int = c andThen c andThen

cc(3)

