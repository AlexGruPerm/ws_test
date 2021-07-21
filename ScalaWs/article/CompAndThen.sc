
val StrToInt :String => Int = param => param.toInt

val mult2: Int => Int = param => param*2

val addSmall: Int => Double = param => param+0.1

val DblToString :Double => String = param => param.toString

val initVal:String = "123"

(StrToInt andThen mult2 andThen addSmall andThen DblToString)(initVal)

DblToString(addSmall(mult2(StrToInt(initVal))))

val func: String => String = StrToInt andThen mult2 andThen addSmall andThen DblToString


  func(initVal)

val funcComp: String => String = DblToString compose addSmall compose mult2 compose StrToInt

funcComp(initVal)

