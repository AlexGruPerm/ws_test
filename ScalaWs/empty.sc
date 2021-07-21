
/*
val anAbsentString: String = null
anAbsentString.length()
*/

val theNullReference: Null = null

val noString: String = theNullReference
val noList: List[Int] = theNullReference

//scala.collection.immutable.Nil
val anEmptyList: List[Int] = Nil
val emptyListLength: Int = Nil.length

/*`Unit` is a subtype of [[scala.AnyVal]]. There is only one value of type
*  `Unit`, `()`,*/
val s:Unit = () // () - value

//Nothing is the type of no value at all.
val noValAtAll: Nothing = throw new RuntimeException("text of exception")