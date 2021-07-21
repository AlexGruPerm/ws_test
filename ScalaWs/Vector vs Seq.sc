
val l: List[Int] = List(1,2,3)
val koef: Int = 10

val lnew: List[Int] = (1 to 5).toList.foldLeft(l){
  case (acc,elm) => acc :+ elm*koef
}

val v: Vector[Int] = Vector(1,2,3)

val c: Array[Int] = Array(1,2,3)

val s: Seq[Int] = List(1,2,3)

println(v)

var ll: List[Int] = List(1,2,3)
//ll ++ List(4)
ll :+ 4
println(ll)



