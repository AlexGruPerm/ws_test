trait Animal
class Predator extends Animal
class Herbivorous extends Animal
class PredSmall extends Predator
class Dog(name: String) extends PredSmall
class SmallDog(name: String) extends Dog(name)
trait HerbiBig extends Herbivorous
class Cow extends HerbiBig

//**********************************************

val pred = new Predator

val dog = new Dog("Buddy")

trait Vet[-T] {
  def heal(animal: T): Boolean
}

/**
 * ‘ункци€ heal контрвариантна по параметру,
 * если мы параметризуем еЄ через Animal, то тип функции становитс€ подтипом Vat[Dog],
 * а значит может принимать всю иерархию от Animal до Dog.
*/
val myVet: Vet[Animal] = new Vet[Animal]{
  override def heal(animal: Animal): Boolean = true
}

myVet.heal(dog)

/**
 * ј если параметризуем через Dog, то она становитс€ подтипом Vat[SmallDog],
 * а значит может принимать всю иерархию от Dog до SmallDog.
*/
val myVet2: Vet[Dog] = new Vet[Dog]{
  override def heal(animal: Dog): Boolean = true
}

myVet2.heal(dog)

val smDog: SmallDog = new SmallDog("small")

myVet2.heal(smDog)


