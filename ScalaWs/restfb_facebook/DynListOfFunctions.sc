
type IntInt = Int => Int
type LisOfFunctions = List[IntInt]

def x[A <: IntInt](v : A): A = v

val dd: Int => Int = new Function1[Int,Int]{
  override def apply(v1: Int): Int = 123
}

println(x(dd(1)))

val f1: IntInt = new Function1[Int,Int]{
  override def apply(v1: Int): Int = v1+1
}

val f2: IntInt = new Function1[Int,Int]{
  override def apply(v1: Int): Int = v1+2
}

val f3: IntInt = new Function1[Int,Int]{
  override def apply(v1: Int): Int = v1+3
}

val f4: IntInt = new Function1[Int,Int]{
  override def apply(v1: Int): Int = v1+4
}

val c : LisOfFunctions = List(f1,f2,f3,f4)

val idF: IntInt = new Function1[Int,Int]{
  override def apply(v1: Int): Int = v1
}

val combined: (IntInt,LisOfFunctions) => IntInt = (acc,ls) => {
  ls.foldLeft(acc){ case(acc,func) =>
    acc andThen func
  }
}

val r = combined(idF,c)

List(1,2,3) map r