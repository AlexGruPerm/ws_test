trait Transformer[A, B] extends (A => B) {
  override def apply(a: A): B = transform(a)
  def transform(a: A): B
}

object FooTransformer extends Transformer[String, String] {
  override def transform(a: String): String = a + ", world"
}

val trans = FooTransformer
val fooList = List("foo", "bar", "baz")
val transformedFooList = fooList map trans

val succ = (x: Int) => x + 1

val anonfun1 = new Function1[Int, Int] {
  def apply(x: Int): Int = x + 1
  def newFunc(x: Int): Int = x*2
}

List(1,2,3) map anonfun1

List(1,2,3) map anonfun1.newFunc