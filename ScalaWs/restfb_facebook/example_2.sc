// Scala Program that uses self type
trait A {
  def x = 1
}

trait B extends A {
  override def x: Int = super.x * 5
}

trait C1 extends B {
  override def x = 2
}

trait C2 extends A {
  this: B => override def x = 3
}

println((new C1 with B).x)
println((new C2 with B).x)
