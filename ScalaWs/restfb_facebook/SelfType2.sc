//https://docs.scala-lang.org/ru/tour/self-types.html
//https://stackoverflow.com/questions/1990948/what-is-the-difference-between-self-types-and-trait-subclasses

trait B {
  def valStr:String = "string from B"
}
trait A extends B {
  def printA(): Unit = {
    println(s"Println in A and value = ${this.valStr}")
  }
}

//Illegal inheritance, self-type Usage does not conform to B trait A

case class Usage() extends A {
  def print() = {
    printA
  }
}

Usage().print()

