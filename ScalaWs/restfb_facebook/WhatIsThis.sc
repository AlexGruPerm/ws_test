
trait Parent {
  def prnt: Unit = println("P")
  trait Child {
    def prnt: Unit = println("P")
    def check: Unit = this.prnt
  }
}

case class A() extends Parent {
  this.prnt
}