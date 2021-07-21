type IntInt = Int => Int

def x[A <: IntInt](v : A): A = v.compose(v)

val dd: Int => Int = new Function1[Int,Int]{
  override def apply(v1: Int): Int = 123
}

x(dd)