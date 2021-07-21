import zio.ZIO

trait Animal
class Predator extends Animal
class Herbivorous extends Animal
class PredSmall extends Predator
class Dog(name: String) extends PredSmall
class SmallDog(name: String) extends Dog(name)
trait HerbiBig extends Herbivorous
class Cow extends HerbiBig

//**********************************************

val func : Dog => Int = _ => 3

func(new Dog("name"))
func(new SmallDog("name"))

trait Cont[-T] {
  def func2(elm: T): Int
}

val myCont: Cont[Animal] = new Cont[Animal]{
  override def func2(elm: Animal): Int = 123
}

myCont.func2(new Dog("name"))
myCont.func2(new SmallDog("name"))
myCont.func2(new Predator)

