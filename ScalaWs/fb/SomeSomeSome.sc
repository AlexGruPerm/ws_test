


val v :Option[Option[Option[Int]]] = Some(Some(Some(123)))

println(v)

val singlOpt = v.flatten.flatten